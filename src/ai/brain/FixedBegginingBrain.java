package ai.brain;

import ai.MazAI;
import game.Action;
import game.TurnInput;

import java.util.concurrent.Future;

public class FixedBegginingBrain implements IBrain {
    private final IBrain brain;
    private final int random;
    private final boolean tengen;

    FixedBegginingBrain(final IBrain brain, final int random, final boolean tengen) {
        this.brain = brain;
        this.random = random;
        this.tengen = tengen;
    }

    @Override
    public void waitingInput(final Future<?> inputFuture) {
        brain.waitingInput(inputFuture);
    }

    @Override
    public void preAction(final TurnInput input) {
        brain.preAction(input);
    }

    @Override
    public Action decideAction(final TurnInput input) {
        if (tengen && input.turn == 0) {
            MazAI.logger.println("TENGEN");
            return Action.drop(4, 0);
        }
        if (input.turn < random) {
            final Action random = Action.randomDrop();
            MazAI.logger.println("RANDOM:" + random.toOutputString());
            return random;
        }
        return brain.decideAction(input);
    }

    @Override
    public void postAction(final TurnInput input, final Action action) {
        brain.postAction(input, action);
    }
}
