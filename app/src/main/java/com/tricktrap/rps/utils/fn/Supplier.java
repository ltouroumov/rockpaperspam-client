package com.tricktrap.rps.utils.fn;

/**
 * @author ldavid
 * @created 3/23/17
 */
@FunctionalInterface
public interface Supplier<V> {

    V get();

}
