package com.schoolcomputers.networkscanner.database;

/*
 * Value object returned by the GROUP BY query in {@link ScanDao#getDeviceCountPerSsid()}.
 * Not a Room entity — used only as a projection.
 */

public class SsidDeviceCount {
    public String ssid;
    public int deviceCount;
}
