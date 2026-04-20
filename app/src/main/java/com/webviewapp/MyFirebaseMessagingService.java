package com.webviewapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * FCM Push Notification handler.
 *
 * Payload format (send from Firebase console or your server):
 * {
 *   "to": "<device_token>",
 *   "notification": {
 *     "title": "Hello!",
 *     "body":  "You have a new message."
 *   },
 *   "data": {
 *     "url": "https://yourwebsite.com/specific-page"  ← optional deep-link
 *   }
 * }
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID   = "webviewapp_channel";
    private static final String CHANNEL_NAME = "App Notifications";
    private static final int    NOTIF_ID     = 1001;

    // ─────────────────────────────────────────────────────────────

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String title = Config.APP_NAME;
        String body  = "";
        String url   = null;

        // 1. Notification payload
        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getTitle() != null)
                title = remoteMessage.getNotification().getTitle();
            if (remoteMessage.getNotification().getBody() != null)
                body  = remoteMessage.getNotification().getBody();
        }

        // 2. Data payload (always arrives even in background)
        if (remoteMessage.getData().containsKey("title"))
            title = remoteMessage.getData().get("title");
        if (remoteMessage.getData().containsKey("body"))
            body  = remoteMessage.getData().get("body");
        if (remoteMessage.getData().containsKey("url"))
            url   = remoteMessage.getData().get("url");

        sendNotification(title, body, url);
    }

    @Override
    public void onNewToken(String token) {
        // TODO: Send this token to your backend to target individual devices.
    }

    // ─────────────────────────────────────────────────────────────

    private void sendNotification(String title, String body, String url) {
        createChannel();

        // Build intent — pass URL so MainActivity can deep-link
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (url != null) intent.putExtra("url", url);

        int flags = PendingIntent.FLAG_ONE_SHOT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            flags |= PendingIntent.FLAG_IMMUTABLE;

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, flags);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setLights(Color.BLUE, 500, 500)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.notify(NOTIF_ID, builder.build());
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("Push notifications from " + Config.APP_NAME);
        channel.enableLights(true);
        channel.setLightColor(Color.BLUE);
        channel.enableVibration(true);
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.createNotificationChannel(channel);
    }
}
