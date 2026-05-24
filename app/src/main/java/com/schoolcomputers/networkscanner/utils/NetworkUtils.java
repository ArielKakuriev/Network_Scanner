package com.networkscanner.utils;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import com.networkscanner.models.NetworkInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Utility class for network information retrieval.
 * Encapsulates WifiManager and ARP table logic.
 */
public class NetworkUtils {

    private static final String TAG = "NetworkUtils";
    private static final String ARP_TABLE_PATH = "/proc/net/arp";

    /**
     * Builds a {@link NetworkInfo} snapshot from the current Wi-Fi state.
     * Must NOT be called on the main thread (reads /proc/net/arp).
     */
    public static NetworkInfo getCurrentNetworkInfo(Context context) {
        NetworkInfo info = new NetworkInfo();

        WifiManager wifiManager =
                (WifiManager) context.getApplicationContext()
                        .getSystemService(Context.WIFI_SERVICE);

        if (wifiManager == null || !wifiManager.isWifiEnabled()) {
            return info;
        }

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            String ssid = wifiInfo.getSSID();
            // Remove surrounding quotes Android adds
            if (ssid != null && ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
            info.setSsid(ssid);
            info.setBssid(wifiInfo.getBSSID());
            info.setLinkSpeedMbps(wifiInfo.getLinkSpeed());
            info.setSignalLevel(WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5));

            // Device IP
            int ipInt = wifiInfo.getIpAddress();
            info.setDeviceIp(Formatter.formatIpAddress(ipInt));
        }

        // Gateway IP from DHCP
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        if (dhcpInfo != null) {
            info.setGatewayIp(Formatter.formatIpAddress(dhcpInfo.gateway));
        }

        // Device MAC from NetworkInterface
        info.setDeviceMac(getDeviceMacAddress());

        // Gateway MAC from ARP table
        if (info.getGatewayIp() != null) {
            info.setGatewayMac(getMacFromArp(info.getGatewayIp()));
        }

        return info;
    }

    /**
     * Reads the MAC address of the wlan0 interface.
     */
    public static String getDeviceMacAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.getName().equalsIgnoreCase("wlan0")) {
                    byte[] mac = iface.getHardwareAddress();
                    if (mac == null) return "02:00:00:00:00:00";
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i],
                                (i < mac.length - 1) ? ":" : ""));
                    }
                    return sb.toString();
                }
            }
        } catch (SocketException e) {
            Log.e(TAG, "getDeviceMacAddress: " + e.getMessage());
        }
        return "02:00:00:00:00:00";
    }

    /**
     * Looks up a MAC address for a given IP in /proc/net/arp.
     * Returns null if not found.
     */
    public static String getMacFromArp(String ipAddress) {
        try (BufferedReader reader = new BufferedReader(new FileReader(ARP_TABLE_PATH))) {
            String line;
            reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 4 && parts[0].equals(ipAddress)) {
                    String mac = parts[3];
                    // "00:00:00:00:00:00" means unresolved
                    if (!mac.equals("00:00:00:00:00:00")) {
                        return mac.toUpperCase();
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "getMacFromArp: " + e.getMessage());
        }
        return null;
    }

    /**
     * Converts an IPv4 address string to an integer for subnet arithmetic.
     */
    public static int ipToInt(String ipAddress) {
        String[] parts = ipAddress.split("\\.");
        int result = 0;
        for (int i = 0; i < 4; i++) {
            result |= Integer.parseInt(parts[i]) << (24 - (8 * i));
        }
        return result;
    }

    /**
     * Converts an integer back to a dotted-decimal IPv4 string.
     */
    public static String intToIp(int ip) {
        return ((ip >> 24) & 0xFF) + "." +
               ((ip >> 16) & 0xFF) + "." +
               ((ip >> 8) & 0xFF) + "." +
               (ip & 0xFF);
    }

    /**
     * Derives the network prefix from a device IP.
     * For a /24 like 192.168.1.x returns "192.168.1."
     */
    public static String getNetworkPrefix(String deviceIp) {
        int lastDot = deviceIp.lastIndexOf('.');
        if (lastDot < 0) return "";
        return deviceIp.substring(0, lastDot + 1);
    }

    /**
     * Returns true if the device is currently connected to Wi-Fi.
     */
    public static boolean isWifiConnected(Context context) {
        WifiManager wm =
                (WifiManager) context.getApplicationContext()
                        .getSystemService(Context.WIFI_SERVICE);
        if (wm == null) return false;
        WifiInfo info = wm.getConnectionInfo();
        return wm.isWifiEnabled() && info != null && info.getNetworkId() != -1;
    }

    /**
     * Attempts a reachability check on the given IP with a 300 ms timeout.
     */
    public static boolean isReachable(String ip) {
        try {
            return InetAddress.getByName(ip).isReachable(300);
        } catch (IOException e) {
            return false;
        }
    }
}
