package ai.evaluator.evaluation;

public class ScoreContainerImpl implements ScoreContainer {
    private final double score;

    public ScoreContainerImpl(final double score) {
        this.score = score;
    }

    @Override
    public double getScore() {
        return score;
    }
}
