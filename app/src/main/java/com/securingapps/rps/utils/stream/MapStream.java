package com.securingapps.rps.utils.stream;

import com.securingapps.rps.utils.fn.Transformer;

import java.util.Iterator;

/**
 * @author ldavid
 * @created 4/7/17
 */
public class MapStream<T, U> extends BaseStream<U> {
    private final Stream<T> parent;
    private final Transformer<T, U> transformer;

    public MapStream(Stream<T> parent, Transformer<T, U> transformer) {
        this.parent = parent;
        this.transformer = transformer;
    }

    @Override
    public Iterator<U> asIterator() {
        return new MapIterator();
    }

    private class MapIterator implements Iterator<U> {

        private final Iterator<T> parentIterator;

        public MapIterator() {
            parentIterator = parent.asIterator();
        }

        @Override
        public boolean hasNext() {
            return parentIterator.hasNext();
        }

        @Override
        public U next() {
            T parentNext = parentIterator.next();
            return transformer.transform(parentNext);
        }

    }
}
