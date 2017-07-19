package com.tricktrap.rps.utils.stream;

import com.tricktrap.rps.utils.optional.Optional;
import com.tricktrap.rps.utils.fn.Predicate;
import com.tricktrap.rps.utils.fn.Transformer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author ldavid
 * @created 4/7/17
 */
public abstract class BaseStream<T> implements Stream<T> {

    @Override
    public Stream<T> filter(Predicate<T> predicate) {
        return new FilterStream<>(this, predicate);
    }

    @Override
    public <U> Stream<U> map(Transformer<T, U> transformer) {
        return new MapStream<>(this, transformer);
    }

    @Override
    public Optional<T> find(Predicate<T> predicate) {
        Iterator<T> iterator = asIterator();
        while (iterator.hasNext()) {
            T value = iterator.next();
            if (predicate.matches(value)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<T> asList() {
        List<T> list = new ArrayList<T>();
        Iterator<T> iterator = asIterator();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }
}
