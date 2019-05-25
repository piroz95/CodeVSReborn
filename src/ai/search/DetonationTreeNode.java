package ai.search;

import ai.evaluator.evaluation.ScoreContainer;

import java.util.Optional;

public class DetonationTreeNode<T extends ScoreContainer> {
    public final GameNode gameNode;
    public Optional<T> evaluation;

    public DetonationTreeNode(final GameNode gameNode) {
        super();
        this.gameNode = gameNode;
        this.evaluation = Optional.empty();
    }

    public DetonationTreeNode(final GameNode gameNode, final T evaluationResult) {
        super();
        this.gameNode = gameNode;
        this.evaluation = Optional.of(evaluationResult);
    }

    //HACK: Searches.detonationTree(~)が発火ノードは評価をしないことを利用している。
    public boolean isDetonationNode() {
        return !evaluation.isPresent() && gameNode.getEdge().isPresent();
    }

    public boolean isExtendNode() {
        return evaluation.isPresent();
    }

    public boolean isRoot() {
        return !evaluation.isPresent() && !gameNode.getEdge().isPresent();
    }

    public void setAsRoot() {
        evaluation = Optional.empty();
        gameNode.setAsRoot();
    }

    /**
     * @return 発火ノードならば火力（おじゃま量）、そうでなければ0を返す。
     */
    public long garbageSentOrZero() {
        if (!gameNode.getEdge().isPresent()) {
            return 0;
        }
        return gameNode.getEdge().get().result.attackDamage;
    }

    /**
     * @return 伸ばしノードならば点数、そうでなければ-INF。
     */
    public double boardScoreOrNeginf() {
        if (!isExtendNode()) {
            return Double.NEGATIVE_INFINITY;
        }
        return evaluation.get().getScore();
    }

    public String toString() {
        if (gameNode.getEdge().isPresent()) {
            return String.valueOf(gameNode.getEdge().get().result.attackDamage);
        }
        return "-";
    }

}
