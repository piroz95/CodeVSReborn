package ai.search.ne;

import ai.evaluator.evaluation.ScoreContainer;
import ai.search.DetonationTreeNode;

import java.util.Optional;

public interface Predictor<E extends ScoreContainer, A, B> {
    //Function<DetonationTreeNode>, E> と BiFunction<E,E,Optional<Double>>

    A myNodeMapper(DetonationTreeNode<E> node);

    B enemyNodeMapper(DetonationTreeNode<E> node);

    //myNodeの勝率を出す。出さないときは子ノードに下る。
    Optional<Double> predict(A myNode, B enemyNode);
}
