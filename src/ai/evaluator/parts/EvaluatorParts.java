package ai.evaluator.parts;

import ai.chainfinder.Chain;
import ai.search.GameNode;

public interface EvaluatorParts {
	double evaluate(GameNode gameState, Chain chain);
}
