package com.securingapps.rps.utils.async;

import com.securingapps.rps.utils.fn.Consumer;
import com.securingapps.rps.utils.fn.Supplier;

import java.util.concurrent.Executor;

/**
 * @author ldavid
 * @created 3/23/17
 */
public class AsyncUtils {

    public static class AsyncConsumer<T> implements Consumer<T> {

        private final Consumer<T> consumer;
        private final Executor executor;

        public AsyncConsumer(Consumer<T> consumer, Executor executor) {
            this.consumer = consumer;
            this.executor = executor;
        }

        @Override
        public void accept(T value) {
            executor.execute(new PartialConsumer<>(consumer, value));
        }

    }

    public static class PartialConsumer<T> implements Runnable {
        private final Consumer<T> consumer;
        private final T value;

        public PartialConsumer(Consumer<T> consumer, T value) {
            this.consumer = consumer;
            this.value = value;
        }

        @Override
        public void run() {
            consumer.accept(value);
        }
    }

    public static class VoidSupplier implements Supplier<Void> {

        private final Runnable runnable;

        public VoidSupplier(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public Void get() {
            runnable.run();
            return null;
        }

    }
}
