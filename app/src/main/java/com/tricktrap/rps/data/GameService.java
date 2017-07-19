package com.tricktrap.rps.data;

import com.google.gson.JsonObject;
import com.tricktrap.rps.utils.async.AsyncFuture;

/**
 * @author ldavid
 * @created 5/15/17
 */
public class GameService {

    private static GameService _instance;

    public static synchronized GameService getInstance() {
        if (_instance == null) {
            _instance = new GameService();
        }
        return _instance;
    }

    private GameService() {

    }

    public void startGame(String opponent, int gameLength) {
        JsonObject body = new JsonObject();
        body.addProperty("challenger", DeviceData.getDeviceId());
        body.addProperty("defender", opponent);
        body.addProperty("rounds", gameLength);

        AsyncFuture.runAsync(() -> {
            ApiProxy.getInstance().doPost(body, "games/start");
        });
    }

}
