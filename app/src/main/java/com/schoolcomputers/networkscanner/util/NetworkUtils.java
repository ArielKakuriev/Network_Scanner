package com.schoolcomputers.networkscanner.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;

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

    public static String getSubnet(String ipAddress) {
        if (ipAddress == null || !ipAddress.contains(".")) return "";
        return ipAddress.substring(0, ipAddress.lastIndexOf("."));
    }
}
