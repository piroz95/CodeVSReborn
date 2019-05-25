package ai.evaluator.parts;

import ai.chainfinder.Chain;
import ai.search.GameNode;

public class HeirloomEvaluatorParts implements EvaluatorParts {

	//CODE VS for STUDENT の評価関数を参考にしたやつだけど、もう微妙なはず
	//分散2000くらい、平均0になってほしい(昔のだと)
	@Override
    public double evaluate(final GameNode gameState, final Chain chain) {
		double score = 0;

        final FieldFeatures f = new FieldFeatures(gameState);
		
		score -= 30 * f.garbage;
		score += 30 * f.colorBlock;
		
		score -= 40 * f.height[0];
		score -= 20 * f.height[1];
		score -= 10 * f.height[2];
		score -= 10 * f.height[7];
		score -= 20 * f.height[8];
		score -= 40 * f.height[9];
		
		score += 10 * f.heightDiff[1];
		score -= 6 * f.heightDiff[3];
		score -= 10 * f.heightDiff[4];
		score -= 6 * f.heightDiff[5];
		score += 10 * f.heightDiff[7];

		score += 24 * f.keima;
		score += 18 * f.daiKeima;
		score += 20 * f.jump;

		score -= 300 * ChainEvaluatorParts.reverseFunction(chain.score);

		score += 2100;
		
		return score;
	}

}
