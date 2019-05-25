package ai.search.ne;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class LPUtil {
    public static void checkLPSolveInstalled() {
        try {
            final LpSolve lp = LpSolve.makeLp(4, 4);
            lp.setVerbose(1);
            lp.setUnbounded(4);
            lp.setObjFn(new double[]{0, 0, 0, 0, 1});
            lp.setAddRowmode(true);
            lp.addConstraint(new double[]{0, 0, 1, -2, -1}, LpSolve.GE, 0);
            lp.addConstraint(new double[]{0, -1, 0, 2, -1}, LpSolve.GE, 0);
            lp.addConstraint(new double[]{0, 2, -2, 0, -1}, LpSolve.GE, 0);
            lp.addConstraint(new double[]{0, 1, 1, 1, 0}, LpSolve.EQ, 1);
            lp.setAddRowmode(false);
            lp.setMaxim();

            lp.solve();
            final double reward = lp.getObjective();
            if (reward != 0) {
                throw new RuntimeException("should be 0");
            }
            lp.deleteLp();
        } catch (final LpSolveException e) {
            e.printStackTrace();
        }
    }

    public static double[] maxmin(final double[][] rewardMatrix) {
        final int n = rewardMatrix.length;
        final int m = rewardMatrix[0].length;
        final double[] res = new double[n + 1];
        if (n == 1) {
            double min = Double.POSITIVE_INFINITY;
            for (int j = 0; j < m; j++) {
                min = Math.min(min, rewardMatrix[0][j]);
            }
            res[0] = 1;
            res[1] = min;
        } else if (m == 1) {
            int maxI = 0;
            double max = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < n; i++) {
                if (max < rewardMatrix[i][0]) {
                    max = rewardMatrix[i][0];
                    maxI = i;
                }
            }
            res[maxI] = 1;
            res[n] = max;
        } else if (n == 2 && m == 2) {
            final double a = rewardMatrix[0][0];
            final double b = rewardMatrix[1][0];
            final double c = rewardMatrix[0][1];
            final double d = rewardMatrix[1][1];
            if (a - b >= 0 && c - d >= 0) {
                res[0] = 1;
                res[n] = Math.min(a, c);
            } else if (a - b <= 0 && c - d <= 0) {
                res[1] = 1;
                res[n] = Math.min(b, d);
            } else {
                double x = (d - b) / (a - b - c + d);
                if (x < 0)
                    x = 0;
                if (x > 1)
                    x = 1;
                res[0] = x;
                res[1] = 1 - x;
                res[n] = Math.min(b + (a - b) * x, d + (c - d) * x);
            }
        } else {
            try {
                final LpSolve lp = LpSolve.makeLp(m + 1, n + 1);
                lp.setVerbose(1);
                lp.setUnbounded(n + 1);
                final double[] objfn = new double[n + 2];
                objfn[n + 1] = 1;
                lp.setObjFn(objfn);
                lp.setAddRowmode(true);
                final double[] sumConstraint = new double[n + 2];
                for (int i = 1; i <= n; i++) {
                    sumConstraint[i] = 1;
                }
                lp.addConstraint(sumConstraint, LpSolve.EQ, 1);
                for (int i = 0; i < m; i++) {
                    final double[] constraint = new double[n + 2];
                    for (int j = 0; j < n; j++) {
                        constraint[j + 1] = rewardMatrix[j][i];
                    }
                    constraint[n + 1] = -1;
                    lp.addConstraint(constraint, LpSolve.GE, 0);
                }
                lp.setAddRowmode(false);
                lp.setMaxim();

                lp.solve();
                res[n] = lp.getObjective();
                final double[] str = lp.getPtrVariables();
                System.arraycopy(str, 0, res, 0, n);
                lp.deleteLp();
            } catch (final LpSolveException e) {
                throw new RuntimeException(e);
            }
        }
        return res;
    }
}
