package ai.brain;

import ai.MazAI;
import ai.evaluator.Evaluator;
import ai.evaluator.evaluation.ScoreContainer;
import ai.search.DetonationTreeNode;
import ai.search.GameNode;
import ai.search.RootedTree;
import ai.search.SearchUtil;
import ai.searcher.BeamStackSearch;
import ai.searcher.ISearcher;
import common.Parameters;
import game.Action;
import game.Board;
import game.TurnInput;
import game.TurnResult;
import util.Pair;
import util.logger.ILogger;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Future;

public class FalconBrain<E extends ScoreContainer> implements IBrain {
	private final Evaluator<E> evaluator;
	private final int initialMaxDepth;
	private final int threshold;
	private final ISearcher<E> searcher;

	/**
	 * preActionで、木の作り直しが発生したら深さをリセットする。
	 * postActionで、連鎖が発生したら深さをリセットする。
	 */
	private int currentDepth = -1;

	/**
	 * preActionで、nullまたは根と入力が一致しないなら木を作り直す。
	 */
	private RootedTree<DetonationTreeNode<E>> root = null;

	public FalconBrain(final Evaluator<E> evaluator, final ISearcher<E> searcher, final int initialMaxDepth, final int threshold) {
		super();
		this.evaluator = evaluator;
		this.searcher = searcher;
		this.initialMaxDepth = initialMaxDepth;
		this.threshold = threshold;
	}


	/*
	 * しきい値を超える最速の連鎖をするAI v2
	 */
	@Override
	public void waitingInput(final Future<?> inputFuture) {
		if (inputFuture.isDone()) {
			return;
		}
		if (root == null) {
			return;
		}
		final long stime = System.nanoTime();
		BeamStackSearch.waitingInput(evaluator, root, 0, initialMaxDepth, inputFuture, threshold);
		if (Parameters.LOG_LEVEL >= 3) {
			MazAI.logger.println("Waiting Input " + (System.nanoTime() - stime) / 1000000 + "ms");
		}
	}

	@Override
	public void preAction(final TurnInput input) {
        root = SearchUtil.updateRoot(MazAI.logger, root, input.myBoard, this::resetDepth);
    }

	@Override
	public Action decideAction(final TurnInput input) {
		final ILogger logger = MazAI.logger;

		final RootedTree<DetonationTreeNode<E>> myDetonationTree = searcher.search(evaluator, root, 0, currentDepth, threshold);
		root = myDetonationTree;

		final Optional<RootedTree<DetonationTreeNode<E>>> aggro = SearchUtil.findAggro(myDetonationTree, threshold);

		if (aggro.isPresent()) {
            final ArrayList<RootedTree<DetonationTreeNode<E>>> aggroPath = RootedTree.prunedPath(
					aggro.get());
			final int aggroDepth = aggro.get().get().gameNode.relativeTurn(aggroPath.get(0).get().gameNode);
			currentDepth = aggroDepth - 1; //連鎖が見つかった場合次の探索の深さは1減らす
            logger.println(String.format("+%d: Ag:%d", aggroDepth, aggro.get().get().gameNode.attackDamage()));
			return SearchUtil.firstAction(aggroPath);
		}

		final Optional<RootedTree<DetonationTreeNode<E>>> extension = SearchUtil.findExtension(myDetonationTree);
		resetDepth(); //連鎖が無かった場合深く探索する

		if (extension.isPresent()) {
			return SearchUtil.extendAction(logger, myDetonationTree, extension);
		}

		logger.println("STUCK");
		return Action.drop(0, 0);
	}

	@Override
	public void postAction(final TurnInput input, final Action action) {
		//木を進める処理
		root = SearchUtil.walkToChild(root, action);
		if (Parameters.TENGEN && Parameters.SEARCH_WHILE_WAITING && input.turn == 0) {
			//ハックっぽい
			final Pair<Optional<Board>, TurnResult> resultPair = input.myBoard.simulate(action);
			root = new RootedTree<>(new DetonationTreeNode<>(GameNode.root(resultPair.first.get())));
		}
		if (currentDepth <= 0) {
			resetDepth();
			MazAI.logger.println("Depth reset (Detonated)");
		}
	}

	private void resetDepth() {
		currentDepth = initialMaxDepth;
		searcher.reset();
	}
}
