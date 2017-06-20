package com.securingapps.rps.utils.stream;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author ldavid
 * @created 4/7/17
 */
public class CollectionStream<T> extends BaseStream<T> {

    private final Collection<T> collection;

    public CollectionStream(Collection<T> collection) {
        this.collection = collection;
    }

    @Override
    public Iterator<T> asIterator() {
        return collection.iterator();
    }
}
