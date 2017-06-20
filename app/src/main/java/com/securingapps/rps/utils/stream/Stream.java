package com.securingapps.rps.utils.stream;

import com.securingapps.rps.utils.fn.Predicate;
import com.securingapps.rps.utils.fn.Transformer;
import com.securingapps.rps.utils.optional.Optional;

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
