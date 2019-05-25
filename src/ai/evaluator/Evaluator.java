package ai.evaluator;

import ai.search.GameNode;

public interface Evaluator<T> {
    T evaluate(GameNode node);
}
