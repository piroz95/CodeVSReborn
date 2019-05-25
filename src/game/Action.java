package game;

import util.Util;

import java.util.ArrayList;
import java.util.List;

public class Action {
	private static final Action[][] DROPS = enumerateDrops();
	private static final Action SKILL = new Action(-1,-1);
	
	//pos<0のときスキル
	public final int pos, rotation;

    private Action(final int pos, final int rotation) {
		this.pos = pos;
		this.rotation = rotation;
	}
	
	private static Action[][] enumerateDrops() {
        final int w = GameConstants.FIELD_WIDTH - GameConstants.PACK_SIZE + 1;
        final Action[][] drops = new Action[w][4];
		for(int i=0;i<w;i++) {
			for(int j=0;j<4;j++) {
				drops[i][j] = new Action(i,j);
			}
		}
		return drops;
	}

    public static Action drop(final int pos, final int rotation) {
		return DROPS[pos][rotation];
	}

	private static Action skill() {
		return SKILL;
	}
	
	//Lightweightにしてもいい気もするし、誤差な気もする
    public static List<Action> validActions(final boolean skill) {
		final List<Action> al = new ArrayList<>();
		for(int rotation=0;rotation<4;rotation++) {
			for(int pos=0;pos<GameConstants.FIELD_WIDTH-GameConstants.PACK_SIZE+1;pos++) {
				al.add(drop(pos, rotation));
			}
		}
		if (skill) {
			al.add(skill());
		}
		return al;
	}

	public boolean isSkill() {
		return pos < 0;
	}
	public static Action randomDrop() {
        final int pos = Util.randInt(0, GameConstants.FIELD_WIDTH - GameConstants.PACK_SIZE);
        final int rotation = Util.randInt(0, 3);
		return drop(pos,rotation);
	}
	
	public String toOutputString() {
		if (isSkill()) {
			return "S";
		}else {
			return pos + " " + rotation;
		}
	}
	
	public String toString() {
		if (isSkill()) {
			return "S";
		}else {
			return "(" + pos + "," + rotation + ")";
		}
	}

    public boolean equals(final Action a) {
		if (isSkill() && a.isSkill()) {
			return true;
		}
		return pos == a.pos && rotation == a.rotation;
	}
}
