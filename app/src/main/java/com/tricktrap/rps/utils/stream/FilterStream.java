package com.tricktrap.rps.utils.stream;

import com.tricktrap.rps.utils.fn.Predicate;

import java.util.Iterator;

/**
 * @author ldavid
 * @created 4/7/17
 */
public class FilterStream<T> extends BaseStream<T> {
    private final Predicate<T> predicate;
    private final Stream<T> parent;

    public FilterStream(Stream<T> parent, Predicate<T> predicate) {
        this.parent = parent;
        this.predicate = predicate;
    }

    @Override
    public Iterator<T> asIterator() {
        return new FilterIterator();
    }

    private class FilterIterator implements Iterator<T> {

        private final Iterator<T> parentIterator;
        private T value;
        private boolean hasNext;

        public FilterIterator() {
            parentIterator = parent.asIterator();
            forward();
        }

        private void forward() {
            if (!parentIterator.hasNext())
                return;

            value = parentIterator.next();
            while (!predicate.matches(value)) {
                if (parentIterator.hasNext()) {
                    value = parentIterator.next();
                } else {
                    hasNext = false;
                }
            }
            hasNext = true;
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public T next() {
            T next = value;
            forward();
            return next;
        }

    }
}
