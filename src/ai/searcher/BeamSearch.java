package ai.searcher;

import ai.evaluator.Evaluator;
import ai.evaluator.evaluation.ScoreContainer;
import ai.search.DetonationTreeNode;
import ai.search.GameNode;
import ai.search.RootedTree;

import java.util.*;

/*
 * 少しの悪い遷移を許可したビームサーチ。
 * この探索方法に名前がついているか分からなかった。
 * 反復局所探索法(Iterated Local Search)における開始点を選び直す処理である摂動(Perturbation)にちょっと似ているのでPerturbation beam searchとした。
 * 
 * ビームのうち最良のものの子は評価値が悪くとも必ず展開することにして、一度悪い遷移を許可する。
 */
public class BeamSearch {
	@SuppressWarnings("unchecked")
    private static <T extends ScoreContainer> RootedTree<DetonationTreeNode<T>> detonationTree(final Evaluator<T> evaluator, final RootedTree<DetonationTreeNode<T>> root, final int minDepth, final int maxDepth, final int width, final long threshold) {
		
		final PriorityQueue<RootedTree<DetonationTreeNode<T>>>[] beam = new PriorityQueue[maxDepth+1];
		final List<RootedTree<DetonationTreeNode<T>>>[] pBeam = new ArrayList[maxDepth+1];
        final Set<Long>[] hs = new Set[maxDepth + 1];
		
		for(int i=0;i<=maxDepth;i++) {
			beam[i] = new PriorityQueue<>((n1,n2)->-Double.compare(n1.get().evaluation.get().getScore(), n2.get().evaluation.get().getScore()));
			pBeam[i] = new ArrayList<>();
            hs[i] = ParallelSearchUtil.emptySet();
		}
		
		beam[0].add(root);
		
		for(int d=0;d<maxDepth;d++) {
			boolean findBigDetonation = false;
			final HashSet<Integer> opened = new HashSet<>();
			for(int i=0;i<width;i++) {
				if (beam[d].isEmpty()) {
					break;
				}

				final RootedTree<DetonationTreeNode<T>> currentTreeNode = beam[d].poll();
				findBigDetonation |= openNode(evaluator, threshold, beam, pBeam, hs, d, findBigDetonation, i == 0,
						currentTreeNode);
				opened.add(System.identityHashCode(currentTreeNode));
			}

			for (final RootedTree<DetonationTreeNode<T>> currentTreeNode : pBeam[d]) {
				if (!opened.contains(System.identityHashCode(currentTreeNode))) {
					findBigDetonation |= openNode(evaluator, threshold, beam, pBeam, hs, d, findBigDetonation, false,
							currentTreeNode);
				}
			}
			
			if (findBigDetonation && d + 1 >= minDepth) {
				break;
			}
		}
		
		return root;
	}

	private static <T extends ScoreContainer> boolean openNode(final Evaluator<T> evaluator, final long threshold,
															   final PriorityQueue<RootedTree<DetonationTreeNode<T>>>[] beam, final List<RootedTree<DetonationTreeNode<T>>>[] pBeam,
															   final Set<Long>[] hs, final int d, boolean findBigDetonation, final boolean isBestNode, final RootedTree<DetonationTreeNode<T>> currentTreeNode) {
		
		//ノードの展開とビームへの変更を分離したい
        ParallelSearchUtil.calcChildren(evaluator, hs, d, currentTreeNode, 0, 0);

		for (final RootedTree<DetonationTreeNode<T>> nextTreeNode : currentTreeNode.getChildren()) {
			final GameNode nextNode = nextTreeNode.get().gameNode;
			if (nextTreeNode.get().isDetonationNode()) {
				final long garbage = nextNode.getEdge().get().result.attackDamage;
				if (garbage >= threshold) {
					findBigDetonation = true;
				}
			}else {
				if (isBestNode) {
					pBeam[d + 1].add(nextTreeNode);
				}
				beam[d + 1].add(nextTreeNode);
				hs[d + 1].add(nextNode.board.field.longHashCode());
			}
		}
			
		return findBigDetonation;
	}

	public static class Searcher<T extends ScoreContainer> implements ISearcher<T> {
        final int width;

		public Searcher(final int width) {
			super();
			this.width = width;
		}

		public RootedTree<DetonationTreeNode<T>> search(final Evaluator<T> evaluator, final RootedTree<DetonationTreeNode<T>> root,
														final int minDepth, final int initialDepth, final long threshold) {
			return BeamSearch.detonationTree(evaluator, root, minDepth, initialDepth, width, threshold);
		}

        @Override
        public void reset() {

        }
	}
}
