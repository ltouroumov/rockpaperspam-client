package com.securingapps.rps.data;

import android.content.Context;
import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import okhttp3.*;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author ldavid
 * @created 3/24/17
 */
public class ApiProxy implements Interceptor {

    private static final String HOST = "http://10.0.2.2:8000";
    //private static final String HOST = "http://rps-cnc.herokuapp.com";
    private static final String TAG = ApiProxy.class.getSimpleName();
    private static ApiProxy _instance;

    public static synchronized ApiProxy getInstance() {
        if (_instance == null) {
            _instance = new ApiProxy();
        }

        return _instance;
    }

    private final OkHttpClient client;
    private final Gson gson;

    public ApiProxy() {
        client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(this)
                .build();

        gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    public String register() {
        OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .build();

        JsonObject body = new JsonObject();
        body.addProperty("client_id", DeviceData.getDeviceId());

        Request request = new Request.Builder()
            .url(String.format("%s/api/register", HOST))
            .post(RequestBody.create(MediaType.parse("application/json"), gson.toJson(body)))
            .build();

        try {
            Response response = client.newCall(request).execute();
            String respBody = response.body().string();
            JsonElement respJson = (new JsonParser()).parse(respBody);
            if (respJson.isJsonObject()) {
                JsonObject respJsonObj = respJson.getAsJsonObject();
                if (respJsonObj.has("secret")) {
                    return respJsonObj.get("secret").getAsString();
                }
            }
            Log.e(TAG, "Invalid register response");
            return null;
        } catch (IOException e) {
            Log.e(TAG, "Register failed", e);
            throw new RuntimeException(e);
        }
    }

    public void syncContacts(ContactService contactService, Context context) {
        JsonElement profile = gson.toJsonTree(contactService.getProfile());
        JsonElement friends = gson.toJsonTree(contactService.getFriends());

        String firebaseToken = FirebaseInstanceId.getInstance().getToken();

        if (firebaseToken == null) {
            Log.w(TAG, "Aborting sync, no firebase token");
            return;
        }

        JsonObject json = new JsonObject();
        json.addProperty("client_id", DeviceData.getDeviceId());
        json.addProperty("token", firebaseToken);
        json.add("profile", profile);
        json.add("friends", friends);

        File jsonFile = new File(context.getFilesDir(), "contacts.json");
        try (
                Writer fw = new FileWriter(jsonFile, false);
                JsonWriter jw = new JsonWriter(fw)
        ) {
            gson.toJson(json, jw);
            Log.d(TAG, jsonFile.getAbsolutePath() + " written");
        } catch (Exception ex) {
            Log.e(TAG, "Failed to open contacts file", ex);
        }

        Request request = new Request.Builder()
                .url(String.format("%s/api/sync", HOST))
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), gson.toJson(json)))
                .build();

        try {
            Response response = client.newBuilder()
                    .build()
                    .newCall(request)
                    .execute();

            Log.d(TAG, "Sync successful");
        } catch (IOException e) {
            Log.e(TAG, "Failed to sync", e);
        }
    }

    public Response doGet(String endpoint, Object... args) {
        Request request = new Request.Builder()
                .url(buildUrl(endpoint, args))
                .get()
                .build();

        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            Log.e(TAG, String.format("Request to %s failed", endpoint), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T doGetJson(Class<T> type, String endpoint, Object... args) {
        Response response = doGet(endpoint, args);
        if (response.isSuccessful() && response.body() != null) {
            return gson.fromJson(response.body().charStream(), type);
        } else {
            return null;
        }
    }

    public Response doPost(JsonElement body, String endpoint, Object... args) {
        Request request = new Request.Builder()
                .url(buildUrl(endpoint, args))
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), gson.toJson(body)))
                .build();

        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            Log.e(TAG, String.format("Request to %s failed", endpoint), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T doPostJson(Class<T> type, JsonElement body, String endpoint, Object... args) {
        Response response = doPost(body, endpoint, args);
        if (response.isSuccessful() && response.body() != null) {
            return gson.fromJson(response.body().charStream(), type);
        } else {
            return null;
        }
    }

    private String buildUrl(String endpoint, Object... args) {
        return String.format(
                String.format("%s/api/%s", HOST, endpoint),
                args
        );
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request();
        Request requestWithAuth = request.newBuilder()
            .header("Authorization", getAuthorization())
            .build();

        Log.d(TAG, String.format("API > %s %s", request.method(), request.url()));

        try {
            Response response = chain.proceed(requestWithAuth);
            Log.d(TAG, String.format("API < %d", response.code()));
            return response;
        } catch (IOException ex) {
            Log.d(TAG, String.format("API < %s", ex.getMessage()));
            throw new RuntimeException("Failed to perform API Call", ex);
        }
    }

    private String _authorization;

    private String getAuthorization() {
        if (_authorization == null) {
            _authorization = makeAuthorization();
            Log.d(TAG, "Authorization: " + _authorization);
        }
        return _authorization;
    }

    private String makeAuthorization() {
        SecureRandom rng = new SecureRandom();
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(DeviceData.getSecret(), "HmacSHA256");
            mac.init(keySpec);

            byte[] random = new byte[8];
            rng.nextBytes(random);
            byte[] deviceId = DeviceData.getDeviceId().getBytes("utf-8");

            mac.update(deviceId);
            mac.update(random);

            byte[] authHash = mac.doFinal();

            return String.format("Client %s:%s:%s",
                DeviceData.getDeviceId(),
                new String(Hex.encodeHex(random)),
                new String(Hex.encodeHex(authHash))
            );
        } catch (Exception ex) {
            Log.e(TAG, "Failed to make authorization token", ex);
            return "Client";
        }
    }


}
