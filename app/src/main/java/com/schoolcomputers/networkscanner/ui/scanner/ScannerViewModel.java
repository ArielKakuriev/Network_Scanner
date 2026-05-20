package com.schoolcomputers.networkscanner.ui.scanner;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.schoolcomputers.networkscanner.data.model.Device;
import com.schoolcomputers.networkscanner.data.model.ScanSession;
import com.schoolcomputers.networkscanner.data.repository.NetworkRepository;
import com.schoolcomputers.networkscanner.domain.usecase.StartScanUseCase;
import com.schoolcomputers.networkscanner.scanner.NetworkScanner;
import com.schoolcomputers.networkscanner.util.NotificationHelper;
import com.schoolcomputers.networkscanner.util.SettingsManager;
import com.schoolcomputers.networkscanner.NetworkScannerApp;

import java.util.ArrayList;
import java.util.List;

public class ScannerViewModel extends AndroidViewModel {
    private final NetworkRepository repository;
    private final StartScanUseCase startScanUseCase;
    private final SettingsManager settingsManager;
    
    private final MutableLiveData<List<Device>> discoveredDevices = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isScanning = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> progress = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isWifiConnected = new MutableLiveData<>(false);
    private final MutableLiveData<String> networkName = new MutableLiveData<>("Not Connected");
    private long currentSessionId = -1;
    private ScanSession currentSession;

    public ScannerViewModel(@NonNull Application application) {
        super(application);
        NetworkScannerApp app = (NetworkScannerApp) application;
        repository = app.getRepository();
        settingsManager = app.getSettingsManager();
        startScanUseCase = new StartScanUseCase(application, repository);
    }

    public LiveData<List<Device>> getDiscoveredDevices() {
        return discoveredDevices;
    }

    public LiveData<Boolean> getIsScanning() {
        return isScanning;
    }

    public LiveData<Integer> getProgress() {
        return progress;
    }

    public LiveData<Boolean> getIsWifiConnected() {
        return isWifiConnected;
    }

    public LiveData<String> getNetworkName() {
        return networkName;
    }

    public void updateNetworkState(boolean isConnected, boolean isWifi, String name) {
        isWifiConnected.setValue(isConnected && isWifi);
        networkName.setValue(isConnected && isWifi ? name : "Disconnected");
    }

    public void startScan(String subnet) {
        discoveredDevices.setValue(new ArrayList<>());
        isScanning.setValue(true);
        progress.setValue(0);

        currentSession = new ScanSession(System.currentTimeMillis(), "Network " + subnet);
        repository.insertSession(currentSession, sessionId -> {
            currentSessionId = sessionId;
            int timeout = settingsManager.getScanTimeout();
            
            startScanUseCase.execute(subnet, timeout, new NetworkScanner.ScanCallback() {
                @Override
                public void onDeviceFound(Device device) {
                    device.setSessionId((int) currentSessionId);
                    repository.insertDevice(device);
                    
                    List<Device> current = discoveredDevices.getValue();
                    if (current != null) {
                        List<Device> updated = new ArrayList<>(current);
                        updated.add(device);
                        discoveredDevices.postValue(updated);
                    }
                }

                @Override
                public void onScanFinished() {
                    isScanning.postValue(false);
                    progress.postValue(100);
                    
                    int count = 0;
                    if (currentSession != null) {
                        List<Device> found = discoveredDevices.getValue();
                        count = found != null ? found.size() : 0;
                        currentSession.setId((int) currentSessionId);
                        currentSession.setDeviceCount(count);
                        repository.updateSession(currentSession);
                    }
                    NotificationHelper.showScanCompleteNotification(getApplication(), count);
                }

                @Override
                public void onProgressUpdate(int p) {
                    progress.postValue(p);
                }
            });
        });
    }

    public void stopScan() {
        startScanUseCase.stop();
        isScanning.setValue(false);
    }
}
