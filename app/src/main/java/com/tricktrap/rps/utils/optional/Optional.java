package com.tricktrap.rps.utils.optional;

import com.tricktrap.rps.utils.fn.Supplier;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * @author ldavid
 * @created 4/7/17
 */
public class Optional<T> {

    private static Optional<?> EMPTY = new Optional<>();

    private final T value;

    public static <T> Optional<T> of(T value) {
        return new Optional<>(value);
    }

    public static <T> Optional<T> empty() {
        @SuppressWarnings("unchecked")
        Optional<T> t = (Optional<T>) EMPTY;
        return t;
    }

    private Optional() {
        this.value = null;
    }

    private Optional(T value) {
        this.value = Objects.requireNonNull(value);
    }

    public boolean isPresent() {
        return value != null;
    }

    public T get() {
        if (value == null) {
            throw new NoSuchElementException("Optional is empty");
        }
        return value;
    }

    public T getOrElse(T orElse) {
        if (isPresent()) {
            return value;
        } else {
            return orElse;
        }
    }

    public T getOrElse(Supplier<T> orElse) {
        if (isPresent()) {
            return value;
        } else {
            return orElse.get();
        }
    }

}
