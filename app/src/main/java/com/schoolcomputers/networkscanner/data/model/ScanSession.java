package com.schoolcomputers.networkscanner.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "scan_sessions")
public class ScanSession {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private long startTime;
    private int deviceCount;
    private String networkName;

    public ScanSession(long startTime, String networkName) {
        this.startTime = startTime;
        this.networkName = networkName;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public int getDeviceCount() { return deviceCount; }
    public void setDeviceCount(int deviceCount) { this.deviceCount = deviceCount; }
    public String getNetworkName() { return networkName; }
    public void setNetworkName(String networkName) { this.networkName = networkName; }
}
