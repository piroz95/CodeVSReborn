package ai.evaluator;

import ai.MazAI;
import ai.chainfinder.ChainFinder;
import ai.chainfinder.DeleteChainFinder;
import ai.evaluator.parts.ChainEvaluatorParts;
import ai.evaluator.parts.CombinedEvaluatorParts;
import ai.evaluator.parts.HeirloomEvaluatorParts;
import ai.feature.TBTFeatureExtractor;
import common.Parameters;

public class Evaluators {
    private static Evaluator<?> getEvaluator(final String evaluatorName, final ChainFinder cf) {
        Evaluator<?> ev = new EvaluatorSimple(cf, new ChainEvaluatorParts());
        switch (evaluatorName) {
            case "1":
            case "first":
            case "eve":
            case "ml":
            case "heirloomml":
            case "zero":
            case "zero1":
            case "classic":
            case "quick":
            case "yoko":
            case "real":
            case "real2":
            case "quickml":
                throw new RuntimeException("These evaluators are deleted!");
            case "2":
            case "heirloom":
                ev = new EvaluatorSimple(cf, new CombinedEvaluatorParts(new ChainEvaluatorParts(), 1, new HeirloomEvaluatorParts(), Parameters.ALPHA / 2000, 0));
                break;
            case "default":
            case "0":
            case "pure":
                ev = new EvaluatorSimple(cf, new ChainEvaluatorParts());
                break;
            case "explosion":
            case "fexplosion":
                ev = new ExplosionEvaluator();
                break;
            case "ml2":
                ev = new InteriorEvaluator<>(
                        new FastMLEvaluatorMini(
                                new DeleteChainFinder(0),
                                new TBTFeatureExtractor(new ChainEvaluatorParts()),
                                Parameters.WORKING_DIRECTORY.resolve(Parameters.ML_PARAM_FILE)),
                        Parameters.ALPHA);
                break;
            case "ml2t":
                ev = new InteriorEvaluator2<>(
                        new FastMLEvaluatorMini(
                                new DeleteChainFinder(0),
                                new TBTFeatureExtractor(new ChainEvaluatorParts()),
                                Parameters.WORKING_DIRECTORY.resolve(Parameters.ML_PARAM_FILE)),
                        0.1, 1.0);
                break;
            default:
                MazAI.logger.println("Unknown evaluator:" + evaluatorName);
                break;
        }
        return ev;
    }

    public static Evaluator<?> getEvaluator(final String evaluatorName) {
        return getEvaluator(evaluatorName, getChainFinder());
    }

    public static Evaluator<?> lightEvaluator() {
        return getEvaluator("heirloom");
    }

    public static ChainFinder getChainFinder() {
        return new DeleteChainFinder(Parameters.DELETE_CHAIN_FINDER_EXTRADEPTH, Parameters.DELETE_CHAIN_FINDER_HEIGHT);
    }
}
