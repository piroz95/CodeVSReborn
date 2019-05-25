package ai.brain;

import ai.MazAI;
import ai.chainfinder.ChainFinder;
import ai.evaluator.Evaluator;
import ai.evaluator.Evaluators;
import ai.evaluator.ExplosionEvaluator;
import ai.evaluator.evaluation.ChainContainer;
import ai.search.ne.GlicoNode2;
import ai.search.ne.GlicoPredictor2;
import ai.search.ne.Predictor;
import ai.searcher.BeamSearch;
import ai.searcher.BeamStackSearch;
import common.Parameters;

//TODO: 型がひどい
public class Brains {
    private static Predictor getPredictor(final ChainFinder cf, final Evaluator ev) {
        if (Parameters.PREDICTOR_VERSION >= 32) {
            return new GlicoPredictor2(cf, ev, Parameters.WORKING_DIRECTORY, "070");
        } else {
            throw new RuntimeException("Deleted predictor version");
        }
    }

    public static IBrain getBrain(final String brainName, final Evaluator evaluator) {
        final IBrain brain;
        if (brainName.startsWith("TS")) {
            brain = new TimeSwitchBrain(getBrain(brainName.substring(2), evaluator),
                    new FalconBrain(new ExplosionEvaluator(), new BeamStackSearch.FixedTimeSearcher(400), 5, 50), 20_000);
        } else {
            brain = getBrain2(brainName, evaluator);
        }
        return new FixedBegginingBrain(brain, Parameters.RANDOM_BEGGINING, Parameters.TENGEN);
    }

    private static IBrain getBrain2(final String brainName, final Evaluator evaluator) {
        IBrain brain = new FalconBrain(evaluator,
                new BeamStackSearch.PropotionalTimeSearcher(Parameters.DETONATION_SEARCH_THINKTIME_PER_TURN_MS),
                Parameters.DETONATION_SEARCH_DEPTH,
                Parameters.DAMAGE_THRESHOLD);
        switch (brainName) {
            case "default":
            case "aggro":
            case "saggro2":
                break;
            case "saggro":
                brain = new FalconBrain(evaluator,
                        new BeamSearch.Searcher(Parameters.DETONATION_SEARCH_WIDTH),
                        Parameters.DETONATION_SEARCH_DEPTH,
                        Parameters.DAMAGE_THRESHOLD);
                break;
            case "aggro3":
            case "aggro6":
                brain = new FalconBrain<>(evaluator,
                        new BeamStackSearch.ExponentialTimeSearcher<>(Parameters.DETONATION_SEARCH_THINKTIME_MS, 100),
                        Parameters.DETONATION_SEARCH_DEPTH,
                        Parameters.DAMAGE_THRESHOLD);
                break;
            case "pine":
            case "pine0":
            case "pine2":
            case "pineml":
            case "pineml2":
                brain = new PineappleBrain<ChainContainer, GlicoNode2, GlicoNode2>(evaluator,
                        getPredictor(Evaluators.getChainFinder(), Evaluators.lightEvaluator()),
                        Parameters.DETONATION_SEARCH_DEPTH,
                        new BeamStackSearch.PropotionalTimeSearcher<>(Parameters.DETONATION_SEARCH_THINKTIME_PER_TURN_MS),
                        Parameters.DAMAGE_THRESHOLD,
                        (Evaluator<ChainContainer>) Evaluators.lightEvaluator(),
                        Parameters.DETONATION_SEARCH_DEPTH_ENEMY,
                        Parameters.DAMAGE_THRESHOLD_ENEMY,
                        Parameters.DETONATION_SEARCH_THINKTIME_ENEMY_MS,
                        Parameters.THORN,
                        Parameters.DETONATION_SEARCH_THRESHOLD);
                break;
            case "pineplus":
                brain = new PineappleBrain<ChainContainer, GlicoNode2, GlicoNode2>(evaluator,
                        getPredictor(Evaluators.getChainFinder(), Evaluators.lightEvaluator()),
                        Parameters.DETONATION_SEARCH_DEPTH,
                        new BeamStackSearch.ExponentialTimeSearcher<>(Parameters.DETONATION_SEARCH_THINKTIME_MS, 100),
                        Parameters.DAMAGE_THRESHOLD,
                        (Evaluator<ChainContainer>) Evaluators.lightEvaluator(),
                        Parameters.DETONATION_SEARCH_DEPTH_ENEMY,
                        Parameters.DAMAGE_THRESHOLD_ENEMY,
                        Parameters.DETONATION_SEARCH_THINKTIME_ENEMY_MS,
                        Parameters.THORN,
                        Parameters.DETONATION_SEARCH_THRESHOLD);
                break;
            case "tnt":
            case "sweeper":
            case "eve":
            case "sailfish":
            case "twinbeam":
            case "aggro5":
            case "aggro4":
                MazAI.logger.println("Deleted brain:" + brainName);
                break;
            default:
                MazAI.logger.println("Unknown brain:" + brainName);
                break;
        }
        return brain;
    }

}
