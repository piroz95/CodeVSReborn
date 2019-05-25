package ai.evaluator;

import ai.chainfinder.Chain;
import ai.chainfinder.ChainFinder;
import ai.evaluator.evaluation.ChainContainerImpl;
import ai.evaluator.parts.EvaluatorParts;
import ai.search.GameNode;

public class EvaluatorSimple implements Evaluator<ChainContainerImpl> {
    private final ChainFinder chainFinder;
    private final EvaluatorParts mini;

    public EvaluatorSimple(final ChainFinder chainFinder, final EvaluatorParts mini) {
        this.chainFinder = chainFinder;
        this.mini = mini;
    }

    @Override
    public ChainContainerImpl evaluate(final GameNode state) {
        final Chain chain = chainFinder.findChain(state.board.field);
        final double score = mini.evaluate(state, chain);
        return new ChainContainerImpl(score, chain);
    }

}
