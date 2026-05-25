package com.schoolcomputers.networkscanner.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Simple latency (ping) measurement utility.
 * Opens a TCP socket to the target and measures round-trip time.
 * Returns -1 on failure.
 */
public class SpeedTestHelper {

    private static final int PORT    = 53;   // DNS port — widely open
    private static final int TIMEOUT = 2000; // 2s max

    private SpeedTestHelper() {}

    /**
     * Measures the latency in milliseconds to the given host.
     * Must NOT be called on the main thread.
     *
     * @param host  hostname or IP (e.g. "8.8.8.8")
     * @return latency in ms, or -1 if unreachable
     */
    public static long measureLatency(String host) {
        long start = System.currentTimeMillis();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, PORT), TIMEOUT);
            return System.currentTimeMillis() - start;
        } catch (IOException e) {
            return -1;
        }
    }
}
