package com.schoolcomputers.networkscanner.scanner;

import java.util.HashMap;
import java.util.Map;

public class VendorService {
    private static final Map<String, String> OUI_MAP = new HashMap<>();

    static {
        OUI_MAP.put("00:50:56", "VMware");
        OUI_MAP.put("00:0C:29", "VMware");
        OUI_MAP.put("3C:5A:B4", "Google");
        OUI_MAP.put("B8:27:EB", "Raspberry Pi");
        OUI_MAP.put("00:1A:11", "Google");
        OUI_MAP.put("00:26:BB", "Apple");
        OUI_MAP.put("D8:E0:E1", "Samsung");
        // Simplified list for demonstration
    }

    public static String getVendor(String macAddress) {
        if (macAddress == null || macAddress.length() < 8) return "Unknown";
        // Convert MAC to standard OUI format (XX:XX:XX)
        String prefix = macAddress.substring(0, 8).toUpperCase();
        return OUI_MAP.getOrDefault(prefix, "Unknown Vendor");
    }
}
