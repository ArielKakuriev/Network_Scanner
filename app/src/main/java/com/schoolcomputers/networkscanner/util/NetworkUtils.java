package com.schoolcomputers.networkscanner.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.RouteInfo;
import android.location.LocationManager;

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

    public static boolean isLocationEnabled(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        boolean networkEnabled = false;
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ignored) {}
        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ignored) {}
        return gpsEnabled || networkEnabled;
    }

    public static String getSubnet(String ipAddress) {
        if (ipAddress == null || !ipAddress.contains(".")) return "";
        return ipAddress.substring(0, ipAddress.lastIndexOf("."));
    }

    public static String resolveHostname(String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            String hostname = addr.getCanonicalHostName();
            if (hostname != null && !hostname.equals(ip)) {
                return hostname;
            }
        } catch (Exception e) {
            // Hostname resolution failed
        }
        return "Unknown Device";
    }
}
