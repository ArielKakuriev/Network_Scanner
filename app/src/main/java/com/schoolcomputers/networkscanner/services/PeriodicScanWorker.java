package com.schoolcomputers.networkscanner.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.schoolcomputers.networkscanner.database.AppDatabase;
import com.schoolcomputers.networkscanner.database.ScanDao;
import com.schoolcomputers.networkscanner.models.Device;
import com.schoolcomputers.networkscanner.models.NetworkInfo;
import com.schoolcomputers.networkscanner.models.ScanRecord;
import com.schoolcomputers.networkscanner.utils.NetworkScanner;
import com.schoolcomputers.networkscanner.utils.NetworkUtils;
import com.schoolcomputers.networkscanner.utils.NotificationHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * WorkManager worker that performs a periodic network scan in the background.

 * Satisfies the "Job Scheduler / Work Manager" requirement.

 * Schedule with:
 *   PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(
 *       PeriodicScanWorker.class, 15, TimeUnit.MINUTES).build();
 *   WorkManager.getInstance(context).enqueueUniquePeriodicWork(
 *       "periodic_scan", ExistingPeriodicWorkPolicy.KEEP, req);
 */
public class PeriodicScanWorker extends Worker {

    private static final String TAG = "PeriodicScanWorker";

    public PeriodicScanWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Periodic scan started");
        Context ctx = getApplicationContext();

        if (!NetworkUtils.isWifiConnected(ctx)) {
            Log.d(TAG, "Not on Wi-Fi; skipping periodic scan");
            return Result.success();
        }

        NotificationHelper notifHelper = new NotificationHelper(ctx);
        ScanDao scanDao = AppDatabase.getInstance(ctx).scanDao();
        NetworkInfo info = NetworkUtils.getCurrentNetworkInfo(ctx);

        if (info.getDeviceIp() == null) return Result.retry();

        String ssid  = info.getSsid() != null ? info.getSsid() : "Unknown";
        ScanRecord record = new ScanRecord("Auto: " + ssid,
                ssid, System.currentTimeMillis(), 0);
        long scanId = scanDao.insertScan(record);

        // Use a latch to block doWork() until the async scanner finishes
        CountDownLatch latch = new CountDownLatch(1);
        List<Device> found = new ArrayList<>();

        NetworkScanner scanner = new NetworkScanner();
        scanner.startScan(
            info.getDeviceIp(),
            info.getGatewayIp(),
            info.getGatewayMac(),
            scanId,
            new NetworkScanner.ScanListener() {
                @Override
                public void onScanProgress(int scanned, int total, Device device) {
                    scanDao.insertDevice(device);
                    found.add(device);
                }
                @Override
                public void onScanComplete(List<Device> devices) {
                    ScanRecord sr = scanDao.getScanById(scanId);
                    if (sr != null) { sr.deviceCount = devices.size(); scanDao.updateScan(sr); }
                    notifHelper.showScanCompleteNotification(devices.size());
                    latch.countDown();
                }
                @Override
                public void onScanError(String message) {
                    Log.e(TAG, "Scan error: " + message);
                    latch.countDown();
                }
            }
        );

        try {
            // Wait max 3 minutes for the scan to finish
            latch.await(3, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            scanner.cancel();
            return Result.retry();
        }

        Log.d(TAG, "Periodic scan complete. Found: " + found.size());
        return Result.success();
    }
}
