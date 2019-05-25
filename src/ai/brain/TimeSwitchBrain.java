package ai.brain;

import game.Action;
import game.TurnInput;

import java.util.concurrent.Future;

public class TimeSwitchBrain implements IBrain {
    private final IBrain brainFirst;
    private final IBrain brainLast;
    private final long switchTimeLeftMS;
    private boolean useFirst;

    TimeSwitchBrain(final IBrain brainFirst, final IBrain brainLast, final long switchTimeLeftMS) {
        super();
        this.brainFirst = brainFirst;
        this.brainLast = brainLast;
        this.switchTimeLeftMS = switchTimeLeftMS;
    }

    @Override
    public void waitingInput(final Future<?> inputFuture) {
        if (useFirst) {
            brainFirst.waitingInput(inputFuture);
        } else {
            brainLast.waitingInput(inputFuture);
        }
    }

    //NOTE: 切り替わるとき変なバグが起きそうで怖い（状態の変化をあまり細かく考えてない）
    @Override
    public void preAction(final TurnInput input) {
        useFirst = input.myRemainTimeMS >= switchTimeLeftMS;
        if (useFirst) {
            brainFirst.preAction(input);
        } else {
            brainLast.preAction(input);
        }
    }

    @Override
    public Action decideAction(final TurnInput input) {
        if (useFirst) {
            return brainFirst.decideAction(input);
        } else {
            return brainLast.decideAction(input);
        }
    }

    @Override
    public void postAction(final TurnInput input, final Action action) {
        if (useFirst) {
            brainFirst.postAction(input, action);
        } else {
            brainLast.postAction(input, action);
        }
    }
}
