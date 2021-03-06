package com.tricktrap.rps.utils;

import android.app.Activity;

import java.util.concurrent.Executor;

/**
 * @author ldavid
 * @created 4/7/17
 */
public class UiRunner implements Executor {

    private final Activity activity;

    public UiRunner(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void execute(Runnable command) {
        activity.runOnUiThread(command);
    }
}
