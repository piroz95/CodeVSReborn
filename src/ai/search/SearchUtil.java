package ai.search;

import ai.MazAI;
import ai.evaluator.evaluation.ScoreContainer;
import common.Parameters;
import game.Action;
import game.Board;
import util.Pair;
import util.logger.ILogger;

import java.util.*;
import java.util.stream.Collectors;

public class SearchUtil {
	public static <E extends ScoreContainer> Optional<RootedTree<DetonationTreeNode<E>>> findAggro(final RootedTree<DetonationTreeNode<E>> root,
																								   final int garbageThreshold) {
        return root.stream().filter((x) -> x.get().gameNode.attackDamage() >= garbageThreshold).min((x1, x2) -> {
			if (x1.get().gameNode.turn() != x2.get().gameNode.turn()) {
				return Integer.compare(x1.get().gameNode.turn(), x2.get().gameNode.turn());
			}
			return -Double.compare(x1.get().garbageSentOrZero(), x2.get().garbageSentOrZero());
		});
	}

    public static <E extends ScoreContainer> Optional<RootedTree<DetonationTreeNode<E>>> findAggroSmart(final RootedTree<DetonationTreeNode<E>> root,
                                                                                                        final int garbageThreshold) {
        final List<RootedTree<DetonationTreeNode<E>>> bigDetonations = root.stream()
                .filter(node -> node.get().isDetonationNode() && node.get().garbageSentOrZero() >= garbageThreshold)
                .collect(Collectors.toList());
        if (bigDetonations.isEmpty()) {
            return Optional.empty();
        }
        final RootedTree<DetonationTreeNode<E>> goodDetonation = bigDetonations.stream().max((node1, node2) -> {
            if (node1.get().gameNode.turn() != node2.get().gameNode.turn()) {
                return -Integer.compare(node1.get().gameNode.turn(), node2.get().gameNode.turn());
            }
            return Long.compare(node1.get().garbageSentOrZero(), node2.get().garbageSentOrZero());
        }).get();
        final List<RootedTree<DetonationTreeNode<E>>> goodDetonations = bigDetonations.stream()
                .filter(node -> node.get().gameNode.turn() == goodDetonation.get().gameNode.turn() &&
                        node.get().garbageSentOrZero() == goodDetonation.get().garbageSentOrZero())
                .collect(Collectors.toList());
        if (Parameters.LOG_LEVEL >= 4) {
            MazAI.logger.println("GoodAggros:" + goodDetonations.size());
        }
        if (goodDetonations.size() == 1) {
            return Optional.of(goodDetonation);
        }
        return Optional.of(goodNode(goodDetonations, goodDetonation.get().garbageSentOrZero()));
    }

    private static <E extends ScoreContainer> RootedTree<DetonationTreeNode<E>> goodNode(final List<RootedTree<DetonationTreeNode<E>>> goodDetonations, final long garbageThresholdExcluded) {
        final HashMap<RootedTree<DetonationTreeNode<E>>, Pair<Integer, RootedTree<DetonationTreeNode<E>>>> map = new HashMap<>();
        final Queue<RootedTree<DetonationTreeNode<E>>> q = new ArrayDeque<>();
        for (final RootedTree<DetonationTreeNode<E>> v : goodDetonations) {
            q.offer(v);
            map.put(v, new Pair<>(0, v));
        }
        while (!q.isEmpty()) {
            final RootedTree<DetonationTreeNode<E>> cur = q.poll();
            final Pair<Integer, RootedTree<DetonationTreeNode<E>>> p = map.get(cur);
            if (cur.get().garbageSentOrZero() > garbageThresholdExcluded) {
                if (Parameters.LOG_LEVEL >= 4) {
                    MazAI.logger.println("bestDist:" + p.first);
                }
                return p.second;
            }
            final RootedTree<DetonationTreeNode<E>> parent = cur.getParent();
            if (parent != null) {
                if (!map.containsKey(parent)) {
                    map.put(parent, new Pair<>(p.first + 1, p.second));
                    q.offer(parent);
                }
            }
            for (final RootedTree<DetonationTreeNode<E>> child : cur.getChildren()) {
                if (!map.containsKey(child)) {
                    map.put(child, new Pair<>(p.first + 1, p.second));
                    q.offer(child);
                }
            }
        }
        if (Parameters.LOG_LEVEL >= 4) {
            MazAI.logger.println("No aggro pair found");
        }
        return goodDetonations.get(0);
    }

	public static <E extends ScoreContainer> Optional<RootedTree<DetonationTreeNode<E>>> findBigChain(final RootedTree<DetonationTreeNode<E>> root, final int maxDepth) {
		return root.stream().filter((x) -> x.get().gameNode.relativeTurn(root.get().gameNode) <= maxDepth && x.get().isDetonationNode())
				.max((x1, x2) -> {
					if (x1.get().garbageSentOrZero() != x2.get().garbageSentOrZero()) {
						return Double.compare(x1.get().garbageSentOrZero(), x2.get().garbageSentOrZero());
					}
					return -Integer.compare(x1.get().gameNode.turn(), x2.get().gameNode.turn());
				});
	}

    public static <E extends ScoreContainer> Optional<RootedTree<DetonationTreeNode<E>>> findBigChain2(final RootedTree<DetonationTreeNode<E>> root, final int maxDepth) {
        return root.stream().filter((x) -> x.get().gameNode.relativeTurn(root.get().gameNode) <= maxDepth && !x.get().isRoot())
                .max((x1, x2) -> {
                    if (x1.get().garbageSentOrZero() != x2.get().garbageSentOrZero()) {
                        return Double.compare(x1.get().garbageSentOrZero(), x2.get().garbageSentOrZero());
                    }
                    return -Integer.compare(x1.get().gameNode.turn(), x2.get().gameNode.turn());
                });
    }

