package ai.evaluator;

import ai.evaluator.evaluation.ScoreContainer;
import ai.evaluator.evaluation.ScoreContainerImpl;
import ai.search.GameNode;
import game.Field;
import game.GameConstants;
import game.GameUtil;

public class ExplosionEvaluator implements Evaluator<ScoreContainer> {

	@Override
    public ScoreContainer evaluate(final GameNode gameState) {
        final Field f = gameState.board.field;
		int colorBlocks = 0;
		int five = 0;
		int nextToFive = 0;
		
		for(int i=0;i<GameConstants.FIELD_HEIGHT;i++) {
			for(int j=0;j<GameConstants.FIELD_WIDTH;j++) {
				if (GameUtil.isColorBlock(f.get(i, j))) {
					colorBlocks++;
					if (f.get(i, j) == 5) {
						five++;
					}else {
						LOOP: for(int ni=i-1;ni<=i+1;ni++) {
							for(int nj=j-1;nj<=j+1;nj++) {
								if (ni == 0 && nj == 0) continue;
								if (!GameUtil.isInsideField(ni, nj)) continue;
								if (f.get(ni, nj) == 5) {
									nextToFive++;
									break LOOP;
								}
							}
						}
					}
				}
			}
		}

        final int skill = Math.min(gameState.board.skill, 93); //スキルゲージを溢れさせないように
		
		double score = 0;
		
		score += 0.1 * (five + nextToFive);
		score += 0.01 * colorBlocks;
		score += skill;
		score += 5 * five; //|5を2つ消す減点| > |スキルゲージ上昇の加点| にしたいので重くする。

		return new ScoreContainerImpl(score);
	}

}
