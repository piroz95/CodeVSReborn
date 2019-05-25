package ai.feature;

import ai.chainfinder.Chain;
import ai.search.GameNode;

public interface FeatureExtractor {
    double[] extract(GameNode node, Chain chain);
}
