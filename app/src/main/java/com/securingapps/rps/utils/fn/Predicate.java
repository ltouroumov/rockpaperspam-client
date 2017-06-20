package com.securingapps.rps.utils.fn;

/**
 * @author ldavid
 * @created 3/24/17
 */
@FunctionalInterface
public interface Predicate<T> {

    boolean matches(T value);

}
