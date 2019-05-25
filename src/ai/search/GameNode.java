package ai.search;

import game.Action;
import game.Board;
import game.GameConstants;
import game.TurnResult;
import util.Pair;

import java.util.ArrayList;
import java.util.Optional;

/**
 * 1人ゲーのシミュレーション結果を表すクラス。
 */
public class GameNode {
	public final Board board;
	private GameEdge edge;
	private GamePath path;

	private GameNode(final Board board, final GameEdge edge, final GamePath path) {
		super();
		this.board = board;
		this.edge = edge;
		this.path = path;
	}

	public static GameNode root(final Board initialBoard) {
		return new GameNode(initialBoard, null, GamePath.identity());
	}

	public Optional<Action> lastAction() {
		if (!getEdge().isPresent()) {
			return Optional.empty();
		}
		return Optional.of(getEdge().get().action);
	}

	/**
	 * @return 発火ノードならば火力（おじゃま量）、そうでなければ0を返す。
	 */
	public long attackDamage() {
		if (!getEdge().isPresent()) {
			return 0;
		}
		return getEdge().get().result.attackDamage;
	}

	public int turn() {
		return board.turn;
	}

	public int relativeTurn(final GameNode root) {
		return board.turn - root.board.turn;
	}

	public int relativeTurn(final int turn) {
		return board.turn - turn;
	}

	private Optional<GameNode> child(final Action action, final long additionalGarbage, final int additionalSkill) {
        final Board b;
        if (additionalGarbage == 0 && additionalSkill == 0) {
            b = board;
        } else {
            b = board.add(additionalGarbage, additionalSkill);
        }
		final Pair<Optional<Board>, TurnResult> result = b.simulate(action);
		if (result.second.dead) {
			return Optional.empty();
		}
		final Board nextBoard = result.first.get();
		final GameNode nextNode = new GameNode(nextBoard, new GameEdge(action, result.second), path.add(result.second.attackDamage, result.second.skillReduce));
		return Optional.of(nextNode);
	}

    /**
	 * @param skill スキルを"使いたい"かどうか
	 * @return 子ノードのリスト
	 */
	public ArrayList<GameNode> children(final boolean skill, final long additionalGarbage, final int additionalSkill) {
		final ArrayList<GameNode> al = new ArrayList<>();
		if (this.board.turn + 1 >= GameConstants.TURN_MAX) return al;

		final boolean useSkill = skill && this.board.skill >= GameConstants.SKILL_COST;
		for (final Action a : Action.validActions(useSkill)) {
			this.child(a, additionalGarbage, additionalSkill).ifPresent(al::add);
		}

		return al;
	}

	public Optional<GameEdge> getEdge() {
		return Optional.ofNullable(edge);
	}

	public void setAsRoot() {
		edge = null;
		path = GamePath.identity();
	}

}
