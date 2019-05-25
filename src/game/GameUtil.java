package game;

/**
 * ゲームルールに関わるメソッド
 */
public class GameUtil {
    public static boolean isColorBlock(final int b) {
		return 1 <= b && b <= GameConstants.BLOCK_MAX;
	}

    public static boolean isInsideField(final int i, final int j) {
		return 0 <= i && i < GameConstants.FIELD_HEIGHT && 0 <= j && j < GameConstants.FIELD_WIDTH;
	}

    public static long explosionScore(final int blownBlocks) {
		if (blownBlocks <= 0) {
			return 0;
		}
		return (long) (25 * Math.pow(2, (double) blownBlocks / 12D));
	}
}
