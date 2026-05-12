package com.schoolcomputers.networkscanner.scanner;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PortScanner {
    private static final int[] COMMON_PORTS = {
            21, 22, 23, 25, 53, 80, 110, 135, 139, 143, 443, 445, 3389, 8080
    };
    private static final int TIMEOUT_MS = 200;
    private static final int THREAD_POOL_SIZE = 10;

    public interface PortScanCallback {
        void onPortScanFinished(List<Integer> openPorts);
    }

    public void scanPorts(String ip, PortScanCallback callback) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<Integer> openPorts = Collections.synchronizedList(new ArrayList<>());

        for (int port : COMMON_PORTS) {
            executor.execute(() -> {
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(ip, port), TIMEOUT_MS);
                    openPorts.add(port);
                } catch (Exception ignored) {
                    // Port is closed or filtered
                }
            });
        }

        executor.shutdown();
        new Thread(() -> {
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            callback.onPortScanFinished(new ArrayList<>(openPorts));
        }).start();
    }
}
