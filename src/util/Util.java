package util;

import common.Parameters;
import game.GameConstants;

import java.util.Arrays;

public class Util {
    public static char blockToChar(final int block) {
        if (block == 0) {
            return '.';
        } else if (block == GameConstants.GARBAGE_ID) {
            return '#';
        } else {
            return (char) ('0' + block);
        }
    }


    public static void myAssert(final boolean truth, final Object... debugs) {
        if (!Parameters.ENABLE_ASSERT) {
            return;
        }
        if (!truth) {
            throw new RuntimeException("Assertion failed:" + Arrays.deepToString(debugs));
        }
    }

    public static int randInt(final int min, final int max) {
        return (int) (Math.random() * (max - min + 1)) + min;
    }

    public static boolean runningOn64bitJava() {
        final String model = System.getProperty("sun.arch.data.model");
        return model != null && model.contains("64");
    }

    public static double sigmoid(final double x) {
        return 1 / (1 + Math.exp(-x));
    }
}
