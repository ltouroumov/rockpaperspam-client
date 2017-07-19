package com.tricktrap.rps.data;

/**
 * @author ldavid
 * @created 3/24/17
 */
public interface SearchFunction<T> {

    boolean matches(T value);

}
