package com.tricktrap.rps.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import com.tricktrap.rps.R;
import com.tricktrap.rps.data.Contact;
import com.tricktrap.rps.data.ContactService;
import com.tricktrap.rps.data.DeviceData;

public class Startup extends AppCompatActivity {

    private static final int PERM_REQ_CONTACTS = 1;
    private static final String TAG = Startup.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
    }

    @Override
    protected void onStart() {
        super.onStart();

        DeviceData.loadData(this);
        Log.d(TAG, "Firebase Token:" + FirebaseInstanceId.getInstance().getToken());

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE},
                    PERM_REQ_CONTACTS);
        } else {
            setupProfile();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERM_REQ_CONTACTS: {
                boolean isGranted = true;

                for (int i = 0; i < grantResults.length; i++) {
                    boolean isPermGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                    String perm = permissions[i];

                    Log.d(TAG, perm + ": " + isPermGranted);
                    isGranted = isGranted && isPermGranted;
                }

                if (!isGranted) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Why? :'(").setMessage("I need those permission -_-");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    Log.d("app", "Got Permissions");
                    setupProfile();
                }
            }
            break;
        }
    }

    private void setupProfile() {
        ContactService.getInstance().loadAsync(this, false).whenComplete(this::showProfileSetup);
    }

    private void showProfileSetup(ContactService contactService) {
        Contact profile = contactService.getProfile();

        if (profile.isCoherent()) {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        }
    }
}
