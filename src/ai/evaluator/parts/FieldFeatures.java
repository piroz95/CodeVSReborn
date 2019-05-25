package ai.evaluator.parts;

import ai.search.GameNode;
import game.Field;
import game.GameConstants;
import game.GameUtil;

class FieldFeatures {
    public int colorBlock, garbage;
    public final int[] height = new int[GameConstants.FIELD_WIDTH];
    public final int[] heightDiff = new int[GameConstants.FIELD_WIDTH - 1];
    public int keima, daiKeima, jump;

    public FieldFeatures(final GameNode gameState) {
        final Field f = gameState.board.field;
        //ブロックのカウント系
        for (int j = 0; j < GameConstants.FIELD_WIDTH; j++) {
            for (int i = GameConstants.FIELD_HEIGHT - 1; i >= 0; i--) {
                if (f.get(i, j) == 0) {
                    break;
                }
                height[j]++;
                final int b = f.get(i, j);
                if (b == GameConstants.GARBAGE_ID) {
                    garbage++;
                } else {
                    colorBlock++;
                }
            }
        }
        for (int i = 0; i < GameConstants.FIELD_WIDTH - 1; i++) {
            heightDiff[i] = Math.abs(height[i] - height[i + 1]);
        }

        //テンプレ積み系
        for (int i = 2; i < GameConstants.FIELD_HEIGHT; i++) {
            for (int j = 0; j < GameConstants.FIELD_WIDTH; j++) {
                keima += check(f, i, j, -2, 1);
                keima += check(f, i, j, -2, -1);
                daiKeima += check(f, i, j, -3, 1);
                daiKeima += check(f, i, j, -3, -1);
                jump += check(f, i, j, -2, 0);
            }
        }
    }

    private static int check(final Field f, final int i, final int j, final int di, final int dj) {
        final int ni = i + di;
        final int nj = j + dj;
        if (!GameUtil.isInsideField(ni, nj)) {
            return 0;
        }
        if (f.get(i, j) + f.get(ni, nj) == GameConstants.ELIMINATION_SUM) {
            return 1;
        }
        return 0;
    }
}
