package com.schoolcomputers.networkscanner.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

public class NetworkStateReceiver extends BroadcastReceiver {

    public interface NetworkStateListener {
        void onNetworkStateChanged(boolean isConnected, boolean isWifi, String networkName);
    }

    private final NetworkStateListener listener;

    public NetworkStateReceiver(NetworkStateListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (listener == null) return;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        boolean isWifi = isConnected && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        
        String networkName;
        if (isWifi) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            String ssid = (wifiManager != null && wifiManager.getConnectionInfo() != null) 
                    ? wifiManager.getConnectionInfo().getSSID() : "Unknown";
            
            // Remove quotes from SSID if present
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                networkName = ssid.substring(1, ssid.length() - 1);
            } else {
                networkName = ssid;
            }
        } else {
            networkName = "Not a Wi-Fi Network";
        }

        listener.onNetworkStateChanged(isConnected, isWifi, networkName);
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        return filter;
    }
}
