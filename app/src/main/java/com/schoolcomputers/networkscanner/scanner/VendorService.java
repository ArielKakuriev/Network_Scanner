package com.schoolcomputers.networkscanner.scanner;

import android.content.Context;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class VendorService {
    private static VendorService instance;
    private final Map<String, String> vendorMap = new HashMap<>();

    private VendorService(Context context) {
        loadVendorData(context);
    }

    public static synchronized VendorService getInstance(Context context) {
        if (instance == null) {
            instance = new VendorService(context.getApplicationContext());
        }
        return instance;
    }

    private void loadVendorData(Context context) {
        try {
            InputStream is = context.getAssets().open("oui_data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            
            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(json);
            
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                vendorMap.put(key.toUpperCase(), jsonObject.getString(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getVendor(String macAddress) {
        if (macAddress == null || macAddress.length() < 8) {
            return "Unknown Vendor";
        }
        
        // Convert MAC to standard format AA:BB:CC for prefix matching
        String prefix = macAddress.substring(0, 8).toUpperCase();
        return vendorMap.getOrDefault(prefix, "Unknown Vendor");
    }
}
