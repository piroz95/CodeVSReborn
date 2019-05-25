package ai.evaluator.parts;

import ai.chainfinder.Chain;
import ai.search.GameNode;

public class ChainEvaluatorParts implements EvaluatorParts {
	public ChainEvaluatorParts() {
	}
	
	//おじゃま数からおおよそ連鎖数を返す関数。floorを考慮できてないから低めに出るはず。
    public static double reverseFunction(final double garbage) {
		return Math.log(garbage * 0.3 / 1.3 + 1) / Math.log(1.3);
	}

    public double evaluate(final GameNode gameState, final Chain chain) {
        return reverseFunction(chain.score); //NOTE: 本来chain.score / 2を入れるべきじゃない？でも大丈夫そう
    }
}
