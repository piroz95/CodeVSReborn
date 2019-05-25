package game;

import util.FastScanner;

public class TurnInput extends BoardPair {
	public final long myRemainTimeMS;
	@SuppressWarnings("unused")
	public final long enemyRemainTimeMS;

	private TurnInput(final int turn, final Board myBoard, final Board enemyBoard, final long myRemainTimeMS, final long enemyRemainTimeMS) {
		super(turn, myBoard, enemyBoard);
		this.myRemainTimeMS = myRemainTimeMS;
		this.enemyRemainTimeMS = enemyRemainTimeMS;
	}

	public static TurnInput parse(final GameSettings game, final FastScanner scanner) {
        final int turn = scanner.nextInt();
        final long myRemainTimeMS = scanner.nextLong();
        final Board myBoard = Board.parse(game, scanner, turn);
        final long enemyRemainTimeMS = scanner.nextLong();
        final Board enemyBoard = Board.parse(game, scanner, turn);
		return new TurnInput(turn, myBoard, enemyBoard, myRemainTimeMS, enemyRemainTimeMS);
	}

}
