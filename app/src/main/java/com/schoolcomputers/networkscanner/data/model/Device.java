package com.schoolcomputers.networkscanner.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "devices",
    foreignKeys = @ForeignKey(
        entity = ScanSession.class,
        parentColumns = "id",
        childColumns = "sessionId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index("sessionId"),
        @Index("ipAddress"),
        @Index("hostname")
    }
)
public class Device {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String ipAddress;
    private String macAddress;
    private String hostname;
    private String vendor;
    private long timestamp;
    private int sessionId;
    private String openPorts;
    private long responseTime;
    private String deviceType;
    private boolean isGateway;
    private boolean isLocalDevice;

    public Device(String ipAddress, String macAddress, String hostname, String vendor, int sessionId) {
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.hostname = hostname;
        this.vendor = vendor;
        this.sessionId = sessionId;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getMacAddress() { return macAddress; }
    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }
    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }
    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }
    public String getOpenPorts() { return openPorts; }
    public void setOpenPorts(String openPorts) { this.openPorts = openPorts; }
    public long getResponseTime() { return responseTime; }
    public void setResponseTime(long responseTime) { this.responseTime = responseTime; }
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    public boolean isGateway() { return isGateway; }
    public void setGateway(boolean gateway) { isGateway = gateway; }
    public boolean isLocalDevice() { return isLocalDevice; }
    public void setLocalDevice(boolean localDevice) { isLocalDevice = localDevice; }
}