	public static <E extends ScoreContainer> Optional<RootedTree<DetonationTreeNode<E>>> findExtension(final RootedTree<DetonationTreeNode<E>> root) {
		return root.stream().filter((x)->x.get().isExtendNode()).max((x1,x2)->{
			if (x1.get().gameNode.turn() != x2.get().gameNode.turn()) {
				return Integer.compare(x1.get().gameNode.turn(), x2.get().gameNode.turn());
			}
			return Double.compare(x1.get().boardScoreOrNeginf(), x2.get().boardScoreOrNeginf());
		});
	}

	public static <E extends ScoreContainer> Optional<RootedTree<DetonationTreeNode<E>>> findInstant(final RootedTree<DetonationTreeNode<E>> root) {
        return root.stream().filter((x) -> x.get().gameNode.relativeTurn(root.get().gameNode) == 1 && x.get().isDetonationNode())
                .max(Comparator.comparingDouble(x -> x.get().garbageSentOrZero()));
    }
	
	@Deprecated
	public static <E extends ScoreContainer> Action extendAction(final ILogger logger, final RootedTree<DetonationTreeNode<E>> myDetonationTree,
																 final Optional<RootedTree<DetonationTreeNode<E>>> extension) {
		final ArrayList<RootedTree<DetonationTreeNode<E>>> extendPath = RootedTree.prunedPath(
				extension.get());
		logger.println(String.format("+%d: Ex:%.3f", extension.get().get().gameNode.relativeTurn(extendPath.get(0).get().gameNode), extension.get().get().boardScoreOrNeginf()));
		return SearchUtil.firstAction(extendPath);
	}

	public static <E extends ScoreContainer> Action firstAction(final ArrayList<RootedTree<DetonationTreeNode<E>>> path) {
		return path.get(1).get().gameNode.lastAction().get();
	}

	public static <E extends ScoreContainer> DetonationTreeNode<E> lastNode(final ArrayList<RootedTree<DetonationTreeNode<E>>> path) {
		return path.get(path.size()-1).get();
	}
	
	/* 
	 * 木(previousRoot)の深さ1のノードからinitialBoardと一致するものを探し、存在すればそのノードを返し、そうでなければ新たにノードを作って返す。
	 * 一致するノードがあった場合は、ついでに親へのリンクを切る（メモリリークするので）
	 */
	public static <E extends ScoreContainer> Pair<Boolean, RootedTree<DetonationTreeNode<E>>> nextRoot(final RootedTree<DetonationTreeNode<E>> previousRoot, final Board initialBoard) {
		if (previousRoot != null) {
			for (final RootedTree<DetonationTreeNode<E>> c : previousRoot.getChildren()) {
				if (c.get().gameNode.board.weaklyEquals(initialBoard)) {
					c.cutParent();
					c.get().setAsRoot();
					return new Pair<>(true, c);
				}
			}
		}

		return new Pair<>(false,new RootedTree<>(new DetonationTreeNode<>(GameNode.root(initialBoard))));
	}

    /**
     * postActionで決めたアクションの部分木に移動する。詰みの場合はnullを返す。
     *
     * @param root   現在の発火木の根
     * @param action このターンの行動
     * @param <E>    評価の型
     * @return 発火木が詰みでなければ、対応する子。詰みならnull。
     */
	public static <E extends ScoreContainer> RootedTree<DetonationTreeNode<E>> walkToChild(final RootedTree<DetonationTreeNode<E>> root, final Action action) {
        if (root == null) {
			return null;
//            throw new NullPointerException();
        }

		for (final RootedTree<DetonationTreeNode<E>> c : root.getChildren()) {
            if (c.get().gameNode.lastAction().get().equals(action)) {
                c.cutParent();
                c.get().setAsRoot();
                return c;
            }
        }

        return null;
    }

	public static <E extends ScoreContainer> RootedTree<DetonationTreeNode<E>> updateRoot(final ILogger logger, final RootedTree<DetonationTreeNode<E>> root, final Board inputBoard, final Runnable reset) {
        if (root == null) {
            logger.println("Tree reset (Not existed)");
            reset.run();
            return new RootedTree<>(new DetonationTreeNode<>(GameNode.root(inputBoard)));
        }
        final Board rootBoard = root.get().gameNode.board;
        if (rootBoard.weaklyEquals(inputBoard)) {
            return root;
        }
        if (rootBoard.turn != inputBoard.turn) {
            logger.println("!!! Turn Not Matched !!!"); //起きてはいけないはず
        }
        if (!rootBoard.field.equals(inputBoard.field)) {
            logger.println("Tree reset (Field not matched ?????)"); //起きてはいけないはず
            reset.run();
            return new RootedTree<>(new DetonationTreeNode<>(GameNode.root(inputBoard)));
        }
		final long garbageDiff = inputBoard.garbage - rootBoard.garbage;
		final long skillDiff = inputBoard.skill - rootBoard.skill;
        logger.println("Tree reset (Attacked?: G" + garbageDiff + " S" + skillDiff + ")");
        reset.run();
        return new RootedTree<>(new DetonationTreeNode<>(GameNode.root(inputBoard)));
    }
}
