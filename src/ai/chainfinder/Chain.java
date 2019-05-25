package ai.chainfinder;

public class Chain {
    final public long score;
    @SuppressWarnings({"unused", "WeakerAccess"})
    final public int posI;
    @SuppressWarnings({"unused", "WeakerAccess"})
    final public int posJ;

    public Chain(final long score, final int posI, final int posJ) {
        super();
        this.score = score;
        this.posI = posI;
        this.posJ = posJ;
    }
}
