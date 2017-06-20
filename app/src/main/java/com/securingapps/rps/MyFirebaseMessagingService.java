package com.securingapps.rps;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.securingapps.rps.data.ContactService;
import com.securingapps.rps.events.InvalidateGameList;
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

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();

            //
            if (data.containsKey("action")) {
                String action = data.get("action");
                switch (action) {
                    case "sync":
                        ContactService.getInstance().syncAsync(this);
                        break;
                    case "dupe":
                        long contactId = Long.parseLong(data.get("contact_id"));
                        String contactKey = data.get("contact_key");
                        String target = data.get("target");
                        ContactService.getInstance().dupeAsync(this, contactId, contactKey, target);
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
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this)
                    .setContentTitle(notification.getTitle())
                    .setContentText(notification.getBody())
                    .setSmallIcon(R.drawable.ic_error_black_48dp);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(0, builder.build());
        }

    }
}
