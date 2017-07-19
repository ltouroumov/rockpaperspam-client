package com.tricktrap.rps.utils.async;

import com.tricktrap.rps.utils.fn.Consumer;
import com.tricktrap.rps.utils.fn.Supplier;
import com.tricktrap.rps.utils.fn.Transformer;

import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author ldavid
 * @created 3/23/17
 */
public class AsyncFuture<V> implements Future<V> {

    private static final String TAG = AsyncFuture.class.getSimpleName();
    private boolean isDone = false;
    private V value = null;

    private CallbackList<V> onComplete = new CallbackList<>();

    private Lock valueLock = new ReentrantLock();
    private Condition valueChanged = valueLock.newCondition();

    public static <T> AsyncFuture<T> completed(T value) {
        AsyncFuture<T> future = new AsyncFuture<>();
        future.complete(value);
        return future;
    }

    public static <T> AsyncFuture<T> supplyAsync(Supplier<T> supplier) {
        return supplyAsync(supplier, AsyncService.getInstance());
    }

    public static <T> AsyncFuture<T> supplyAsync(Supplier<T> supplier, Executor executor) {
        AsyncFuture<T> future = new AsyncFuture<T>();
        executor.execute(new FutureSupplier<>(future, supplier));
        return future;
    }

    private AsyncFuture() {
    }

    public AsyncFuture<V> whenComplete(Consumer<V> consumer) {
        if (isDone) {
            consumer.accept(value);
        } else {
            onComplete.add(consumer);
        }
        return this;
    }

    public AsyncFuture<V> whenCompleteAsync(Consumer<V> consumer) {
        return whenCompleteAsync(consumer, AsyncService.getInstance());
    }

    public AsyncFuture<V> whenCompleteAsync(Consumer<V> consumer, Executor executor) {
        return whenComplete(new AsyncUtils.AsyncConsumer<>(consumer, executor));
    }

    public <T> AsyncFuture<T> thenApplyAsync(Transformer<V, T> transformer) {
        return thenApplyAsync(transformer, AsyncService.getInstance());
    }

    public <T> AsyncFuture<T> thenApplyAsync(Transformer<V, T> transformer, Executor executor) {
        AsyncFuture<T> future = new AsyncFuture<T>();
        whenComplete(new CompletionConsumer<>(future, transformer, executor));
        return future;
    }

    public void complete(V value) {
        valueLock.lock();
        try {
            this.value = value;
            this.isDone = true;
            onComplete.call(this.value);
            valueChanged.signalAll();
        } finally {
            valueLock.unlock();
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        valueLock.lock();
        try {
            while (!isDone)
                valueChanged.await();

            return value;
        } finally {
            valueLock.unlock();
        }
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        valueLock.lock();
        try {
            if (!isDone) {
                valueChanged.await(timeout, unit);
            }

            if (isDone) {
                return value;
            } else {
                throw new TimeoutException();
            }
        } finally {
            valueLock.unlock();
        }
    }

    public static AsyncFuture<Void> runAsync(Runnable runnable) {
        return runAsync(runnable, AsyncService.getInstance());
    }

    public static AsyncFuture<Void> runAsync(Runnable runnable, Executor executor) {
        return supplyAsync(new AsyncUtils.VoidSupplier(runnable), executor);
    }

    private static class FutureSupplier<T> implements Runnable {

        private final AsyncFuture<T> future;
        private final Supplier<T> supplier;

        public FutureSupplier(AsyncFuture<T> future, Supplier<T> supplier) {
            this.future = future;
            this.supplier = supplier;
        }

        @Override
        public void run() {
            T value = supplier.get();
            future.complete(value);
        }
    }

    private static class CompletionConsumer<V, T> implements Consumer<V> {

        private final AsyncFuture<T> future;
        private final Transformer<V, T> transformer;
        private final Executor executor;

        public CompletionConsumer(AsyncFuture<T> future, Transformer<V, T> transformer, Executor executor) {
            this.future = future;
            this.transformer = transformer;
            this.executor = executor;
        }

        @Override
        public void accept(V value) {
            executor.execute(
                    new FutureSupplier<>(
                            future,
                            new Transformer.Partial<>(value, transformer)
                    )
            );
        }

    }
}
