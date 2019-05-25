package ai.evaluator.evaluation;

import ai.chainfinder.Chain;

import java.util.Optional;

public interface ChainContainer extends ScoreContainer {
    Optional<Chain> getChain();
}
