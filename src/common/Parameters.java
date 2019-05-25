package common;

import util.FileUtil;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

@SuppressWarnings("CanBeFinal")
public class Parameters {

	//General
	@Parameter("ainame")
    public static String AI_NAME = "Glico";

	//Evaluator
	@Parameter("brain")
    public static String BRAIN = "TSpineplus";
	@Parameter("dcfdepth")
	public static int DELETE_CHAIN_FINDER_EXTRADEPTH = 0;
	@Parameter("dcfh")
	public static int DELETE_CHAIN_FINDER_HEIGHT = 3;
	@Parameter("evaluator")
    public static String EVALUATOR = "ml2";
	@Parameter("weight")
    public static double ALPHA = 0.5;
	@Parameter("mlf")
    public static Path ML_PARAM_FILE = Paths.get("fast0420-4.txt");
	@Parameter("predictor")
    public static int PREDICTOR_VERSION = 32;

	//Debug
	@Parameter("wdir")
	public static Path WORKING_DIRECTORY = FileUtil.classPath();
	@Parameter("log")
	public static int LOG_LEVEL = 0;
	@Parameter("logdir")
	public static Path LOG_DIRECTORY = Paths.get(".");
	@Parameter("assert")
	public static boolean ENABLE_ASSERT = true;
	@Parameter("evil")
	public static boolean EVIL_DEBUG = false;
	@Parameter("lp")
	public static boolean CHECK_LP_SOLVE_INSTALLED = true;
	@Parameter("ini")
    public static boolean CHECK_INI_LOADED = false;

	//Search Parameter
	@Parameter("thinktime")
    public static long DETONATION_SEARCH_THINKTIME_MS = 10000L;
	@Parameter("thinktimept")
	public static long DETONATION_SEARCH_THINKTIME_PER_TURN_MS = 250L;
	@Parameter("thinktimee")
	public static long DETONATION_SEARCH_THINKTIME_ENEMY_MS = 500L;
	@Parameter("thinktimei")
    public static long DETONATION_SEARCH_THINKTIME_INSTANT = 500L;
	@Parameter("depth")
	public static int DETONATION_SEARCH_DEPTH = 20;
	@Parameter("depthe")
    public static int DETONATION_SEARCH_DEPTH_ENEMY = 4;
	@Parameter("width")
	public static int DETONATION_SEARCH_WIDTH = 1000;
	@Parameter("threshold")
    public static int DAMAGE_THRESHOLD = 45;
	@Parameter("thresholds")
    public static int DETONATION_SEARCH_THRESHOLD = 105;
	@Parameter("thresholde")
	public static int DAMAGE_THRESHOLD_ENEMY = 30;
	@Parameter("thorn")
    public static boolean THORN = true;
	@Parameter("async")
    public static boolean SEARCH_WHILE_WAITING = true;
	@Parameter("smart")
    public static boolean USE_SMART_AGGRO = true;

	@Parameter("thread")
	public static int NTHREADS = 1;

	@Parameter("random")
	public static int RANDOM_BEGGINING = 0;
	@Parameter("tengen")
    public static boolean TENGEN = true;

	private static Map<String, Field> map;

	//パラメータの優先順位: 引数での指定＞ma2AI.iniでの指定＞デフォルト値
	@Deprecated
	public static void set(final String[] args) {
		setDefaults();
		setFromFile(Parameters.WORKING_DIRECTORY);
		setFromArgs(args);
	}

	public static void init() {
		setDefaults();
	}

	public static void setFromArgs(final String[] args) {
		for (final String s : args) {
			setOption(s);
		}
	}

	public static boolean setFromFile(final Path currentPath) {
		try (final Scanner sc = new Scanner(currentPath.resolve("ma2ai.ini").toFile())) {
			while(sc.hasNext()) {
				setOption(sc.next());
			}
			return true;
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(".ini file not found");
			return false;
		}
	}

	/* リフレクションを用いて宣言されたParameter型の変数すべてを、mapなどに追加する。
	 * このハックにより変数の宣言とmapへの登録を2箇所に書かないで済む。
	 */
	private static void setDefaults() {
		map = new HashMap<>();

		final Class<Parameters> c = Parameters.class;
		final Field[] fields = c.getFields();
		for (final Field f : fields) {
			final Parameter annotation = f.getAnnotation(Parameter.class);
			if (annotation == null) continue;
			map.put(annotation.value(), f);
		}
	}

	private static void setOption(final String s) {
		final int idx = s.indexOf('=');
		final String key;
		final String valueString;
		if (idx == -1) {
			System.err.println("Need value:" + s);
			return;
		}
		key = s.substring(0, idx);
		valueString = s.substring(idx + 1);

		final Field parameter = map.get(key);
		if (parameter == null) {
			System.err.println("Unknown option:" + s);
			return;
		}

		final Object value;
		try {
			final Class<?> type = parameter.getType();
			if (type.equals(boolean.class) || type.equals(Boolean.class)) {
				value = Boolean.parseBoolean(valueString);
			} else if (type.equals(int.class) || type.equals(Integer.class)) {
				value = Integer.parseInt(valueString);
			} else if (type.equals(long.class) || type.equals(Long.class)) {
				value = Long.parseLong(valueString);
			} else if (type.equals(double.class) || type.equals(Double.class)) {
				value = Double.parseDouble(valueString);
			} else if (type.equals(String.class)) {
				value = valueString;
			} else if (type.equals(Path.class)) {
				value = Paths.get(valueString);
			} else {
				System.err.println("Unknown Class " + type.getName() + " of " + parameter.getName());
				return;
			}
		} catch (final RuntimeException e) {
			System.err.println("Parse failed:" + s);
			return;
		}

		try {
			parameter.set(null, value);
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static String parametersString() {
		return map.entrySet().stream().map(e -> {
			try {
				return e.getKey() + "=" + e.getValue().get(null);
			} catch (IllegalAccessException ex) {
				ex.printStackTrace();
				return "???ERR???";
			}
		}).collect(Collectors.joining(",", "[", "]"));
	}
}
