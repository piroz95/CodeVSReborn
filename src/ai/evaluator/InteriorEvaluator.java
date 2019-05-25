package ai.evaluator;

import ai.evaluator.evaluation.ChainContainer;
import ai.evaluator.evaluation.ChainContainerImpl;
import ai.evaluator.evaluation.TwinScoreContainer;
import ai.search.GameNode;

public class InteriorEvaluator<E extends TwinScoreContainer> implements Evaluator<ChainContainer> {
    private final Evaluator<E> evaluator;
    private final double weightSecond;

    InteriorEvaluator(final Evaluator<E> evaluator, final double weightSecond) {
        this.evaluator = evaluator;
        this.weightSecond = weightSecond;
    }

    @Override
    public ChainContainer evaluate(final GameNode node) {
        final TwinScoreContainer twin = evaluator.evaluate(node);
        final double score = (1 - weightSecond) * twin.getScore() + weightSecond * twin.getSecondaryScore();
        return new ChainContainerImpl(score, twin.getChain().orElse(null));
    }
}
