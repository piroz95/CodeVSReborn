package game;

import util.FastScanner;

/**
 * 今の所パックのみ。
 * 万が一試合ごとにマップが変わるとかあったらここ。
 * 万が一試合ごとにパラメータがちがったらGameConstantがここに来る
 */
public class GameSettings {
	public final Pack[][] packs; //便利なので回転済みのを持っている

	private GameSettings(final Pack[] pack) {
		super();
		this.packs = rotatePacks(pack);
	}

    public GameSettings(final FastScanner sc) {
		this(parsePacks(sc));
	}

    private static Pack[][] rotatePacks(final Pack[] packs) {
        final Pack[][] res = new Pack[GameConstants.TURN_MAX][4];
		for(int i=0;i<GameConstants.TURN_MAX;i++) {
			Pack p = packs[i];
			for(int j=0;j<4;j++) {
				res[i][j] = p;
				p = p.rotate90Clockwise();
			}
		}
		return res;
	}

    private static Pack[] parsePacks(final FastScanner scanner) {
        final Pack[] pack = new Pack[GameConstants.TURN_MAX];
		for(int i=0;i<GameConstants.TURN_MAX;i++) {
			try {
				pack[i] = Pack.parse(scanner);
            } catch (final RuntimeException e) {
				throw new RuntimeException("Exception while parsing " + i + "th element", e);
			}
		}
		return pack;
	}

}
