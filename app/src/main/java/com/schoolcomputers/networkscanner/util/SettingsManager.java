package com.schoolcomputers.networkscanner.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class SettingsManager {
    private static final String PREF_NAME = "network_scanner_settings";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_SCAN_TIMEOUT = "scan_timeout";
    
    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setDarkMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
        applyTheme(enabled);
    }

    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public void setScanTimeout(int timeout) {
        prefs.edit().putInt(KEY_SCAN_TIMEOUT, timeout).apply();
    }

    public int getScanTimeout() {
        return prefs.getInt(KEY_SCAN_TIMEOUT, 1000);
    }

    public void applyTheme(boolean enabled) {
        AppCompatDelegate.setDefaultNightMode(enabled ? 
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
    
    public void initTheme() {
        applyTheme(isDarkMode());
    }
}
