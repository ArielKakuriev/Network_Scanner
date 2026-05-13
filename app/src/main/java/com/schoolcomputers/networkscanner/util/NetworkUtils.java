package com.schoolcomputers.networkscanner.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.RouteInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.Inet4Address;
import java.net.InetAddress;

public class NetworkUtils {

    public static String getLocalIpAddress(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return "0.0.0.0";

        Network activeNetwork = cm.getActiveNetwork();
        if (activeNetwork == null) return "0.0.0.0";

        LinkProperties lp = cm.getLinkProperties(activeNetwork);
        if (lp == null) return "0.0.0.0";

        for (LinkAddress linkAddress : lp.getLinkAddresses()) {
            InetAddress address = linkAddress.getAddress();
            if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                return address.getHostAddress();
            }
        }
        return "0.0.0.0";
    }

    public static String getGatewayIpAddress(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return null;

        Network activeNetwork = cm.getActiveNetwork();
        if (activeNetwork == null) return null;

        LinkProperties lp = cm.getLinkProperties(activeNetwork);
        if (lp == null) return null;

        for (RouteInfo route : lp.getRoutes()) {
            if (route.isDefaultRoute() && route.getGateway() instanceof Inet4Address) {
                return route.getGateway().getHostAddress();
            }
        }
        return null;
    }

    public static String getSubnet(String ipAddress) {
        if (ipAddress == null || !ipAddress.contains(".")) return "";
        return ipAddress.substring(0, ipAddress.lastIndexOf("."));
    }

    /**
     * Retrieves the MAC address for a given IP address by parsing the ARP cache.
     * Note: Access to /proc/net/arp is restricted on Android 10 (API 29) and above.
     */
    public static String getMacAddressFromArp(String ip) {
        if (ip == null) return "00:00:00:00:00:00";
        try (BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(ip)) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 4) {
                        String mac = parts[3];
                        if (mac.matches("..:..:..:..:..:..") && !mac.equals("00:00:00:00:00:00")) {
                            return mac.toUpperCase();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "00:00:00:00:00:00";
    }

    public static String resolveHostname(String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            String hostname = addr.getCanonicalHostName();
            if (hostname != null && !hostname.equals(ip)) {
                return hostname;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown Device";
    }
}
