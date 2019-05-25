package ai.evaluator.evaluation;

import ai.chainfinder.Chain;

import java.util.Optional;

public class TwinScoreContainerImpl implements TwinScoreContainer {
    private final double score;
    private final double secondaryScore;
    private final Chain chain;

    public TwinScoreContainerImpl(final double score, final double secondaryScore, final Chain chain) {
        this.score = score;
        this.secondaryScore = secondaryScore;
        this.chain = chain;
    }

    @Override
    public double getSecondaryScore() {
        return secondaryScore;
    }

    @Override
    public Optional<Chain> getChain() {
        return Optional.ofNullable(chain);
    }

    @Override
    public double getScore() {
        return score;
    }
}
