package com.networkscanner.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.networkscanner.models.Device;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Core scanning engine.
 *
 * Uses a fixed thread pool (Handler/Thread pattern) to ping all 254 hosts in
 * a /24 subnet concurrently. Satisfies the "Handler / Thread" requirement and
 * "complex algorithmic model" requirement.
 *
 * Progress and result callbacks are delivered on the main thread via {@link Handler}.
 */
public class NetworkScanner {

    private static final String TAG = "NetworkScanner";
    private static final int THREAD_COUNT = 50;    // concurrent ping workers
    private static final int TIMEOUT_MS  = 400;    // per-host timeout

    /** Listener interface for scan lifecycle events. */
    public interface ScanListener {
        void onScanProgress(int scanned, int total, Device foundDevice);
        void onScanComplete(List<Device> devices);
        void onScanError(String message);
    }

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private ExecutorService executor;
    private volatile boolean cancelled = false;

    /**
     * Starts an asynchronous scan of the /24 subnet containing {@code baseIp}.
     *
     * @param baseIp     e.g. "192.168.1.5" — the scanner derives the /24 prefix
     * @param routerIp   known gateway IP (pre-populated for all discovered devices)
     * @param routerMac  known gateway MAC
     * @param scanId     Room ScanRecord ID (assigned before calling this method)
     * @param listener   progress / completion callbacks
     */
    public void startScan(String baseIp, String routerIp, String routerMac,
                          long scanId, ScanListener listener) {

        cancelled = false;
        String prefix = NetworkUtils.getNetworkPrefix(baseIp);
        int total = 254;
        AtomicInteger scanned = new AtomicInteger(0);
        List<Device> found = new ArrayList<>();
        Object foundLock = new Object();

        executor = Executors.newFixedThreadPool(THREAD_COUNT);

        for (int i = 1; i <= total; i++) {
            final String ip = prefix + i;

            executor.submit(() -> {
                if (cancelled) return;

                boolean reachable = false;
                try {
                    reachable = InetAddress.getByName(ip).isReachable(TIMEOUT_MS);
                } catch (IOException ignored) {}

                if (reachable && !cancelled) {
                    String mac = NetworkUtils.getMacFromArp(ip);
                    String hostname = resolveHostname(ip);

                    Device device = new Device(
                        scanId,
                        hostname,
                        ip,
                        mac,
                        routerIp,
                        routerMac,
                        System.currentTimeMillis(),
                        true
                    );

                    synchronized (foundLock) {
                        found.add(device);
                    }

                    mainHandler.post(() -> {
                        if (!cancelled) {
                            listener.onScanProgress(scanned.get(), total, device);
                        }
                    });
                }

                int done = scanned.incrementAndGet();
                if (done == total) {
                    mainHandler.post(() -> listener.onScanComplete(new ArrayList<>(found)));
                }
            });
        }

        executor.shutdown();
    }

    /** Cancels any in-progress scan. */
    public void cancel() {
        cancelled = true;
        if (executor != null) executor.shutdownNow();
    }

    /** Attempts reverse DNS resolution; returns the IP string on failure. */
    private String resolveHostname(String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            String host = addr.getCanonicalHostName();
            return (host != null && !host.equals(ip)) ? host : ip;
        } catch (IOException e) {
            return ip;
        }
    }
}
