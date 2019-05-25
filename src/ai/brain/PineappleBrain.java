package ai.brain;

import ai.MazAI;
import ai.evaluator.Evaluator;
import ai.evaluator.Evaluators;
import ai.evaluator.evaluation.ChainContainer;
import ai.evaluator.evaluation.ScoreContainer;
import ai.search.DetonationTreeNode;
import ai.search.GameNode;
import ai.search.RootedTree;
import ai.search.SearchUtil;
import ai.search.ne.ActionContainer;
import ai.search.ne.OptimalMixedStrategy;
import ai.search.ne.Predictor;
import ai.searcher.BeamStackSearch;
import ai.searcher.ISearcher;
import common.Parameters;
import game.*;
import util.Pair;
import util.ProcessWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class PineappleBrain<E extends ChainContainer, A extends ActionContainer, B> implements IBrain {
	private final Evaluator<E> evaluator;
	private final Evaluator<E> evaluatorEnemy;
	
	private final int initialMaxDepth;
	private final ISearcher<E> searcher;
	private final int threshold;
	private final int searchThreshold;
	
	private final int yomiDepth;
	private final int thresholdEnemy;
	private final long thinkTimeEnemy;
	private final boolean thorn;
	
	private final Predictor<E,A,B> predictor;

	private int currentDepth = -1;
	private RootedTree<DetonationTreeNode<E>> root = null;

	PineappleBrain(final Evaluator<E> evaluator, final Predictor<E, A, B> predictor, final int initialMaxDepth, final ISearcher<E> searcher, final int threshold,
				   final Evaluator<E> evaluatorEnemy, final int enemyDepth, final int thresholdEnemy, final long thinkTimeEnemy, final boolean thorn, final int searchThreshold) {
		super();
		this.evaluator = evaluator;
		this.predictor = predictor;
		this.initialMaxDepth = initialMaxDepth;
		this.searcher = searcher;
		this.threshold = threshold;
		this.evaluatorEnemy = evaluatorEnemy;
		this.yomiDepth = enemyDepth;
		this.thresholdEnemy = thresholdEnemy;
		this.thinkTimeEnemy = thinkTimeEnemy;
		this.thorn = thorn;
		this.searchThreshold = searchThreshold;
	}

	//TODO 地獄
	/*
	 * 次のルールで枝刈りする。
	 * (1) 良いノードと、そこから根までのパスのみを残す。
	 * (2) thornsがtrueのとき、1で内点となるノードの(元の)子で、最も火力の高くノードがしきい値を超えるなら、それを生やす。
	 */
	private static <E extends ScoreContainer> RootedTree<DetonationTreeNode<E>> prune(final RootedTree<DetonationTreeNode<E>> root, final int depth,
																					  final int garbageThreshold, final boolean thorns) {

		final ArrayList<RootedTree<DetonationTreeNode<E>>> goodNodes = extractGoodNodes(root, depth, garbageThreshold);

		// うまいこと木を構成する
		final HashMap<RootedTree<DetonationTreeNode<E>>, RootedTree<DetonationTreeNode<E>>> old2new = new HashMap<>();
		final RootedTree<DetonationTreeNode<E>> newRoot = new RootedTree<>(root);
		old2new.put(root, newRoot);

		for (final RootedTree<DetonationTreeNode<E>> leaf : goodNodes) {
			RootedTree<DetonationTreeNode<E>> currentNode = leaf, childNewNode = null;
			while (currentNode != null) {
				boolean update = false;
				RootedTree<DetonationTreeNode<E>> currentNewNode = old2new.get(currentNode);
				if (currentNewNode == null) {
					currentNewNode = new RootedTree<>(currentNode);
					old2new.put(currentNode, currentNewNode);
					update = true;
				}
				if (childNewNode != null) {
					currentNewNode.addChild(childNewNode);
				}
				if (thorns) {
					RootedTree<DetonationTreeNode<E>> bestChild = null;
					for (final RootedTree<DetonationTreeNode<E>> child : currentNode.getChildren()) {
						if (!child.get().isDetonationNode()) {
							continue;
						}
						if (bestChild == null || bestChild.get().gameNode.attackDamage() < child.get().gameNode
								.attackDamage()) {
							bestChild = child;
						}
					}
					if (bestChild != null && bestChild.get().gameNode.attackDamage() >= garbageThreshold) {
						if (old2new.get(bestChild) == null) {
							final RootedTree<DetonationTreeNode<E>> newBestChild = new RootedTree<>(bestChild);
							currentNewNode.addChild(newBestChild);
							old2new.put(bestChild, newBestChild);
						}
					}
				}

				childNewNode = currentNewNode;
				currentNode = currentNode.getParent();
				if (!update) {
					break;
				}
			}
		}

		return newRoot;
	}

	/**
	 * 各深さで最も火力の高い発火ノードのリストを返す。
	 *
	 * @param root             根
	 * @param garbageThreshold しきい値
	 * @return 「良い」ノードのリスト
	 */
	private static <E extends ScoreContainer> ArrayList<RootedTree<DetonationTreeNode<E>>> extractGoodNodes(final RootedTree<DetonationTreeNode<E>> root,
																											final int depth, final int garbageThreshold) {
		final HashMap<Integer, RootedTree<DetonationTreeNode<E>>> detonations = new HashMap<>();

		final int rootTurn = root.get().gameNode.turn();
		root.stream().forEachOrdered((treeNode) -> {
			final DetonationTreeNode<E> node = treeNode.get();
			if (node.isDetonationNode() && node.garbageSentOrZero() >= garbageThreshold && node.gameNode.relativeTurn(rootTurn) <= depth) {
				final RootedTree<DetonationTreeNode<E>> currentGood = detonations.get(node.gameNode.relativeTurn(rootTurn));

				if (currentGood == null
						|| currentGood.get().gameNode.attackDamage() <= node.garbageSentOrZero()) {
					detonations.put(node.gameNode.relativeTurn(rootTurn), treeNode);
				}
			}
		});

		return new ArrayList<>(detonations.values());
	}

	@SuppressWarnings("unchecked")
	private static void saveDetonationTrees(final int turn, final RootedTree<?>... trees) {
		final RootedTree<String>[] trees2 = new RootedTree[trees.length];
		for (int i = 0; i < trees.length; i++) {
			trees2[i] = trees[i].map(Object::toString);
		}
		saveTrees(turn, trees2);
	}

	@SafeVarargs
	private static void saveTrees(final int turn, final RootedTree<String>... trees) {
		final String fileName = MazAI.timeString + "_" + turn + ".png";
		final String path = Parameters.WORKING_DIRECTORY.resolve(Parameters.LOG_DIRECTORY).resolve(fileName)
				.toAbsolutePath().toString();
		final ProcessWriter pw = new ProcessWriter("cmd", "/c", "dot", "-Tpng", "-o" + path);
		pw.println("digraph detonationTree {");
		for (final RootedTree<String> tree : trees) {
			printTree(pw, tree);
		}
		pw.println("}");
		pw.close();
	}

	private static void printTree(final ProcessWriter pw, final RootedTree<String> v) {
		pw.println(String.format("Node_%x [label=\"%s\"]", v.hashCode(), v.get()));
		for (final RootedTree<String> u : v.getChildren()) {
			pw.println(String.format("Node_%x -> Node_%x;", v.hashCode(), u.hashCode()));
			printTree(pw, u);
		}
	}

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
			MazAI.logger.println("thought additional " + (System.nanoTime() - stime) / 1000000 + "ms");
		}
	}

	@Override
	public void preAction(final TurnInput input) {
        root = SearchUtil.updateRoot(MazAI.logger, root, input.myBoard, this::resetDepth);
	}

	@Override
	public Action decideAction(final TurnInput input) {
        if (Parameters.LOG_LEVEL >= 3) {
            MazAI.logger.println("Depth:" + currentDepth);
        }
		final Result res = nextActionMain(input);
		MazAI.logger.println(res.turnMessage);
        currentDepth = nextDepth(res.nextAction);
		return res.nextAction;
	}

    private int nextDepth(final Action nextAction) {
        if (root.getChildren() == null || root.getChildren().isEmpty()) {
            return initialMaxDepth;
        }
        boolean detonate = false;
        for (final RootedTree<DetonationTreeNode<E>> c : root.getChildren()) {
            if (c.get().gameNode.lastAction().get().equals(nextAction)) {
                detonate = c.get().garbageSentOrZero() >= 5;
                break;
            }
        }
        if (detonate) {
            return 0;
        }

        final Optional<RootedTree<DetonationTreeNode<E>>> aggro;
        if (Parameters.USE_SMART_AGGRO) {
            aggro = SearchUtil.findAggroSmart(root, threshold);
        } else {
            aggro = SearchUtil.findAggro(root, threshold);
        }

        if (!aggro.isPresent()) {
            return initialMaxDepth;
        }
        final Optional<RootedTree<DetonationTreeNode<E>>> bigAggro = SearchUtil.findAggro(root, searchThreshold);
        return bigAggro.map(detonationTreeNodeRootedTree -> Math.max(RootedTree.prunedPath(detonationTreeNodeRootedTree).size() - 1 - 1, yomiDepth))
                .orElseGet(() -> Math.max(RootedTree.prunedPath(aggro.get()).size() - 1 + 3, yomiDepth));
    }

    /*
     * しきい値以上の連鎖までのターン数が
     * 自分\敵	~5	N/A
     * ~5		(1)	(2)
     * ~10		(3)	(3)
     * N/A		(4) (4)
     * (1) - ナッシュ均衡を計算して、最適な混合戦略をとる
     * (2) - 5T以内の最も大きい連鎖を目指す
     * (3) - 最も早い連鎖を目指す
     * (4) - 評価関数を用いて連鎖を伸ばす
     *
     * 5とか10は実際にはそれぞれdepth2,depthとかのパラメータ。
     * (2)が必要なのは、(1)で後発火にしたときに適切なパスをとるため。
     */
    private Result nextActionMain(final TurnInput input) {
        final RootedTree<DetonationTreeNode<E>> myDetonationTree = searcher.search(evaluator, root, yomiDepth, currentDepth, searchThreshold);
        root = myDetonationTree;

        if (Parameters.LOG_LEVEL >= 4 && input.turn == 0) {
            final RootedTree<DetonationTreeNode<E>> tree = RootedTree.prunedTree(myDetonationTree, myDetonationTree.stream().filter(root -> root.get().garbageSentOrZero() >= threshold).collect(Collectors.toCollection(ArrayList::new)));
            saveDetonationTrees(0, tree);
        }

        if (Parameters.DETONATION_SEARCH_THINKTIME_INSTANT >= 100) {
            final Result instant = checkInstant(input, myDetonationTree);
            if (instant != null) return instant;
        }

        final Optional<RootedTree<DetonationTreeNode<E>>> aggro = SearchUtil.findAggro(myDetonationTree, threshold);

        if (aggro.isPresent() && aggro.get().get().gameNode.relativeTurn(input.myBoard.turn) <= yomiDepth) {
            final RootedTree<DetonationTreeNode<E>> enemyDetonationTree = BeamStackSearch.detonationTree(evaluatorEnemy, input.enemyBoard, yomiDepth, thinkTimeEnemy);
            final RootedTree<DetonationTreeNode<E>> myDetonationTreePruned = prune(myDetonationTree, yomiDepth, threshold, thorn);
            final RootedTree<DetonationTreeNode<E>> enemyDetonationTreePruned = prune(enemyDetonationTree, yomiDepth, thresholdEnemy, thorn);
            if (enemyDetonationTreePruned.getChildren().size() == 0) {
                // パターン2
                final RootedTree<DetonationTreeNode<E>> bigChain = SearchUtil.findBigChain(myDetonationTreePruned, yomiDepth).get();
                final ArrayList<RootedTree<DetonationTreeNode<E>>> bigPath = RootedTree.prunedPath(bigChain);
                final DetonationTreeNode<E> bigNode = SearchUtil.lastNode(bigPath);
                return new Result(
                        SearchUtil.firstAction(bigPath),
                        String.format("+%d: B:%d", bigNode.gameNode.relativeTurn(input.turn), bigNode.garbageSentOrZero()));
            }

            //パターン1
            final RootedTree<A> myDetonationGameTree = myDetonationTreePruned.map(predictor::myNodeMapper);
            final RootedTree<B> enemyDetonationGameTree = enemyDetonationTreePruned.map(predictor::enemyNodeMapper);
            if (Parameters.LOG_LEVEL >= 3) {
                saveDetonationTrees(input.turn, myDetonationGameTree, enemyDetonationGameTree);
            }

            final OptimalMixedStrategy.Strategy optimalMixedStrategy = OptimalMixedStrategy.optimalMixedStrategy(myDetonationGameTree, enemyDetonationGameTree, predictor);
            final Action nextAction = executeMixedStrategy(optimalMixedStrategy);
            return new Result(
                    nextAction,
                    String.format("NE:%.3f", optimalMixedStrategy.expectedReward));
        }

        if (aggro.isPresent()) {
            //パターン3
            final ArrayList<RootedTree<DetonationTreeNode<E>>> aggroPath = RootedTree.prunedPath(aggro.get());
            final DetonationTreeNode<E> aggroNode = SearchUtil.lastNode(aggroPath);
            return new Result(
                    SearchUtil.firstAction(aggroPath),
                    String.format("+%d: Ag:%d", aggroNode.gameNode.relativeTurn(input.turn), aggroNode.garbageSentOrZero()));
        }

        //パターン4
        final Optional<RootedTree<DetonationTreeNode<E>>> extension = SearchUtil.findExtension(myDetonationTree);
        resetDepth();

        if (extension.isPresent()) {
            final ArrayList<RootedTree<DetonationTreeNode<E>>> extendPath = RootedTree.prunedPath(extension.get());
            final DetonationTreeNode<E> extendNode = SearchUtil.lastNode(extendPath);
            return new Result(
                    SearchUtil.firstAction(extendPath),
                    String.format("+%d: Ex:%.2f", extendNode.gameNode.relativeTurn(input.turn), extendNode.boardScoreOrNeginf()));
        }

        //詰み
        return new Result(Action.drop(0, 0), "STUCK");
    }

	private Result checkInstant(final TurnInput input, final RootedTree<DetonationTreeNode<E>> myDetonationTree) {
		final Optional<RootedTree<DetonationTreeNode<E>>> instant = SearchUtil.findInstant(myDetonationTree);
		if (instant.isPresent()) {
			final long instantDamage = instant.get().get().garbageSentOrZero();
            MazAI.logger.println("I:" + instantDamage);
            if (instantDamage + input.enemyBoard.garbage >= GameConstants.FIELD_WIDTH) {
                final int instantDepth = yomiDepth;
				//相手の元の火力がすごく減ってたら成功とみなす
				final RootedTree<DetonationTreeNode<E>> rootedTree = SearchUtil.<E>nextRoot(null, input.enemyBoard).second;
                final RootedTree<DetonationTreeNode<E>> enemyDetonationTree = BeamStackSearch.detonationTree(
                        evaluatorEnemy,
                        rootedTree,
                        instantDepth,
                        instantDepth,
                        Parameters.DETONATION_SEARCH_THINKTIME_INSTANT,
                        1L << 60,
                        1,
                        instantDamage,
                        instant.get().get().gameNode.getEdge().get().result.skillReduce);
				final Optional<RootedTree<DetonationTreeNode<E>>> extension = SearchUtil.findExtension(enemyDetonationTree);
                if (!extension.isPresent()) {
                    //相手はもうこのターンで死んでる
                    return new Result(
							SearchUtil.firstAction(RootedTree.prunedPath(instant.get())),
                            "Mate (0)");
                }
				final int enemyDepth = RootedTree.prunedPath(extension.get()).size() - 1;
                if (enemyDepth < instantDepth) {
                    return new Result(
							SearchUtil.firstAction(RootedTree.prunedPath(instant.get())),
                            String.format("Mate (%d)", enemyDepth));
                }

                MazAI.logger.println(String.format("EnemyDepth:%d, EnemySkill:%d", enemyDepth, input.enemyBoard.skill));
                if (input.enemyBoard.skill <= 40) {
                    final long enemyDamage = Evaluators.getChainFinder().findChain(input.enemyBoard.field).score / 2;
                    final long maxDamage = enemyDetonationTree.stream()
                            .filter(node -> node.get().isExtendNode())
                            .mapToLong(r -> r.get().evaluation.get().getChain().get().score / 2)
                            .max().orElse(0);
                    MazAI.logger.println(String.format("%d -> %d,", enemyDamage, maxDamage));
                    if (enemyDamage >= 20 && maxDamage <= 20) {
                        return new Result(
								SearchUtil.firstAction(RootedTree.prunedPath(instant.get())),
                                String.format("Tsubushi (%d -> %d)", enemyDamage, maxDamage));
                    }
                }
            }
        }
        return null;
    }

	private Action executeMixedStrategy(final OptimalMixedStrategy.Strategy optimalMixedStrategy) {
		double random = Math.random();
		for (final Map.Entry<Action, Double> e : optimalMixedStrategy.strategy.entrySet()) {
			random -= e.getValue();
			if (random < 1E-10) {
				return e.getKey();
			}
		}
		throw new RuntimeException("What");
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

	class Result {
		final Action nextAction;
		final String turnMessage;

		Result(final Action nextAction, final String turnMessage) {
			super();
			this.nextAction = nextAction;
			this.turnMessage = turnMessage;
		}
	}

	private void resetDepth() {
		currentDepth = initialMaxDepth;
		searcher.reset();
	}
}
