package ai.search;

public class GamePath {
    private final long garbageSentSum;
    private final long skillReduceSum;

    private GamePath(final long garbageSentSum, final long skillReduceSum) {
        this.garbageSentSum = garbageSentSum;
        this.skillReduceSum = skillReduceSum;
    }

    private static final GamePath IDENTITY = new GamePath(0, 0);

    public static GamePath identity() {
        return IDENTITY;
    }

    public GamePath add(final long garbageSent, final long skillReduce) {
        return new GamePath(this.garbageSentSum + garbageSent, this.skillReduceSum + skillReduce);
    }
}
