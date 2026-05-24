package com.networkscanner.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Represents a device discovered on a network scan.
 * Each device belongs to exactly one ScanRecord (network scan session).
 */
@Entity(
    tableName = "devices",
    foreignKeys = @ForeignKey(
        entity = ScanRecord.class,
        parentColumns = "id",
        childColumns = "scanId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("scanId")}
)
public class Device {

    @PrimaryKey(autoGenerate = true)
    public long id;

    /** FK to the parent ScanRecord */
    public long scanId;

    /** Human-readable hostname (may be null if not resolvable) */
    public String hostname;

    /** IPv4 address of this device */
    public String ipAddress;

    /** MAC address (may be null on Android 10+ for non-router devices) */
    public String macAddress;

    /** Router / gateway IPv4 */
    public String routerIp;

    /** Router / gateway MAC */
    public String routerMac;

    /** Timestamp when this device was first seen in this scan (epoch ms) */
    public long discoveredAt;

    /** Whether the ping/connect succeeded */
    public boolean isReachable;

    public Device() {}

    public Device(long scanId, String hostname, String ipAddress, String macAddress,
                  String routerIp, String routerMac, long discoveredAt, boolean isReachable) {
        this.scanId = scanId;
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.routerIp = routerIp;
        this.routerMac = routerMac;
        this.discoveredAt = discoveredAt;
        this.isReachable = isReachable;
    }

    @Override
    public String toString() {
        return "Device{ip=" + ipAddress + ", mac=" + macAddress + "}";
    }
}
