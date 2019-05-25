package util.logger;

public class ErrLogger implements ILogger {

    @Override
    public void println(final String s) {
        System.err.println(s);
    }

    @Override
    public void close() {
        System.err.flush();
        System.err.close();
    }

}
