package util;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

public class AsyncScanner implements Closeable {
    //FIXME: queueに待ちがあってもscannerにアクセスできる。競合の可能性が増える。
    public final FastScanner scanner;
    private final ExecutorService executorService;
    private Future<?> queue = null;

    public AsyncScanner(final FastScanner scanner) {
        this.scanner = scanner;
        this.executorService = Executors.newFixedThreadPool(1);
    }

    public <T> Future<T> asyncRead(final Function<FastScanner, T> function) {
        if (queue != null && !queue.isDone()) {
            throw new IllegalStateException("複数回回呼ばれた");
        }
        final Future<T> f = executorService.submit(() -> function.apply(scanner));
        queue = f;
        return f;
    }

    @Override
    public void close() {
        scanner.close();
        executorService.shutdown();
    }
}
