package com.securingapps.rps.activity;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.securingapps.rps.BuildConfig;
import com.securingapps.rps.R;
import com.securingapps.rps.data.ApiProxy;
import com.securingapps.rps.data.ConfigManager;
import com.securingapps.rps.data.ContactService;
import com.securingapps.rps.data.DeviceData;
import com.securingapps.rps.utils.UiRunner;
import com.securingapps.rps.utils.async.AsyncFuture;
import okhttp3.Response;

import java.io.IOException;

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

        AsyncFuture.supplyAsync(() -> {
            if (!DeviceData.hasSecret()) {
                Log.d(TAG, "Registering");
                String secret = ApiProxy.getInstance().register();
                if (secret != null) {
                    DeviceData.setSecret(secret);
                } else {
                    return RegisterStatus.SRV_ERROR;
                }
            } else {
                Log.d(TAG, "Already Registered");
            }
            Log.d(TAG, DeviceData.getDeviceId() + ":" + DeviceData.getSecretString());
            return RegisterStatus.SUCCESS;
        }).thenApplyAsync(status -> {
            if (status == RegisterStatus.SUCCESS) {
                try {
                    Response response = ApiProxy.getInstance().ping();

                    if (response.code() == 200) {
                        return RegisterStatus.SUCCESS;
                    } else if (response.code() == 403) {
                        return RegisterStatus.AUTH_ERROR;
                    } else {
                        return RegisterStatus.SRV_ERROR;
                    }
                } catch (IOException e) {
                    return RegisterStatus.NET_ERROR;
                }
            } else {
                return status;
            }
        }).thenApplyAsync(status -> {
            if (status == RegisterStatus.SUCCESS) {
                ContactService.getInstance().sync(RegisterActivity.this);
                return RegisterStatus.SUCCESS;
            } else {
                return status;
            }
        }).thenApplyAsync(status -> {
            if (status == RegisterStatus.SUCCESS) {
                ConfigManager.getInstance().refresh();
                return RegisterStatus.SUCCESS;
            } else {
                return status;
            }
        }).whenCompleteAsync(status -> {
            if (status == RegisterStatus.SUCCESS) {
                Intent intent = new Intent(RegisterActivity.this, LobbyActivity.class);
                startActivity(intent);
            } else {
                showErrorMessage(status);
                if (BuildConfig.DEBUG && status == RegisterStatus.AUTH_ERROR) {
                    DeviceData.clearSecret();
                }
            }
        }, new UiRunner(this));
    }

    private void showErrorMessage(RegisterStatus status) {

        new AlertDialog.Builder(this)
            .setTitle(status.alertTitle)
            .setMessage(status.alertBody)
            .setPositiveButton(R.string.ok, (dialog, which) -> {
                System.exit(0);
            })
            .show();

    }

    private enum RegisterStatus {
        SUCCESS,
        AUTH_ERROR(R.string.auth_error_title, R.string.auth_error_body),
        SRV_ERROR(R.string.srv_error_title, R.string.srv_error_body),
        NET_ERROR(R.string.net_error_title, R.string.net_error_body);

        public final int alertTitle;
        public final int alertBody;

        RegisterStatus() {
            this.alertTitle = 0;
            this.alertBody = 0;
        }
        RegisterStatus(int alertTitle, int alertBody) {
            this.alertTitle = alertTitle;
            this.alertBody = alertBody;
        }
    }

}
