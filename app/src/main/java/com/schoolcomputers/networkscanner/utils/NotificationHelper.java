package com.networkscanner.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.networkscanner.R;
import com.networkscanner.activities.MainActivity;

/**
 * Centralises all notification creation for the app.
 * Creates channels on first use (Android 8+).
 */
public class NotificationHelper {

    public static final String CHANNEL_SCAN   = "channel_scan";
    public static final String CHANNEL_ALERT  = "channel_alert";

    public static final int NOTIF_SCAN_PROGRESS = 1001;
    public static final int NOTIF_SCAN_DONE     = 1002;
    public static final int NOTIF_NEW_DEVICE    = 1003;

    private final Context context;
    private final NotificationManager manager;

    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.manager = (NotificationManager) this.context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        createChannels();
    }

    private void createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel scanChannel = new NotificationChannel(
                    CHANNEL_SCAN, "Scan Progress",
                    NotificationManager.IMPORTANCE_LOW);
            scanChannel.setDescription("Shows progress while scanning the network");

            NotificationChannel alertChannel = new NotificationChannel(
                    CHANNEL_ALERT, "Network Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT);
            alertChannel.setDescription("Alerts about new or unknown devices");

            manager.createNotificationChannel(scanChannel);
            manager.createNotificationChannel(alertChannel);
        }
    }

    /** Builds and shows a scan-progress notification (indeterminate). */
    public void showScanningNotification() {
        PendingIntent pi = buildMainPendingIntent();
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_SCAN)
                        .setSmallIcon(R.drawable.ic_wifi)
                        .setContentTitle("Scanning network…")
                        .setContentText("Looking for devices on your Wi-Fi")
                        .setProgress(0, 0, true)
                        .setOngoing(true)
                        .setContentIntent(pi)
                        .setAutoCancel(false);
        manager.notify(NOTIF_SCAN_PROGRESS, builder.build());
    }

    /** Replaces the progress notification with a completion notification. */
    public void showScanCompleteNotification(int deviceCount) {
        cancel(NOTIF_SCAN_PROGRESS);
        PendingIntent pi = buildMainPendingIntent();
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_SCAN)
                        .setSmallIcon(R.drawable.ic_wifi)
                        .setContentTitle("Scan complete")
                        .setContentText("Found " + deviceCount + " device(s) on your network")
                        .setContentIntent(pi)
                        .setAutoCancel(true);
        manager.notify(NOTIF_SCAN_DONE, builder.build());
    }

    /** Alerts about a newly discovered (potentially unknown) device. */
    public void showNewDeviceAlert(String ip, String mac) {
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

    private PendingIntent buildMainPendingIntent() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
