package util.logger;

public interface ILogger {
    void println(String s);

    @SuppressWarnings("unused")
    default void println(final Object o) {
        println(String.valueOf(o));
    }

    void close();
}
