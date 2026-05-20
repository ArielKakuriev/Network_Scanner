package com.schoolcomputers.networkscanner;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import com.schoolcomputers.networkscanner.data.repository.NetworkRepository;
import com.schoolcomputers.networkscanner.util.SettingsManager;

public class NetworkScannerApp extends Application {
    public static final String CHANNEL_ID = "scan_results_channel";
    private NetworkRepository repository;
    private SettingsManager settingsManager;

    @Override
    public void onCreate() {
        super.onCreate();
        settingsManager = new SettingsManager(this);
        settingsManager.initTheme();
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

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }
}
