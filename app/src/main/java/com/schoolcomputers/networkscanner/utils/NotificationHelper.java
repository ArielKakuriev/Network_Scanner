package com.schoolcomputers.networkscanner.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.schoolcomputers.networkscanner.R;
import com.schoolcomputers.networkscanner.activities.MainActivity;

/**
 * Centralizes all notification creation for the app.
 * Creates channels on first use (Android 8+).
 *
 * Respects the "notifications_enabled" SharedPreference:
 *   - true  → show scan-complete notification WITH sound
 *   - false → suppress all non-foreground notifications
 *
 * NOTE: CHANNEL_SCAN was renamed from "channel_scan" to "channel_scan_v2"
 * because Android does not allow changing the importance of an already-created
 * channel. The old silent channel (IMPORTANCE_LOW) is deleted on upgrade and
 * the new one (IMPORTANCE_DEFAULT + sound) is created in its place.
 */
public class NotificationHelper {

    // Old silent channel – kept only so we can delete it on upgrade
    private static final String CHANNEL_SCAN_LEGACY = "channel_scan";

    public static final String CHANNEL_SCAN  = "channel_scan_v2";   // with sound
    public static final String CHANNEL_ALERT = "channel_alert";

    public static final int NOTIF_SCAN_PROGRESS = 1001;
    public static final int NOTIF_SCAN_DONE     = 1002;
    public static final int NOTIF_NEW_DEVICE    = 1003;

    private static final String PREFS_NAME     = "app_prefs";
    private static final String PREF_NOTIF_KEY = "notifications_enabled";

    private final Context             context;
    private final NotificationManager manager;

    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.manager = (NotificationManager) this.context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        createChannels();
    }

    // ── channel setup ────────────────────────────────────────────────────────

    private void createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Delete the old silent channel so Android recreates it with sound
            manager.deleteNotificationChannel(CHANNEL_SCAN_LEGACY);

            // Scan-complete channel – DEFAULT importance = sound + heads-up
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            AudioAttributes audioAttr = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            NotificationChannel scanChannel = new NotificationChannel(
                    CHANNEL_SCAN, "Scan Results",
                    NotificationManager.IMPORTANCE_DEFAULT);
            scanChannel.setDescription("Plays a sound when a network scan finishes");
            scanChannel.setSound(soundUri, audioAttr);
            scanChannel.enableVibration(true);

            // Alert channel – unchanged
            NotificationChannel alertChannel = new NotificationChannel(
                    CHANNEL_ALERT, "Network Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT);
            alertChannel.setDescription("Alerts about new or unknown devices");

            manager.createNotificationChannel(scanChannel);
            manager.createNotificationChannel(alertChannel);
        }
    }

    // ── preference helper ────────────────────────────────────────────────────

    /** Returns true if the user has notifications enabled (default: true). */
    public boolean areNotificationsEnabled() {
        return context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(PREF_NOTIF_KEY, true);
    }

    // ── public API ───────────────────────────────────────────────────────────

    /**
     * Shows the "scanning…" progress notification.
     * This is always shown (it is the foreground-service notification required
     * by Android; suppressing it would crash the service on API 26+).
     */
    public void showScanningNotification() {
        PendingIntent pi = buildMainPendingIntent();
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_SCAN)
                        .setSmallIcon(R.drawable.ic_wifi)
                        .setContentTitle("Scanning network…")
                        .setContentText("Looking for devices on your Wi-Fi")
                        .setProgress(0, 0, true)
                        .setOngoing(true)
                        .setSilent(true)          // progress bar — always silent
                        .setContentIntent(pi)
                        .setAutoCancel(false);
        manager.notify(NOTIF_SCAN_PROGRESS, builder.build());
    }

    /**
     * Replaces the progress notification with a completion notification.
     * Respects the notifications_enabled preference:
     *   - enabled  → shows notification with sound
     *   - disabled → cancels the progress notification and shows nothing
     */
    public void showScanCompleteNotification(int deviceCount) {
        cancel(NOTIF_SCAN_PROGRESS);

        if (!areNotificationsEnabled()) {
            return;   // user turned notifications off – do nothing
        }

        PendingIntent pi = buildMainPendingIntent();

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_SCAN)
                        .setSmallIcon(R.drawable.ic_wifi)
                        .setContentTitle("Scan complete")
                        .setContentText("Found " + deviceCount + " device(s) on your network")
                        .setSound(soundUri)           // explicit sound on the notification
                        .setVibrate(new long[]{0, 250, 100, 250})
                        .setContentIntent(pi)
                        .setAutoCancel(true);
        manager.notify(NOTIF_SCAN_DONE, builder.build());
    }

    /** Alerts about a newly discovered (potentially unknown) device. */
    public void showNewDeviceAlert(String ip, String mac) {
        if (!areNotificationsEnabled()) return;

        PendingIntent pi = buildMainPendingIntent();
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ALERT)
                        .setSmallIcon(R.drawable.ic_device)
                        .setContentTitle("New device detected")
                        .setContentText("IP: " + ip + "  MAC: " + (mac != null ? mac : "unknown"))
                        .setContentIntent(pi)
                        .setAutoCancel(true);
        manager.notify(NOTIF_NEW_DEVICE, builder.build());
    }

    public void cancel(int id) {
        manager.cancel(id);
    }

    // ── private helpers ───────────────────────────────────────────────────────
    private PendingIntent buildMainPendingIntent() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}