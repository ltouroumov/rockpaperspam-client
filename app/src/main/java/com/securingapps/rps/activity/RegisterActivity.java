package com.securingapps.rps.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.securingapps.rps.R;
import com.securingapps.rps.data.ApiProxy;
import com.securingapps.rps.data.ContactService;
import com.securingapps.rps.data.DeviceData;
import com.securingapps.rps.utils.async.AsyncFuture;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
    }

    @Override
    protected void onStart() {
        super.onStart();

        AsyncFuture.runAsync(() -> {
            if (!DeviceData.hasSecret()) {
                Log.d(TAG, "Registering");
                String secret = ApiProxy.getInstance().register();
                DeviceData.setSecret(secret);
            } else {
                Log.d(TAG, "Already Registered");
            }
            Log.d(TAG, DeviceData.getDeviceId() + ":" + DeviceData.getSecretString());
        }).thenApplyAsync(
            v -> ContactService.getInstance().syncAsync(RegisterActivity.this)
        ).whenComplete(v -> {
            Intent intent = new Intent(RegisterActivity.this, LobbyActivity.class);
            startActivity(intent);
        });
    }
}
