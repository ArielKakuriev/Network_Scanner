package com.schoolcomputers.networkscanner.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.schoolcomputers.networkscanner.MainActivity;
import com.schoolcomputers.networkscanner.NetworkScannerApp;
import com.schoolcomputers.networkscanner.R;

public class NotificationHelper {

    private static final int NOTIFICATION_ID = 1001;

    public static void showScanCompleteNotification(Context context, int deviceCount) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 
                0, 
                intent, 
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NetworkScannerApp.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Using existing drawable
                .setContentTitle("Scan Complete")
                .setContentText("Found " + deviceCount + " devices on your network.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        
        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        } catch (SecurityException e) {
            // This happens if POST_NOTIFICATIONS permission is not granted on Android 13+
            e.printStackTrace();
        }
    }
}
