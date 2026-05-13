package com.schoolcomputers.networkscanner;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import com.schoolcomputers.networkscanner.data.repository.NetworkRepository;

public class NetworkScannerApp extends Application {
    public static final String CHANNEL_ID = "scan_results_channel";
    private NetworkRepository repository;

    @Override
    public void onCreate() {
        super.onCreate();
        repository = new NetworkRepository(this);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Scan Results",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for completed network scans");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public NetworkRepository getRepository() {
        return repository;
    }
}
