package ai.evaluator;

import ai.evaluator.evaluation.TwinScoreContainer;
import ai.evaluator.evaluation.TwinScoreContainerImpl;
import ai.search.GameNode;

public class InteriorEvaluator2<E extends TwinScoreContainer> implements Evaluator<TwinScoreContainer> {
    private final Evaluator<E> evaluator;
    private final double weight1;
    private final double weight2;

    InteriorEvaluator2(final Evaluator<E> evaluator, final double weight11, final double weight22) {
        this.evaluator = evaluator;
        this.weight1 = weight11;
        this.weight2 = weight22;
    }

    @Override
    public TwinScoreContainer evaluate(final GameNode node) {
        final TwinScoreContainer twin = evaluator.evaluate(node);
        final double score1 = (1 - weight1) * twin.getScore() + weight1 * twin.getSecondaryScore();
        final double score2 = (1 - weight2) * twin.getScore() + weight2 * twin.getSecondaryScore();
        return new TwinScoreContainerImpl(score1, score2, twin.getChain().orElse(null));
    }
}
