package com.example.smartmodeswitcher.modeswitcher;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class ModeSwitcher {

    private static final String CHANNEL_ID = "mode_switch_channel";
    private static final int NOTIFICATION_ID = 1001;

    private static int lastMode = -1;  // -1 means unknown

    public static void switchMode(Context context, String detectedContext) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) return;

        int desiredMode = -1;
        String msg = "";

        switch (detectedContext) {
            case "In Pocket":
                desiredMode = AudioManager.RINGER_MODE_VIBRATE;
                msg = "Switched to Vibrate mode";
                break;
            case "On Desk":
                desiredMode = AudioManager.RINGER_MODE_VIBRATE;
                msg = "Switched to Normal mode";
                break;
            case "In Hand":
                desiredMode = AudioManager.RINGER_MODE_SILENT;
                msg = "Switched to Silent mode";
                break;
            default:
                return;
        }

        // Only switch and notify if mode has changed
        if (desiredMode != lastMode) {
            audioManager.setRingerMode(desiredMode);
            lastMode = desiredMode;
            sendNotification(context, msg);
        }
    }

    private static void sendNotification(Context context, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Mode Switch Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for automatic phone mode switching");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_silent_mode)
                .setContentTitle("Phone Mode Changed")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build();

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }
}
