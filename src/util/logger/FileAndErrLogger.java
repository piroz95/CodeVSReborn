package util.logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class FileAndErrLogger implements ILogger {
	private final PrintWriter pw;

	public FileAndErrLogger(final File file) throws FileNotFoundException {
		pw = new PrintWriter(file);
	}
	@Override
	public void println(final String s) {
		System.err.println(s);
		pw.println(s);
		pw.flush();
	}
	@Override
	public void close() {
		pw.flush();
		pw.close();
	}
	
}
