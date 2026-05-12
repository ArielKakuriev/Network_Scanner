package com.schoolcomputers.networkscanner.scanner;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.schoolcomputers.networkscanner.data.model.Device;
import com.schoolcomputers.networkscanner.util.NetworkUtils;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkScanner {
    private static final int THREAD_POOL_SIZE = 50;
    private static final int REACHABLE_TIMEOUT = 1000;
    private static final int RETRY_COUNT = 1;

    private ExecutorService executorService;
    private final Handler mainHandler;
    private final Context context;
    private volatile boolean isScanning = false;
    private final Set<String> discoveredIps = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public interface ScanCallback {
        void onDeviceFound(Device device);
        void onScanFinished();
        void onProgressUpdate(int progress);
    }

    public NetworkScanner(Context context) {
        this.context = context.getApplicationContext();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void startScan(String subnet, ScanCallback callback) {
        if (isScanning) return;
        isScanning = true;
        discoveredIps.clear();
        
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        AtomicInteger completedTasks = new AtomicInteger(0);

        new Thread(() -> {
            for (int i = 1; i <= 254; i++) {
                if (!isScanning) break;
                
                final String host = subnet + "." + i;
                executorService.execute(() -> {
                    try {
                        if (!isScanning) return;
                        
                        boolean reachable = scanHost(host);
                        
                        // Retry once if not reachable
                        if (!reachable && isScanning) {
                            reachable = scanHost(host);
                        }

                        if (reachable && isScanning && discoveredIps.add(host)) {
                            String hostname = NetworkUtils.resolveHostname(host);
                            String mac = NetworkUtils.getMacAddressFromArp(host);
                            String vendor = VendorService.getInstance(context).getVendor(mac);
                            
                            Device device = new Device(host, mac, hostname, vendor, 0);
                            mainHandler.post(() -> callback.onDeviceFound(device));
                        }
                    } finally {
                        int completed = completedTasks.incrementAndGet();
                        mainHandler.post(() -> callback.onProgressUpdate((completed * 100) / 254));
                        
                        if (completed == 254) {
                            finishScan(callback);
                        }
                    }
                });
            }

            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(2, TimeUnit.MINUTES)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
            
            if (isScanning) {
                finishScan(callback);
            }
        }).start();
    }

    private boolean scanHost(String host) {
        try {
            InetAddress inetAddress = InetAddress.getByName(host);
            return inetAddress.isReachable(REACHABLE_TIMEOUT);
        } catch (Exception e) {
            return false;
        }
    }

    private synchronized void finishScan(ScanCallback callback) {
        if (isScanning) {
            isScanning = false;
            mainHandler.post(callback::onScanFinished);
        }
    }

    public void stopScan() {
        isScanning = false;
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
}
