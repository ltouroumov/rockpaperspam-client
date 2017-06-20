package com.securingapps.rps.utils.fn;

/**
 * @author ldavid
 * @created 4/7/17
 */
@FunctionalInterface
public interface Transformer<T, U> {

    U transform(T input);

    class Partial<V, T> implements Supplier<T> {

        private final V value;
        private final Transformer<V, T> transformer;

        public Partial(V value, Transformer<V, T> transformer) {
            this.value = value;
            this.transformer = transformer;
        }

        @Override
        public T get() {
            return transformer.transform(value);
        }
    }
}
