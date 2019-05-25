package game;

import util.FastScanner;
import util.Util;

public class Pack {
    private static final int N = GameConstants.PACK_SIZE;
    private final int[][] block;

    private Pack(final int[][] block) {
        this.block = block;
    }

    public static Pack parse(final FastScanner sc) {
        final int[][] block = new int[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                block[i][j] = sc.nextInt();
            }
        }
        Util.myAssert(sc.next().equals("END"));
        return new Pack(block);
    }

    public int get(final int i, final int j) {
        return block[i][j];
    }

    public Pack rotate90Clockwise() {
        final int[][] b = new int[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                b[i][j] = block[N - 1 - j][i];
            }
        }
        return new Pack(b);
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                sb.append(Util.blockToChar(block[i][j]));
            }
            sb.append('\n');
        }
        return sb.toString();
    }

}
