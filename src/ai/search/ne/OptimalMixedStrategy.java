package ai.search.ne;

import ai.evaluator.evaluation.ScoreContainer;
import ai.search.RootedTree;
import game.Action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class OptimalMixedStrategy {
    public static <E extends ScoreContainer, A extends ActionContainer, B> Strategy optimalMixedStrategy(final RootedTree<A> myTree, final RootedTree<B> enemyTree, final Predictor<E, A, B> predictor) {
        final ArrayList<RootedTree<A>> children1 = myTree.getChildren();
        final ArrayList<RootedTree<B>> children2 = enemyTree.getChildren();
        final int n = children1.size();
        final int m = children2.size();
        final double[][] rewardMatrix = new double[n][m];
        for (int i = 0; i < n; i++) {
            final RootedTree<A> v1 = children1.get(i);
            for (int j = 0; j < m; j++) {
                final RootedTree<B> v2 = children2.get(j);

                final Optional<Double> prediction = predictor.predict(v1.get(), v2.get());
                rewardMatrix[i][j] = prediction.orElseGet(() -> optimalMixedStrategy(v1, v2, predictor).expectedReward);
            }
        }

        final double[] maxmin = LPUtil.maxmin(rewardMatrix);
        final HashMap<Action, Double> strategy = new HashMap<>();
        for (int i = 0; i < n; i++) {
            strategy.put(children1.get(i).get().getLastAction(), maxmin[i]);
        }
        return new Strategy(strategy, maxmin[n]);
    }

    public static class Strategy {
        public final HashMap<Action, Double> strategy;
        public final double expectedReward;

        private Strategy(final HashMap<Action, Double> strategy, final double expectedReward) {
            super();
            this.strategy = strategy;
            this.expectedReward = expectedReward;
        }
    }
}
