package com.schoolcomputers.networkscanner.util;

import com.schoolcomputers.networkscanner.data.model.Device;
import java.util.List;

public class ExportUtils {
    public static String devicesToCsv(List<Device> devices) {
        StringBuilder csv = new StringBuilder("IP,Hostname,MAC,Vendor\n");
        for (Device d : devices) {
            csv.append(d.getIpAddress()).append(",")
               .append(d.getHostname()).append(",")
               .append(d.getMacAddress()).append(",")
               .append(d.getVendor()).append("\n");
        }
        return csv.toString();
    }
}
