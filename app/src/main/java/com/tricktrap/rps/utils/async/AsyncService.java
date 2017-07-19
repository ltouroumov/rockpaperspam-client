package com.tricktrap.rps.utils.async;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author ldavid
 * @created 3/23/17
 */
public class AsyncService implements Executor {

    private static AsyncService _instance;

    public synchronized static AsyncService getInstance() {
        if (_instance == null) {
            _instance = new AsyncService();
        }

        return _instance;
    }

    private final Executor executor;

    private AsyncService() {
        executor = Executors.newCachedThreadPool();
    }

    @Override
    public void execute(Runnable command) {
        executor.execute(command);
    }

}
