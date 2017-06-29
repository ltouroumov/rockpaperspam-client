package com.securingapps.rps;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

/**
 * @author ldavid
 * @created 6/22/17
 */
public class RpsApplication extends Application implements Thread.UncaughtExceptionHandler {

    private static final String TAG = RpsApplication.class.getSimpleName();

    private ActivityLifecycleCallbacksHandler activityLifecycleCallbacksHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(this);
        activityLifecycleCallbacksHandler = new ActivityLifecycleCallbacksHandler();
        registerActivityLifecycleCallbacks(activityLifecycleCallbacksHandler);
    }

    public boolean isForeground() {
        if (activityLifecycleCallbacksHandler == null) {
            Log.d(TAG, "No Lifecycle Callbacks");
            return false;
        }

        Log.d(TAG, "Number of activities started:" + activityLifecycleCallbacksHandler.getActivitiesStarted());
        Log.d(TAG, "Number of activities resumed:" + activityLifecycleCallbacksHandler.getActivitiesResumed());
        return activityLifecycleCallbacksHandler.getActivitiesStarted() > 0;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Log.e(TAG, "Uncaught Exception", e);
    }

    private static class ActivityLifecycleCallbacksHandler implements ActivityLifecycleCallbacks {

        private static final String TAG = ActivityLifecycleCallbacksHandler.class.getSimpleName();

        private int activitiesStarted = 0;
        private int activitiesResumed = 0;

        public int getActivitiesStarted() {
            return activitiesStarted;
        }

        public int getActivitiesResumed() {
            return activitiesResumed;
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            activitiesStarted++;
            Log.d(TAG, "Activity Started:" + activity.getClass().getSimpleName());
        }

        @Override
        public void onActivityResumed(Activity activity) {
            activitiesResumed++;
            Log.d(TAG, "Activity Resumed:" + activity.getClass().getSimpleName());
        }

        @Override
        public void onActivityPaused(Activity activity) {
            activitiesResumed--;
            Log.d(TAG, "Activity Paused:" + activity.getClass().getSimpleName());
        }

        @Override
        public void onActivityStopped(Activity activity) {
            activitiesStarted--;
            Log.d(TAG, "Activity Stopped:" + activity.getClass().getSimpleName());
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }

    }
}
