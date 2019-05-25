package game;

public class TurnResult {
	public final boolean dead;
	public final long chainScore;
	public final long explosionScore;
	public final long explosionChainScore;
	public final int skillReduce;
	public final long attackDamage;

	public TurnResult(final boolean dead, final long chainScore, final long explosionScore, final long explosionChainScore,
					  final int skillReduce, final long attackDamage) {
		super();
		this.dead = dead;
		this.chainScore = chainScore;
		this.explosionScore = explosionScore;
		this.explosionChainScore = explosionChainScore;
		this.skillReduce = skillReduce;
		this.attackDamage = attackDamage;
	}

}
