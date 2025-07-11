package com.example.smartmodeswitcher.modeswitcher;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.smartmodeswitcher.data.Profile;
import com.example.smartmodeswitcher.utils.ProfileEvaluator;

public class ModeSwitcher {

    private static final String CHANNEL_ID = "mode_switch_channel";
    private static final int NOTIFICATION_ID = 1001;

    private static int lastMode = -1;  // -1 means unknown

    private static long lastSwitchTime = 0;
    private static final long COOLDOWN_MS = 10000; // 10 seconds

    public static void switchMode(Context context, String detectedContext) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSwitchTime < COOLDOWN_MS) return;

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) return;

        // Check if we have notification policy access
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null && !notificationManager.isNotificationPolicyAccessGranted()) {
                Toast.makeText(context, "Please grant notification policy access to change phone mode", Toast.LENGTH_LONG).show();
                // Open notification policy access settings
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return;
            }
        }

        // First check if there's an active profile
        Profile activeProfile = ProfileEvaluator.getActiveProfile(context);
        if (activeProfile != null) {
            int desiredMode = getModeFromString(activeProfile.getMode());
            if (desiredMode != lastMode) {
                try {
                    audioManager.setRingerMode(desiredMode);
                    lastMode = desiredMode;
                    sendNotification(context, "Profile Active: " + activeProfile.getName() + " - " + activeProfile.getMode() + " mode");
                } catch (SecurityException e) {
                    Toast.makeText(context, "Permission denied to change phone mode", Toast.LENGTH_LONG).show();
                }
            }
            return; // Active profile takes precedence
        }

        // If no active profile, use sensor-based mode
        int desiredMode = -1;
        String msg = "";

        switch (detectedContext) {
            case "In Pocket":
                desiredMode = AudioManager.RINGER_MODE_VIBRATE;
                msg = "Switched to Vibrate mode (In Pocket)";
                break;
            case "On Desk":
                desiredMode = AudioManager.RINGER_MODE_NORMAL;
                msg = "Switched to Normal mode (On Desk)";
                break;
            case "In Hand":
                desiredMode = AudioManager.RINGER_MODE_NORMAL;
                msg = "Switched to Silent mode";
                break;
            default:
                return;
        }

        // Only switch and notify if mode has changed
        if (desiredMode != lastMode) {
            try {
                audioManager.setRingerMode(desiredMode);
                lastMode = desiredMode;
                sendNotification(context, msg);
            } catch (SecurityException e) {
                Toast.makeText(context, "Permission denied to change phone mode", Toast.LENGTH_LONG).show();
            }
        }
    }

    private static int getModeFromString(String mode) {
        switch (mode) {
            case "SILENT":
                return AudioManager.RINGER_MODE_SILENT;
            case "VIBRATE":
                return AudioManager.RINGER_MODE_VIBRATE;
            case "NORMAL":
                return AudioManager.RINGER_MODE_NORMAL;
            default:
                return AudioManager.RINGER_MODE_NORMAL;
        }
    }

    private static void sendNotification(Context context, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Mode Switch Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for automatic phone mode switching");
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_silent_mode)
                .setContentTitle("Phone Mode Changed")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
