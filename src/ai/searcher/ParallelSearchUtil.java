package ai.searcher;

import ai.evaluator.Evaluator;
import ai.evaluator.evaluation.ScoreContainer;
import ai.search.DetonationTreeNode;
import ai.search.GameNode;
import ai.search.RootedTree;
import common.Parameters;

import java.util.*;
import java.util.concurrent.*;

class ParallelSearchUtil {
    public static <T extends ScoreContainer> void calcChildren(final Evaluator<T> evaluator, final Set<Long>[] hs, final int d, final RootedTree<DetonationTreeNode<T>> currentTreeNode, final long additionalGarbage, final int additionalSkill) {
        if (Parameters.NTHREADS <= 1) {
            calcChildrenSingleThread(evaluator, hs, d, currentTreeNode, additionalGarbage, additionalSkill);
        } else {
            calcChildrenMultiThread(evaluator, hs, d, currentTreeNode, true, additionalGarbage, additionalSkill);
        }
    }

    private static <T extends ScoreContainer> void calcChildrenSingleThread(final Evaluator<T> evaluator, final Set<Long>[] hs, final int d, final RootedTree<DetonationTreeNode<T>> currentTreeNode, final long additionalGarbage, final int additionalSkill) {
        if (currentTreeNode.getChildren().isEmpty()) {
            final GameNode currentNode = currentTreeNode.get().gameNode;
            for (final GameNode nextNode : currentNode.children(true, additionalGarbage, additionalSkill)) {
                if (nextNode.getEdge().get().result.attackDamage >= 5) {
                    final RootedTree<DetonationTreeNode<T>> nextTreeNode = new RootedTree<>(
                            new DetonationTreeNode<>(nextNode));
                    currentTreeNode.addChild(nextTreeNode);
                } else {
                    if (hs[d + 1].contains(nextNode.board.field.longHashCode())) {
                        continue;
                    }

                    final DetonationTreeNode<T> evaluatedNextState = new DetonationTreeNode<>(nextNode, evaluator.evaluate(nextNode));
                    final RootedTree<DetonationTreeNode<T>> nextTreeNode = new RootedTree<>(evaluatedNextState);
                    currentTreeNode.addChild(nextTreeNode);
                }
            }
        }
    }

    private static ExecutorService es = null;

    // Evaluatorとそれが呼ぶやつはスレッドセーフを仮定する。
    private static <T extends ScoreContainer> void calcChildrenMultiThread(final Evaluator<T> evaluator, final Set<Long>[] hs, final int d, final RootedTree<DetonationTreeNode<T>> currentTreeNode, final boolean det, final long additionalGarbage, final int additionalSkill) {
        if (es == null) {
            es = Executors.newFixedThreadPool(Parameters.NTHREADS, new DaemonThreadFactory());
        }
        if (!currentTreeNode.getChildren().isEmpty()) {
            return;
        }
        final GameNode currentNode = currentTreeNode.get().gameNode;
        final List<Future<Optional<RootedTree<DetonationTreeNode<T>>>>> futureList = new ArrayList<>();
        for (final GameNode nextNode : currentNode.children(true, additionalGarbage, additionalSkill)) {
            futureList.add(es.submit(() -> {
                if (nextNode.getEdge().get().result.attackDamage >= 5 && det) {
                    return Optional.of(new RootedTree<>(new DetonationTreeNode<>(nextNode)));
                } else {
                    return Optional.of(new RootedTree<>(new DetonationTreeNode<>(nextNode, evaluator.evaluate(nextNode))));
                }
            }));
        }
        for (final Future<Optional<RootedTree<DetonationTreeNode<T>>>> f : futureList) {
            try {
                f.get().ifPresent(child -> {
                    if (hs[d + 1].contains(child.get().gameNode.board.field.longHashCode())) {
                        return;
                    }
                    currentTreeNode.addChild(child);
                });
            } catch (final InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    //なんか並列アクセスできるSetが必要な気がしたんだけど違かった
    public static <E> Set<E> emptySet() {
//        if (Parameters.NTHREADS <= 1) {
//            return new HashSet<>();
//        }else{
        return Collections.newSetFromMap(new ConcurrentHashMap<>());
//        }
    }

    private static class DaemonThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(final Runnable r) {
            final Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    }
}
