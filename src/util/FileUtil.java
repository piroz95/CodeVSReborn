package util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileUtil {
	//HACK: これはおそらくJarの位置を判定するハック。IntelliJ上の実行だとうまくいかない。
	public static Path classPath() {
		return Paths.get(System.getProperty("java.class.path").split(";")[0]).toAbsolutePath().getParent();
	}
	
	public static String currentTimeString() {
        final LocalDateTime now = LocalDateTime.now();
		return now.format(DateTimeFormatter.ofPattern("MMdd_HHmmss_SSS"));
	}

}
