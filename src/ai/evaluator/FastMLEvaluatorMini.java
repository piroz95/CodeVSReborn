package ai.evaluator;

import ai.chainfinder.Chain;
import ai.chainfinder.ChainFinder;
import ai.evaluator.evaluation.TwinScoreContainer;
import ai.evaluator.evaluation.TwinScoreContainerImpl;
import ai.feature.FeatureExtractor;
import ai.search.GameNode;

import java.nio.file.Path;

public class FastMLEvaluatorMini implements Evaluator<TwinScoreContainer> {
	private final MLEData data;
	private final FeatureExtractor fe;
	private final ChainFinder cf;

    FastMLEvaluatorMini(final ChainFinder cf, final FeatureExtractor fe, final Path path) {
		this.data = new MLEData(path);
		this.cf = cf;
		this.fe = fe;
	}

	@Override
    public TwinScoreContainer evaluate(final GameNode state) {
        final Chain chain = cf.findChain(state.board.field);
        final double[] f = fe.extract(state, chain);
        final double turnShallow = 16 - f[data.d - 1];
        final double turnDeep = data.dot(f);
		return new TwinScoreContainerImpl(-turnShallow, -turnDeep, chain);
	}

}
