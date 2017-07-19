package com.tricktrap.rps.utils.stream;

import com.tricktrap.rps.utils.fn.Predicate;
import com.tricktrap.rps.utils.fn.Transformer;
import com.tricktrap.rps.utils.optional.Optional;

import java.util.Iterator;
import java.util.List;

/**
 * Represents a stream of elements
 */
public interface Stream<T> {

    Stream<T> filter(Predicate<T> predicate);

    <U> Stream<U> map(Transformer<T, U> transformer);

    Optional<T> find(Predicate<T> predicate);

    Iterator<T> asIterator();

    List<T> asList();

}
