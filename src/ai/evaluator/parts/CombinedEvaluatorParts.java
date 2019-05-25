package ai.evaluator.parts;

import ai.chainfinder.Chain;
import ai.search.GameNode;

public class CombinedEvaluatorParts implements EvaluatorParts {
    private final EvaluatorParts evaluator1;
    private final EvaluatorParts evaluator2;
    private final double weight1;
    private final double weight2;
    private final double offset;

    public CombinedEvaluatorParts(final EvaluatorParts evaluator1, final double weight1, final EvaluatorParts evaluator2, final double weight2, final double offset) {
		super();
		this.evaluator1 = evaluator1;
		this.weight1 = weight1;
		this.evaluator2 = evaluator2;
		this.weight2 = weight2;
		this.offset = offset;
	}
	@Override
    public double evaluate(final GameNode gameState, final Chain chain) {
		return evaluator1.evaluate(gameState, chain) * weight1 + evaluator2.evaluate(gameState, chain) * weight2 + offset;
	}
	
}
