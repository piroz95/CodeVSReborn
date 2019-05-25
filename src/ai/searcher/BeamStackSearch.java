package ai.searcher;

import ai.evaluator.Evaluator;
import ai.evaluator.evaluation.ScoreContainer;
import ai.search.DetonationTreeNode;
import ai.search.GameNode;
import ai.search.RootedTree;
import game.Board;

import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.Future;

public class BeamStackSearch {
    @SuppressWarnings("unchecked")
    public static <T extends ScoreContainer> RootedTree<DetonationTreeNode<T>> detonationTree(final Evaluator<T> evaluator, final RootedTree<DetonationTreeNode<T>> root, final int minDepth, final int initialDepth, final long timeLimitMS, final long threshold, final int addGarbageTurn, final long additionalGarbage, final int additionalSkill) {
        final long startTimeNS = System.nanoTime();

        final PriorityQueue<RootedTree<DetonationTreeNode<T>>>[] beam = new PriorityQueue[initialDepth + 1];
        final Set<Long>[] hs = new Set[initialDepth + 1];

        for (int i = 0; i <= initialDepth; i++) {
            beam[i] = new PriorityQueue<>((n1, n2) -> -Double.compare(n1.get().evaluation.get().getScore(), n2.get().evaluation.get().getScore()));
            hs[i] = ParallelSearchUtil.emptySet();
        }

        beam[0].add(root);

        int depth = initialDepth;
        while ((System.nanoTime() - startTimeNS) / 1000000 <= timeLimitMS && depth >= 0) {
            depth = singleIteration(evaluator, minDepth, depth, beam, hs, threshold, addGarbageTurn, additionalGarbage, additionalSkill);
        }

        return root;
    }

    private static <T extends ScoreContainer> RootedTree<DetonationTreeNode<T>> detonationTree(final Evaluator<T> evaluator, final RootedTree<DetonationTreeNode<T>> root, final int minDepth, final int initialDepth, final long timeLimitMS, final long threshold) {
        return detonationTree(evaluator, root, minDepth, initialDepth, timeLimitMS, threshold, -1, 0, 0);
    }

    @SuppressWarnings("unchecked")
    public static <T extends ScoreContainer> void waitingInput(final Evaluator<T> evaluator, final RootedTree<DetonationTreeNode<T>> root, final int minDepth, final int initialDepth, final Future<?> inputFuture, final long threshold) {
        final PriorityQueue<RootedTree<DetonationTreeNode<T>>>[] beam = new PriorityQueue[initialDepth + 1];
        final Set<Long>[] hs = new Set[initialDepth + 1];

        for (int i = 0; i <= initialDepth; i++) {
            beam[i] = new PriorityQueue<>((n1, n2) -> -Double.compare(n1.get().evaluation.get().getScore(), n2.get().evaluation.get().getScore()));
            hs[i] = ParallelSearchUtil.emptySet();
        }

        beam[0].add(root);

        int depth = initialDepth;
        while (!inputFuture.isDone() && depth >= 0) {
            depth = singleIteration(evaluator, minDepth, depth, beam, hs, threshold, -1, 0, 0);
        }

    }

    //old signature
    public static <T extends ScoreContainer> RootedTree<DetonationTreeNode<T>> detonationTree(final Evaluator<T> evaluator, final Board b2, final int depth, final long thinkTimeMS) {
        return detonationTree(evaluator, new RootedTree<>(new DetonationTreeNode<>(GameNode.root(b2))), depth, depth, thinkTimeMS, 1L << 60);
    }

