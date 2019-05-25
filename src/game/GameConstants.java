package game;

/**
 * ゲームの定数。
 * もしラウンド毎にゲームルールが変わるようになったりしたら、名前を変えてGameSettingのメンバにでもしたほうがよさそう
 */
public class GameConstants {
	public static final int FIELD_WIDTH = 10;
	public static final int FIELD_HEIGHT = 16;
	public static final int PACK_SIZE = 2;
	public static final int BLOCK_MAX = 9;
	public static final int ELIMINATION_SUM = 10;
	public static final int TURN_MAX = 500;
	public static final int GARBAGE_ID = ELIMINATION_SUM + 1;
	public static final int SKILL_COST = 80;
	public static final int SKILL_MAX = 100;
	public static final int SKILL_GAIN = 8;

	public static final long[] CHAIN_SCORE_TABLE = makeScoreTable(FIELD_HEIGHT * FIELD_WIDTH / 2 + 5);

    private static long[] makeScoreTable(final int maxChain) {
        final long[] table = new long[maxChain + 1];
		long sum = 0;
		for(int i=1;i<=maxChain;i++) {
			sum += (long) Math.pow(1.3, i);
			table[i] = sum;
		}
		return table;
	}
}
