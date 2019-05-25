package ai.search;

import game.Action;
import game.TurnResult;

public class GameEdge {
    public final Action action;
    public final TurnResult result;

    public GameEdge(final Action action, final TurnResult result) {
        super();
        this.action = action;
        this.result = result;
    }
}
