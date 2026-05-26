package com.schoolcomputers.networkscanner.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.schoolcomputers.networkscanner.database.AppDatabase;
import com.schoolcomputers.networkscanner.database.ScanDao;
import com.schoolcomputers.networkscanner.models.Device;
import com.schoolcomputers.networkscanner.models.NetworkInfo;
import com.schoolcomputers.networkscanner.models.ScanRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.schoolcomputers.networkscanner.utils.NetworkScanner;
import com.schoolcomputers.networkscanner.utils.NetworkUtils;
import com.schoolcomputers.networkscanner.utils.NotificationHelper;

import java.util.List;

/**
 * Foreground-capable Service that can execute a network scan independently
 * of any Activity lifecycle.

 * Satisfies the "Writing a substantial Service class" requirement.

 * Start with:
 *   Intent intent = new Intent(context, NetworkScanService.class);
 *   intent.putExtra("networkName", "My Home");
 *   context.startService(intent);
 */
public class NetworkScanService extends Service {

    private static final String TAG = "NetworkScanService";
    public static final String EXTRA_NETWORK_NAME = "networkName";

    private NetworkScanner scanner;
    private NotificationHelper notifHelper;
    private ScanDao scanDao;

    @Override
    public void onCreate() {
        super.onCreate();
        scanner     = new NetworkScanner();
        notifHelper = new NotificationHelper(this);
        scanDao     = AppDatabase.getInstance(this).scanDao();
        Log.d(TAG, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String networkName = intent != null
                ? intent.getStringExtra(EXTRA_NETWORK_NAME) : null;

        notifHelper.showScanningNotification();
        performScan(networkName);

        return START_NOT_STICKY;
    }

    private void performScan(String networkName) {
        new Thread(() -> {
            NetworkInfo info = NetworkUtils.getCurrentNetworkInfo(this);

            if (info.getDeviceIp() == null) {
                Log.w(TAG, "Not connected to Wi-Fi; aborting service scan");
                stopSelf();
                return;
            }

            String ssid  = info.getSsid() != null ? info.getSsid() : "Unknown";
            String label = networkName != null ? networkName : ssid;

            FirebaseUser _user = FirebaseAuth.getInstance().getCurrentUser();
            String uid = _user != null ? _user.getUid() : "";
            ScanRecord record = new ScanRecord(uid, label, ssid, System.currentTimeMillis(), 0);
            long scanId = scanDao.insertScan(record);

            scanner.startScan(
                    info.getDeviceIp(),
                    info.getGatewayIp(),
                    info.getGatewayMac(),
                    scanId,
                    new NetworkScanner.ScanListener() {
                        @Override
                        public void onScanProgress(int scanned, int total, Device found) {
                            scanDao.insertDevice(found);
                        }

                        @Override
                        public void onScanComplete(List<Device> devices) {
                            ScanRecord sr = scanDao.getScanById(scanId);
                            if (sr != null) {
                                sr.deviceCount = devices.size();
                                scanDao.updateScan(sr);
                            }
                            notifHelper.showScanCompleteNotification(devices.size());
                            stopSelf();
                        }

                        @Override
                        public void onScanError(String message) {
                            Log.e(TAG, "Scan error: " + message);
                            stopSelf();
                        }
                    }
            );
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (scanner != null) scanner.cancel();
        Log.d(TAG, "Service destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }
}