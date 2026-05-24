package com.networkscanner.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Represents one network scan session.
 * Has a 1-to-many relationship with {@link Device}.
 */
@Entity(tableName = "scan_records")
public class ScanRecord {

    @PrimaryKey(autoGenerate = true)
    public long id;

    /** User-supplied or auto-generated label for this network scan */
    public String networkName;

    /** SSID of the Wi-Fi network at scan time */
    public String ssid;

    /** Timestamp when the scan was performed (epoch ms) */
    public long scannedAt;

    /** Total devices found */
    public int deviceCount;

    public ScanRecord() {}

    public ScanRecord(String networkName, String ssid, long scannedAt, int deviceCount) {
        this.networkName = networkName;
        this.ssid = ssid;
        this.scannedAt = scannedAt;
        this.deviceCount = deviceCount;
    }
}
