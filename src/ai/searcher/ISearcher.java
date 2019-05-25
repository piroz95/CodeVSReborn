package ai.searcher;

import ai.evaluator.Evaluator;
import ai.evaluator.evaluation.ScoreContainer;
import ai.search.DetonationTreeNode;
import ai.search.RootedTree;

public interface ISearcher<T extends ScoreContainer> {
    /**
     * @param evaluator    評価関数
     * @param root         根。 SearchUtilを使って以前の木を再利用しよう。
     * @param minDepth     最低でも探索する深さ。
     * @param initialDepth 最大でこの深さまで探索する
     * @param threshold    この大きさの連鎖を見つけ、minDepth以上の深さがあれば打ち切る。
     * @return
     */
    RootedTree<DetonationTreeNode<T>> search(Evaluator<T> evaluator, RootedTree<DetonationTreeNode<T>> root, int minDepth, int initialDepth, long threshold);

    void reset();
}
