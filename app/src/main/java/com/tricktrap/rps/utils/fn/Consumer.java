package com.tricktrap.rps.utils.fn;

/**
 * @author ldavid
 * @created 3/23/17
 */
@FunctionalInterface
public interface Consumer<V> {

    void accept(V value);

}
