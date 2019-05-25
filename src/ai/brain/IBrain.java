package ai.brain;

import game.Action;
import game.TurnInput;

import java.util.concurrent.Future;

public interface IBrain {

	/**
	 * 入力待ちの間の動作
	 *
	 * @param inputFuture isDoneになったら入力がある
	 */
	default void waitingInput(final Future<?> inputFuture) {
	}

	/**
	 * 入力を読み取った後すぐの動作
	 *
	 * @param input 入力
	 */
	default void preAction(final TurnInput input) {
	}

	/**
	 * 互換性のため残すやつ。
	 *
	 * @param input
	 * @return
	 */
	default Action nextAction(final TurnInput input) {
		preAction(input);
		final Action action = decideAction(input);
		postAction(input, action);
		return action;
	}

	/**
	 * 本体。前処理と後処理を分離するのは、一部だけ上書きできるようにするため。
	 *
	 * @param input 入力
	 * @return
	 */
	Action decideAction(TurnInput input);

	/**
	 * アクションを決めた後の動作
	 *
	 * @param input 入力
	 */
	default void postAction(final TurnInput input, final Action action) {
	}
}