    private static <T extends ScoreContainer> int singleIteration(final Evaluator<T> evaluator, final int depth, final int initialDepth,
                                                                  final PriorityQueue<RootedTree<DetonationTreeNode<T>>>[] beam, final Set<Long>[] hs, final long threshold, final int addGarbageTurn, final long additionalGarbage, final int additionalSkill) {
        boolean update = false;
        for (int d = 0; d < initialDepth; d++) {
            if (beam[d].isEmpty()) continue;
            final RootedTree<DetonationTreeNode<T>> currentTreeNode = beam[d].poll();

            boolean findBigDetonation = false;

            final long additionalGarbageThisTurn;
            final int additionalSkillThisTurn;
            if (addGarbageTurn == d) {
                additionalGarbageThisTurn = additionalGarbage;
                additionalSkillThisTurn = additionalSkill;
            } else {
                additionalGarbageThisTurn = 0;
                additionalSkillThisTurn = 0;
            }
            ParallelSearchUtil.calcChildren(evaluator, hs, d, currentTreeNode, additionalGarbageThisTurn, additionalSkillThisTurn);

            for (final RootedTree<DetonationTreeNode<T>> nextTreeNode : currentTreeNode.getChildren()) {
                final GameNode nextNode = nextTreeNode.get().gameNode;
                if (nextTreeNode.get().isDetonationNode()) {
                    final long garbage = nextNode.getEdge().get().result.attackDamage;
                    if (garbage >= threshold) {
                        findBigDetonation = true;
                    }
                } else {
                    update = true;
                    beam[d + 1].add(nextTreeNode);
                    hs[d + 1].add(nextNode.board.field.longHashCode());
                }
            }

            if (findBigDetonation && d + 1 >= depth) {
                return d + 1;
            }

        }
        if (!update) {
            return -1;
        }
        return initialDepth;
    }

    public static class FixedTimeSearcher<T extends ScoreContainer> implements ISearcher<T> {
        final long timeLimitMS;

        public FixedTimeSearcher(final long timeLimitMS) {
            super();
            this.timeLimitMS = timeLimitMS;
        }

        @Override
        public RootedTree<DetonationTreeNode<T>> search(final Evaluator<T> evaluator, final RootedTree<DetonationTreeNode<T>> root,
                                                        final int minDepth, final int initialDepth, final long threshold) {
            return BeamStackSearch.detonationTree(evaluator, root, minDepth, initialDepth, timeLimitMS, threshold);
        }

        @Override
        public void reset() {

        }
    }

    public static class PropotionalTimeSearcher<T extends ScoreContainer> implements ISearcher<T> {
        final long timeLimitPerTurnMS;

        public PropotionalTimeSearcher(final long timeLimitMS) {
            super();
            this.timeLimitPerTurnMS = timeLimitMS;
        }

        @Override
        public RootedTree<DetonationTreeNode<T>> search(final Evaluator<T> evaluator, final RootedTree<DetonationTreeNode<T>> root,
                                                        final int minDepth, final int initialDepth, final long threshold) {
            return BeamStackSearch.detonationTree(evaluator, root, minDepth, initialDepth, initialDepth * timeLimitPerTurnMS, threshold);
        }

        @Override
        public void reset() {

        }
    }

    public static class ExponentialTimeSearcher<T extends ScoreContainer> implements ISearcher<T> {
        private final long firstTurnTimeLimit;
        private final long minimumTimeLimit;
        private long currentThinkTime;

        public ExponentialTimeSearcher(final long timeLimitMS, final long minimumTimeLimit) {
            super();
            this.firstTurnTimeLimit = timeLimitMS;
            this.minimumTimeLimit = minimumTimeLimit;
            this.currentThinkTime = timeLimitMS;
        }

        @Override
        public RootedTree<DetonationTreeNode<T>> search(final Evaluator<T> evaluator, final RootedTree<DetonationTreeNode<T>> root,
                                                        final int minDepth, final int initialDepth, final long threshold) {
            final int timeLimitMS = (int) Math.max(minimumTimeLimit, currentThinkTime);
            currentThinkTime = timeLimitMS / 2;
            return BeamStackSearch.detonationTree(evaluator, root, minDepth, initialDepth, timeLimitMS, threshold);
        }

        @Override
        public void reset() {
            currentThinkTime = firstTurnTimeLimit;
        }
    }
}
