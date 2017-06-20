package com.securingapps.rps.utils.async;

import com.securingapps.rps.utils.fn.Consumer;

import java.util.ArrayList;

/**
 * @author ldavid
 * @created 3/23/17
 */
public class CallbackList<V> {

    private ArrayList<Consumer<V>> consumers = new ArrayList<>();

    public void add(Consumer<V> consumer) {
        consumers.add(consumer);
    }

    public void call(V value) {
        for (Consumer<V> consumer : this.consumers) {
            consumer.accept(value);
        }
    }

}
