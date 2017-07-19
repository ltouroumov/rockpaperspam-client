package com.tricktrap.rps.data;

import android.util.Log;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ldavid
 * @created 7/13/17
 */
public class ConfigManager {

    private static final String TAG = ConfigManager.class.getSimpleName();

    private static ConfigManager _instance = null;

    public static ConfigManager getInstance() {
        if (_instance == null) {
            _instance = new ConfigManager();
        }
        return _instance;
    }

    public static long CACHE_DURATION = 5L * 60L * 1000L * 1000L * 1000L;

    private long _lastRefresh = 0;
    private Map<String, JsonElement> config;

    ConfigManager() {
        config = new HashMap<>();
    }

    public void refresh() {
        long curTime = System.nanoTime();

        if ((curTime - _lastRefresh) < CACHE_DURATION) {
            return;
        }

        JsonElement jsonConfig = ApiProxy.getInstance().doGetJson("config");

        if (!jsonConfig.isJsonObject()) {
            return;
        }

        for (Map.Entry<String, JsonElement> entry : jsonConfig.getAsJsonObject().entrySet()) {
            config.put(entry.getKey(), entry.getValue());
            Log.d(TAG, String.format("%s = %s", entry.getKey(), entry.getValue()));
        }

        _lastRefresh = curTime;
    }

    public boolean has(String key) {
        return config.containsKey(key);
    }

    public String getString(String key) {
        if (config.containsKey(key)) {
            return config.get(key).getAsString();
        } else {
            return null;
        }
    }

    public int getInt(String key) {
        if (config.containsKey(key)) {
            return config.get(key).getAsInt();
        } else {
            return 0;
        }
    }

}
