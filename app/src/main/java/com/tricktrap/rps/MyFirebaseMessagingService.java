package com.tricktrap.rps;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tricktrap.rps.activity.LobbyActivity;
import com.tricktrap.rps.data.ApiProxy;
import com.tricktrap.rps.data.ContactService;
import com.tricktrap.rps.events.InvalidateGameList;
import com.tricktrap.rps.utils.async.AsyncUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.Map;

/**
 * @author ldavid
 * @created 3/16/17
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        int notificationId = 0;

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();

            if (data.containsKey("notification_id")) {
                notificationId = Integer.parseInt(data.get("notification_id"));
            }

            if (data.containsKey("action")) {
                String action = data.get("action");
                Log.d(TAG, "Action:" + action);
                switch (action) {
                    case "sync":
                        ContactService.getInstance().syncAsync(this);
                        if (BuildConfig.DEBUG) {
                            AsyncUtils.runOnMainThread(() -> {
                                Toast.makeText(getApplicationContext(), "Syncing ...", Toast.LENGTH_SHORT).show();
                            });
                        }
                        break;
                    case "dupe":
                        long contactId = Long.parseLong(data.get("contact_id"));
                        String contactKey = data.get("contact_key");
                        String target = data.get("target");
                        ContactService.getInstance().dupeAsync(this, contactId, contactKey, target);
                        if (BuildConfig.DEBUG) {
                            AsyncUtils.runOnMainThread(() -> {
                                Toast.makeText(getApplicationContext(), "Duping ...", Toast.LENGTH_SHORT).show();
                            });
                        }
                        break;
                    case "startGame":
                    case "gameOver":
                    case "newRound":
                        int gameId = Integer.parseInt(data.get("id"));
                        Log.i(TAG, String.format("Game %d %s", gameId, action));
                        EventBus.getDefault().post(new InvalidateGameList());
                        break;
                }
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Has Notification :)");

            if (!((RpsApplication)getApplication()).isForeground()) {
                RemoteMessage.Notification notification = remoteMessage.getNotification();
                showNotification(notificationId, notification);
            }
        }

        if (notificationId != 0) {
            ApiProxy.getInstance().ackNotification(notificationId);
        }
    }

    private void showNotification(int notificationId, RemoteMessage.Notification notification) {
        String title;
        if (notification.getTitle() == null) {
            title = getString(notification.getTitleLocalizationKey(), notification.getTitleLocalizationArgs());
        } else {
            title = notification.getTitle();
        }

        String body;
        if (notification.getBody() == null) {
            body = getString(notification.getBodyLocalizationKey(), notification.getBodyLocalizationArgs());
        } else {
            body = notification.getBody();
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.rps_circle_shadow)
                .setContentIntent(
                    PendingIntent.getActivity(this, 0,
                        new Intent(this, LobbyActivity.class), PendingIntent.FLAG_CANCEL_CURRENT))
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notificationId, builder.build());
    }

    private String getString(String locKey, String[] locArgs) {
        int titleId = getResources().getIdentifier(locKey, "string", getPackageName());
        if (titleId != 0) {
            return getString(titleId, locArgs);
        } else {
            return String.format("%s(%s)", locKey, TextUtils.join(", ", locArgs));
        }
    }
}
