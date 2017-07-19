package com.tricktrap.rps.utils.stream;

import java.util.Collection;

/**
 * @author ldavid
 * @created 4/7/17
 */
public class StreamUtils {

    public static <T> Stream<T> toStream(Collection<T> collection) {
        return new CollectionStream<>(collection);
    }

}
