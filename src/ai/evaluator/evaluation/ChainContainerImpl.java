package ai.evaluator.evaluation;

import ai.chainfinder.Chain;

import java.util.Optional;

public class ChainContainerImpl implements ChainContainer {
    private final double boardScore;
    private final Chain chain;

    public ChainContainerImpl(final double boardScore, final Chain chain) {
        super();
        this.boardScore = boardScore;
        this.chain = chain;
    }

    @Override
    public double getScore() {
        return boardScore;
    }

    @Override
    public Optional<Chain> getChain() {
        return Optional.ofNullable(chain);
    }

    public String toString() {
        return chain.score / 2 + "/" + boardScore;
    }
}
