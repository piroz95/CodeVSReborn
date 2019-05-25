package game;

import ai.MazAI;
import util.FastScanner;
import util.Pair;
import util.Util;

import java.util.Optional;

public class Board {
	public final Field field;
	public final int turn;
	public final GameSettings game;
	public final long garbage;
	public final int skill;

    public Board(final GameSettings game, final int turn, final Field field, final long garbage, final int skill) {
		super();
		this.game = game;
		this.turn = turn;
		this.field = field;
		this.garbage = garbage;
		this.skill = skill;
        if (turn < 0 || garbage < 0 || skill < 0 || skill > GameConstants.SKILL_MAX) {
            throw new IllegalArgumentException();
        }
	}

    public static Board parse(final GameSettings game, final FastScanner sc, final int turn) {
        final int garbage = sc.nextInt();
        final int skill = sc.nextInt();
		sc.nextLong(); //FIXME: スコアを実装する
        final BitBoardField.BitBoard bb = new BitBoardField.BitBoard();
		for(int i=0;i<GameConstants.FIELD_HEIGHT;i++) {
			for(int j=0;j<GameConstants.FIELD_WIDTH;j++) {
				bb.set(i, j, sc.nextInt());
			}
		}
		Util.myAssert(sc.next().equals("END"));
		return new Board(game, turn, new BitBoardField(bb), garbage, skill);
	}

	public Board add(final long garbage, final int skill) {
        int nextSkill = this.skill + skill;
        if (nextSkill < 0) {
            nextSkill = 0;
        }
        if (nextSkill >= GameConstants.SKILL_MAX) {
            nextSkill = GameConstants.SKILL_MAX;
        }
        return new Board(game, turn, field, this.garbage + garbage, nextSkill);
    }

	/*
	 * このシミュレーションは一人用のため、結果のgarbage,skillは相殺あるいはスキル減少を考慮していない。
	 */
	//TODO: シグネチャが気に入らない
    public Pair<Optional<Board>, TurnResult> simulate(final Action action) {
        final NaiveMutableExtendedField b = MazAI.mutableBoard.get();
		long nextGarbage = garbage;
		int nextSkill = skill;
		
		b.init(field);
		if (nextGarbage >= GameConstants.FIELD_WIDTH) {
			b.putGarbage();
			nextGarbage -= GameConstants.FIELD_WIDTH;
		}
		int blownBlocks = 0;
		if (action.isSkill()) {
			//オジャマを実際に落とす前に爆破シミュレーションするのは一見おかしいが大丈夫なはず
			Util.myAssert(skill >= GameConstants.SKILL_COST, "Skill:" + skill);
			nextSkill = 0;
			blownBlocks = b.explosion();
		}else {
			b.putPack(game.packs[turn][action.rotation], action.pos);
		}
        final int chains = b.simulateChain();
		if (b.isDead()) {
			return new Pair<>(Optional.empty(), new TurnResult(true, 0, 0, 0, 0, 0));
		}


        final long chainScore = GameConstants.CHAIN_SCORE_TABLE[chains];

		long normalChainScore = 0;
		long explosionScore = 0;
		long explosionChainScore = 0;
		if (action.isSkill()) {
			explosionScore = GameUtil.explosionScore(blownBlocks);
			explosionChainScore = chainScore;
		}else {
			normalChainScore = chainScore;
		}
		
		if (chains > 0) {
			nextSkill += GameConstants.SKILL_GAIN;
			if (nextSkill > GameConstants.SKILL_MAX) {
				nextSkill = GameConstants.SKILL_MAX;
			}
		}

        //オジャマと相殺
        final long attackDamage = explosionScore / 2 + chainScore / 2;
        final long sousai = Math.min(attackDamage, nextGarbage);
        nextGarbage -= sousai;

		final int skillReduce = chains <= 2 ? 0 : 12 + 2 * chains;

        final Board nextBoard = new Board(game, turn + 1, b.toField(), nextGarbage, nextSkill);
		return new Pair<>(Optional.of(nextBoard), new TurnResult(false, normalChainScore, explosionScore, explosionChainScore, skillReduce, attackDamage));
	}

    public boolean weaklyEquals(final Board b) {
		if (garbage / 10 != b.garbage / 10) {
			return false;
		}
		if (skill / 8 != b.skill / 8) {
			return false;
		}
		if (turn != b.turn) {
			return false;
		}
        return field.equals(b.field);
    }

}
