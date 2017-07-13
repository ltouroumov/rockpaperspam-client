package com.securingapps.rps.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.UUID;

/**
 * @author ldavid
 * @created 3/15/17
 */
public class DeviceData {

    private static final String TAG = DeviceData.class.getSimpleName();
    private static SharedPreferences sharedPrefs;
    private static String clientId = null;
    private static String firebaseToken = null;
    private static byte[] secret = null;
    private static final String PREF_CLIENT_ID = "PREF_CLIENT_ID";
    private static final String PREF_FIREBASE_TOKEN = "PREF_FIREBASE_TOKEN";
    private static final String PREF_SECRET = "PREF_SECRET";

    public synchronized static void loadData(Context context) {
        sharedPrefs = context.getSharedPreferences(PREF_CLIENT_ID, Context.MODE_PRIVATE);

        clientId = sharedPrefs.getString(PREF_CLIENT_ID, null);
        if (clientId == null) {
            clientId = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(PREF_CLIENT_ID, clientId);
            editor.apply();
        }

        firebaseToken = sharedPrefs.getString(PREF_FIREBASE_TOKEN, null);

        String secretHex = sharedPrefs.getString(PREF_SECRET, null);
        if (secretHex != null) {
            try {
                secret = Hex.decodeHex(secretHex.toCharArray());
            } catch (DecoderException e) {
                Log.e(TAG, "Failed to read secret");
            }
        }

        Log.i(TAG, String.format("Device ID: %s", clientId));
    }

    public synchronized static String getDeviceId() {
        if (clientId == null) {
            throw new RuntimeException("Device ID not loaded");
        }
        return clientId;
    }

    public synchronized static byte[] getSecret() {
        if (secret == null) {
            throw new RuntimeException("Secret not loaded");
        }
        return secret;
    }

    public synchronized static String getSecretString() {
        if (secret == null) {
            throw new RuntimeException("Secret not loaded");
        }
        return new String(Hex.encodeHex(secret));
    }

    public static void clearSecret() {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.remove(PREF_SECRET);
        editor.apply();

        DeviceData.secret = null;
    }

    public synchronized static void setSecret(byte[] secret) {
        String secretHex = new String(Hex.encodeHex(secret));
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(PREF_SECRET, secretHex);
        editor.apply();

        DeviceData.secret = secret;
    }

    public synchronized static void setSecret(String secret) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(PREF_SECRET, secret);
        editor.apply();

        try {
            DeviceData.secret = Hex.decodeHex(secret.toCharArray());
        } catch (DecoderException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getFirebaseToken() {
        if (firebaseToken == null) {
            throw new RuntimeException("Firebase Token not loaded");
        }
        return firebaseToken;
    }

    public static void setFirebaseToken(String firebaseToken) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(PREF_FIREBASE_TOKEN, firebaseToken);
        editor.apply();
        DeviceData.firebaseToken = firebaseToken;
    }

    public static boolean hasSecret() {
        return secret != null;
    }

}
