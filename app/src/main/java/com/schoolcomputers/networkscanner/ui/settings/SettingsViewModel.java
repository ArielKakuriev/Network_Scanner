package com.schoolcomputers.networkscanner.ui.settings;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.schoolcomputers.networkscanner.data.repository.NetworkRepository;
import com.schoolcomputers.networkscanner.util.SettingsManager;
import com.schoolcomputers.networkscanner.NetworkScannerApp;

public class SettingsViewModel extends AndroidViewModel {
    private final SettingsManager settingsManager;
    private final NetworkRepository repository;
    private final MutableLiveData<Boolean> darkMode = new MutableLiveData<>();
    private final MutableLiveData<Integer> scanTimeout = new MutableLiveData<>();

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        NetworkScannerApp app = (NetworkScannerApp) application;
        settingsManager = app.getSettingsManager();
        repository = app.getRepository();
        darkMode.setValue(settingsManager.isDarkMode());
        scanTimeout.setValue(settingsManager.getScanTimeout());
    }

    public LiveData<Boolean> getDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean enabled) {
        settingsManager.setDarkMode(enabled);
        darkMode.setValue(enabled);
    }

    public LiveData<Integer> getScanTimeout() {
        return scanTimeout;
    }

    public void setScanTimeout(int timeout) {
        settingsManager.setScanTimeout(timeout);
        scanTimeout.setValue(timeout);
    }

    public void clearHistory() {
        repository.deleteAllHistory();
    }
}
