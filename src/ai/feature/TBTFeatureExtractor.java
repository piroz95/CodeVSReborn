package ai.feature;

import ai.chainfinder.Chain;
import ai.evaluator.parts.EvaluatorParts;
import ai.search.GameNode;
import game.GameConstants;
import game.GameUtil;

public class TBTFeatureExtractor implements FeatureExtractor {
    private final EvaluatorParts e;

    public TBTFeatureExtractor(final EvaluatorParts e) {
        this.e = e;
    }

    @Override
    public double[] extract(final GameNode node, final Chain chain) {
        final int d = 1 + 11 * 11 * 8;
        final double[] f = new double[d];
        f[d - 1] = e.evaluate(node, chain);
        final int h = GameConstants.FIELD_HEIGHT;
        final int w = GameConstants.FIELD_WIDTH;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int b = node.board.field.get(i, j);
                if (b == 11) b = 10;
                for (int di = -2; di <= 0; di++) {
                    for (int dj = -2; dj <= 0; dj++) {
                        if (di == 0 && dj == 0) continue;
                        final int offsetId = (di + 2) * 3 + (dj + 2);
                        increment(node, f, b, offsetId, i + di, j + dj);
                        if (di < 0 && dj != 0) {
                            increment(node, f, b, offsetId, i + di, j - dj);
                        }
                    }
                }
            }
        }

        return f;
    }

    private void increment(final GameNode node, final double[] f, final int b, final int offsetId, final int ni, final int nj) {
        if (GameUtil.isInsideField(ni, nj)) {
            int b2 = node.board.field.get(ni, nj);
            if (b2 == 11) b2 = 10;
            final int min = Math.min(b, b2);
            final int max = Math.max(b, b2);
            f[offsetId * 11 * 11 + max * 11 + min]++;
        }
    }

}
