package ai.search.ne;

import game.TurnResult;

class AttackDamage {
    final double chainDamageScore;
    final double bombDamageScore;

    private AttackDamage(final double chainDamageScore, final double bombDamageScore) {
        super();
        this.chainDamageScore = chainDamageScore;
        this.bombDamageScore = bombDamageScore;
    }

    static AttackDamage fromGarbageSent(final long chainDamage, final long bombDamage) {
        return new AttackDamage(mapToScore(chainDamage), mapToScore(bombDamage));
    }

    static AttackDamage zero() {
        return new AttackDamage(0, 0);
    }

    static AttackDamage fromTurnResult(final TurnResult result) {
        return fromGarbageSent(result.chainScore / 2, result.explosionScore / 2 + result.explosionChainScore / 2);
    }

    private static double mapToScore(final long garbage) {
        final int twentyChainGarbage = 405;
        return Math.min(twentyChainGarbage, garbage) + (Math.log(Math.max(twentyChainGarbage, garbage)) - Math.log(twentyChainGarbage)) * 1000;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (bombDamageScore > 0) {
            sb.append(String.format("B%.1f", bombDamageScore));
        }
        if (chainDamageScore > 0) {
            sb.append(String.format("C%.1f", chainDamageScore));
        }
        if (sb.length() == 0) {
            sb.append('0');
        }
        return sb.toString();
    }
}
