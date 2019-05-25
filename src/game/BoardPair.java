package game;

public class BoardPair {
    public final int turn;
    public final Board myBoard, enemyBoard;

    BoardPair(final int turn, final Board myBoard, final Board enemyBoard) {
        super();
        this.turn = turn;
        this.myBoard = myBoard;
        this.enemyBoard = enemyBoard;
    }
}
