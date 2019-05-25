package util;

import java.io.IOException;
import java.io.PrintWriter;

public class ProcessWriter implements AutoCloseable {
    private final Process p;
    private final PrintWriter pw;

    public ProcessWriter(final String... args) {
        final ProcessBuilder pb = new ProcessBuilder(args);
		try {
			p = pb.start();
        } catch (final IOException e) {
			throw new RuntimeException(e);
		}
		pw = new PrintWriter(p.getOutputStream(), false);
	}

    public void println(final Object o) {
		pw.println(o);
	}

// --Commented out by Inspection START (2019/05/06 18:36):
//	public void flush() {
//		pw.flush();
//	}
// --Commented out by Inspection STOP (2019/05/06 18:36)

    public void close() {
		pw.flush();
		pw.close();
		try {
            p.waitFor();
        } catch (final InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
