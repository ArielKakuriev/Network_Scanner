package com.schoolcomputers.networkscanner.scanner;

import android.os.Handler;
import android.os.Looper;

import com.schoolcomputers.networkscanner.data.model.Device;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NetworkScanner {
    private ExecutorService executorService;
    private final Handler mainHandler;
    private boolean isScanning = false;

    public interface ScanCallback {
        void onDeviceFound(Device device);
        void onScanFinished();
        void onProgressUpdate(int progress);
    }

    public NetworkScanner() {
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void startScan(String subnet, ScanCallback callback) {
        if (isScanning) return;
        isScanning = true;
        executorService = Executors.newFixedThreadPool(20);

        new Thread(() -> {
            for (int i = 1; i < 255; i++) {
                final String host = subnet + "." + i;
                final int finalI = i;
                executorService.execute(() -> {
                    try {
                        InetAddress inetAddress = InetAddress.getByName(host);
                        if (inetAddress.isReachable(1000)) {
                            String hostname = inetAddress.getCanonicalHostName();
                            Device device = new Device(host, "Unknown", hostname, "Unknown", 0);
                            mainHandler.post(() -> callback.onDeviceFound(device));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mainHandler.post(() -> callback.onProgressUpdate((finalI * 100) / 254));
                });
            }

            executorService.shutdown();
            try {
                executorService.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            isScanning = false;
            mainHandler.post(callback::onScanFinished);
        }).start();
    }

    public void stopScan() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
        isScanning = false;
    }
}
