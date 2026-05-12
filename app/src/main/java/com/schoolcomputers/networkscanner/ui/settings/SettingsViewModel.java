package com.schoolcomputers.networkscanner.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {
    private final MutableLiveData<Boolean> darkMode = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> scanTimeout = new MutableLiveData<>(1000);

    public LiveData<Boolean> getDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean enabled) {
        darkMode.setValue(enabled);
    }

    public LiveData<Integer> getScanTimeout() {
        return scanTimeout;
    }

    public void setScanTimeout(int timeout) {
        scanTimeout.setValue(timeout);
    }

    public void clearHistory() {
        // Implementation for clearing history
    }
}
