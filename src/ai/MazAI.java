package ai;

import ai.brain.Brains;
import ai.brain.IBrain;
import ai.evaluator.Evaluator;
import ai.evaluator.Evaluators;
import ai.search.ne.LPUtil;
import common.Parameters;
import game.*;
import util.*;
import util.logger.ErrLogger;
import util.logger.FileAndErrLogger;
import util.logger.ILogger;
import util.logger.NullLogger;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MazAI {
    //IO
    private static final AsyncScanner asyncScanner = new AsyncScanner(new FastScanner());
    public static ILogger logger;
    public static String timeString;

    //Game
    private static GameSettings game;

    //Concurrent?
    public static final ThreadLocal<NaiveMutableExtendedField> mutableBoard = ThreadLocal.withInitial(() -> new NaiveMutableExtendedField());

    private static IBrain brain;
    private static Board prediction;

    public static void main(final String[] args) {
        System.setProperty("line.separator", "\n");

        Parameters.init();
        //ひどい
        if (!Parameters.setFromFile(Paths.get(".")) &&
                !Parameters.setFromFile(FileUtil.classPath()) &&
                Parameters.CHECK_INI_LOADED) {
            throw new RuntimeException("ini required");
        }
        Parameters.setFromArgs(args);

        configureLogger();

        final String aiName = Parameters.AI_NAME + "_" + getBuildID();
        System.out.println(aiName);

        logger.println(aiName);
        logger.println(Parameters.parametersString());
        if (!Util.runningOn64bitJava()) {
            logger.println("Warning: May be running on 32bit Java");
        }

        if (Parameters.CHECK_LP_SOLVE_INSTALLED) {
            try {
                LPUtil.checkLPSolveInstalled(); //提出先でおかしい感じがするので
            } catch (final UnsatisfiedLinkError e) {
                e.printStackTrace();
                System.err.println(System.getProperty("java.library.path"));
                System.exit(-1);
            }
        }

        try {
            aiInit();
            //noinspection InfiniteLoopStatement
            while (true) {
                aiMain();
            }
        } catch (final RuntimeException e) {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            try {
                final Path wdir = Parameters.WORKING_DIRECTORY;
                final Path logdir = Parameters.LOG_DIRECTORY;
                final PrintWriter pw2 = new PrintWriter(wdir.resolve(logdir).resolve(timeString + "_ERROR.log").toFile());
                pw2.println(sw);
                pw2.flush();
                pw2.close();
            } catch (final FileNotFoundException e2) {
                //do nothing
            }

            logger.close();
            for (int i = 0; i < 10; i++) {
                System.out.println("-1 -1");
            }
            System.out.println("PROGRAM CRASHED");
        }

    }

    private static int getBuildID() {
        try (final Scanner sc = new Scanner(Parameters.WORKING_DIRECTORY.resolve("version.txt").toFile())) {
            while (sc.hasNextLine()) {
                final String s = sc.nextLine();
                if (s.startsWith("build.number")) {
                    return Integer.parseInt(s.split("=")[1]);
                }
            }
        } catch (final Exception e) {
            //silent
        }
        return 0;
    }

    private static void configureLogger() {
        if (Parameters.LOG_LEVEL <= 0) {
            logger = new NullLogger();
        } else if (Parameters.LOG_LEVEL == 1) {
            logger = new ErrLogger();
        } else {
            timeString = FileUtil.currentTimeString();
            try {
                final Path wdir = Parameters.WORKING_DIRECTORY;
                final Path logdir = Parameters.LOG_DIRECTORY;
                logger = new FileAndErrLogger(wdir.resolve(logdir).resolve(timeString + ".log").toFile());
            } catch (final FileNotFoundException e) {
                logger = new ErrLogger();
                logger.println("ERROR: failed to log to file");
            }
        }
    }

    private static void aiInit() {
        final Evaluator evaluator = Evaluators.getEvaluator(Parameters.EVALUATOR);
        brain = Brains.getBrain(Parameters.BRAIN, evaluator);
        game = new GameSettings(asyncScanner.scanner);
    }

    private static void aiMain() {
        final TurnInput input = getTurnInput();
        logger.println("#Turn " + input.turn);

        final long startTimeNS = System.nanoTime();

        if (prediction != null) {
            if (!input.myBoard.field.equals(prediction.field)) {
                logger.println("EXPECT:");
                logger.println(prediction.field.toString());
                logger.println("ACTUAL:");
                logger.println(input.myBoard.field.toString());
            }
        }

        final Action action = brain.nextAction(input);

        System.out.println(action.toOutputString());

        final long endTimeNS = System.nanoTime();
        if (Parameters.EVIL_DEBUG) {
            System.err.println((endTimeNS - startTimeNS) / 1000000 + " ms");
            if (input.turn >= 5) {
                System.out.println("-1 -1"); //意図的に落ちて提出失敗する
            }
        } else {
            logger.println((endTimeNS - startTimeNS) / 1000000 + " ms");
        }

        final Pair<Optional<Board>, TurnResult> result = input.myBoard.simulate(action);
        if (result.second.attackDamage > 0) {
            logger.println("Attack:" + result.second.attackDamage);
        }

        prediction = result.first.orElse(null); //死ぬ手ならnullになろうが関係ないはず
    }

    private static TurnInput getTurnInput() {
        final TurnInput input;
        if (Parameters.SEARCH_WHILE_WAITING) {
            final Future<TurnInput> inputFuture = asyncScanner.asyncRead((sc) -> TurnInput.parse(game, sc));
            brain.waitingInput(inputFuture);
            try {
                input = inputFuture.get();
            } catch (final InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        } else {
            input = TurnInput.parse(game, asyncScanner.scanner);
        }
        return input;
    }

}
