package com.tricktrap.rps.data;

import android.util.Log;
import com.tricktrap.rps.utils.async.AsyncFuture;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ldavid
 * @created 4/13/17
 */
public class FriendService {

    private static final String TAG = FriendService.class.getSimpleName();
    private static FriendService _instance;

    private static ApiProxy api = ApiProxy.getInstance();

    public static synchronized FriendService getInstance() {
        if (_instance == null) {
            _instance = new FriendService();
        }
        return _instance;
    }

    private FriendService() {

    }
    private long _lastRefresh = 0;
    private GetFriendsResponse _GetFriendsResponse = null;
    private Map<String, Friend> _friendCache = new HashMap<>();
    private Map<String, AsyncFuture<Friend>> _futureCache = new HashMap<>();

    public synchronized AsyncFuture<Friend> getFriend(String id) {
        if (!_friendCache.containsKey(id)) {
            if (_futureCache.containsKey(id)) {
                return _futureCache.get(id);
            } else {
                AsyncFuture<Friend> future = AsyncFuture
                    .supplyAsync(() -> api.doGetJson(Friend.class, "profile?of=%s", id))
                    .whenComplete(response -> {
                        _friendCache.put(id, response);
                        _futureCache.remove(id);
                    });
                _futureCache.put(id, future);
                return future;
            }
        } else {
            return AsyncFuture.completed(_friendCache.get(id));
        }
    }

    public static long CACHE_DURATION = 5L * 60L * 1000L * 1000L * 1000L;

    public synchronized AsyncFuture<Friend[]> getFriends() {
        long now = System.nanoTime();
        if (_GetFriendsResponse == null || Math.abs(now - _lastRefresh) >= CACHE_DURATION) {
            Log.d(TAG, "From network");
            return AsyncFuture
                .supplyAsync(() -> api.doGetJson(GetFriendsResponse.class, "friends?of=%s", DeviceData.getDeviceId()))
                .whenCompleteAsync(response -> {
                    this._lastRefresh = System.nanoTime();
                    this._GetFriendsResponse = response;
                    for (Friend f : this._GetFriendsResponse.friends) {
                        _friendCache.put(f.getId(), f);
                    }
                })
                .thenApplyAsync(response -> response.friends);
        } else {
            Log.d(TAG, "From cache");
            return AsyncFuture.completed(_GetFriendsResponse.friends);
        }
    }

    private class GetFriendsResponse {
        public Friend[] friends;
        public GetClientResponse client;
    }

    private class GetClientResponse {
        public String id;
        public String displayName;
    }

}
