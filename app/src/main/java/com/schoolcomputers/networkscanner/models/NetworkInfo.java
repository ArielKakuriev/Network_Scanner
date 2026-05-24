package com.networkscanner.models;

/**
 * Snapshot of the current Wi-Fi network state.
 * Built by {@link com.networkscanner.utils.NetworkUtils} and exposed via ViewModel LiveData.
 */
public class NetworkInfo {

    private String ssid;
    private String bssid;
    private String gatewayIp;
    private String gatewayMac;
    private String deviceIp;
    private String deviceMac;
    private int linkSpeedMbps;
    private int signalLevel;          // 0-4
    private int connectedDeviceCount; // populated after scan

    public NetworkInfo() {}

    // ---- Getters & Setters ----

    public String getSsid() { return ssid; }
    public void setSsid(String ssid) { this.ssid = ssid; }

    public String getBssid() { return bssid; }
    public void setBssid(String bssid) { this.bssid = bssid; }

    public String getGatewayIp() { return gatewayIp; }
    public void setGatewayIp(String gatewayIp) { this.gatewayIp = gatewayIp; }

    public String getGatewayMac() { return gatewayMac; }
    public void setGatewayMac(String gatewayMac) { this.gatewayMac = gatewayMac; }

    public String getDeviceIp() { return deviceIp; }
    public void setDeviceIp(String deviceIp) { this.deviceIp = deviceIp; }

    public String getDeviceMac() { return deviceMac; }
    public void setDeviceMac(String deviceMac) { this.deviceMac = deviceMac; }

    public int getLinkSpeedMbps() { return linkSpeedMbps; }
    public void setLinkSpeedMbps(int linkSpeedMbps) { this.linkSpeedMbps = linkSpeedMbps; }

    public int getSignalLevel() { return signalLevel; }
    public void setSignalLevel(int signalLevel) { this.signalLevel = signalLevel; }

    public int getConnectedDeviceCount() { return connectedDeviceCount; }
    public void setConnectedDeviceCount(int count) { this.connectedDeviceCount = count; }
}
