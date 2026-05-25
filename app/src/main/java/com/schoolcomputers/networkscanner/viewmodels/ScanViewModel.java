package com.schoolcomputers.networkscanner.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.schoolcomputers.networkscanner.database.AppDatabase;
import com.schoolcomputers.networkscanner.database.ScanDao;
import com.schoolcomputers.networkscanner.models.Device;
import com.schoolcomputers.networkscanner.models.NetworkInfo;
import com.schoolcomputers.networkscanner.models.ScanRecord;
import com.schoolcomputers.networkscanner.models.ScanWithDevices;
import com.schoolcomputers.networkscanner.utils.NetworkScanner;
import com.schoolcomputers.networkscanner.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Central ViewModel for scan operations, history, and network info.
 * Follows MVVM: no Android context flows into Fragments/Activities.
 */
public class ScanViewModel extends AndroidViewModel {

    private static final String TAG = "ScanViewModel";

    // ---- State enums ----
    public enum ScanState { IDLE, SCANNING, DONE, ERROR }

    // ---- Dependencies ----
    private final ScanDao scanDao;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final NetworkScanner scanner = new NetworkScanner();

    // ---- LiveData ----
    private final MutableLiveData<ScanState>    scanState      = new MutableLiveData<>(ScanState.IDLE);
    private final MutableLiveData<List<Device>> liveDevices    = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer>      scanProgress   = new MutableLiveData<>(0);
    private final MutableLiveData<NetworkInfo>  networkInfo    = new MutableLiveData<>();
    private final MutableLiveData<String>       errorMessage   = new MutableLiveData<>();

    /** The Room ID of the currently active scan record, set before scan begins. */
    private long currentScanId = -1;

    public ScanViewModel(@NonNull Application application) {
        super(application);
        scanDao = AppDatabase.getInstance(application).scanDao();
    }

    // ---- Accessors ----

    public LiveData<ScanState>    getScanState()   { return scanState; }
    public LiveData<List<Device>> getLiveDevices()  { return liveDevices; }
    public LiveData<Integer>      getScanProgress() { return scanProgress; }
    public LiveData<NetworkInfo>  getNetworkInfo()  { return networkInfo; }
    public LiveData<String>       getErrorMessage() { return errorMessage; }

    public LiveData<List<ScanWithDevices>> getHistory() {
        return scanDao.getAllScansWithDevices();
    }

    public LiveData<List<Device>> getDevicesForScan(long scanId) {
        return scanDao.getDevicesForScan(scanId);
    }

    // ---- Network info refresh ----

    public void refreshNetworkInfo() {
        ioExecutor.execute(() -> {
            NetworkInfo info = NetworkUtils.getCurrentNetworkInfo(getApplication());
            networkInfo.postValue(info);
        });
    }

    // ---- Scan lifecycle ----

    /**
     * Starts a network scan.
     * Creates a ScanRecord in Room first (we need its ID for Device FKs),
     * then kicks off the subnet scanner.
     *
     * @param networkName user-supplied label (may be null → auto-generated)
     */
    public void startScan(String networkName) {
        if (scanState.getValue() == ScanState.SCANNING) return;

        scanState.setValue(ScanState.SCANNING);
        liveDevices.setValue(new ArrayList<>());
        scanProgress.setValue(0);

        ioExecutor.execute(() -> {
            NetworkInfo info = NetworkUtils.getCurrentNetworkInfo(getApplication());
            networkInfo.postValue(info);

            String ssid = info.getSsid() != null ? info.getSsid() : "Unknown";
            String label = (networkName != null && !networkName.isEmpty())
                    ? networkName : ssid + " " + android.text.format.DateFormat
                            .format("dd/MM/yyyy HH:mm", System.currentTimeMillis());

            // Insert the ScanRecord placeholder; we'll update deviceCount at the end
            ScanRecord record = new ScanRecord(label, ssid, System.currentTimeMillis(), 0);
            currentScanId = scanDao.insertScan(record);

            if (info.getDeviceIp() == null || info.getDeviceIp().isEmpty()) {
                scanState.postValue(ScanState.ERROR);
                errorMessage.postValue("Not connected to Wi-Fi");
                return;
            }

            scanner.startScan(
                info.getDeviceIp(),
                info.getGatewayIp(),
                info.getGatewayMac(),
                currentScanId,
                new NetworkScanner.ScanListener() {

                    @Override
                    public void onScanProgress(int scanned, int total, Device foundDevice) {
                        // Accumulate discovered devices
                        List<Device> current = liveDevices.getValue();
                        if (current == null) current = new ArrayList<>();
                        current.add(foundDevice);
                        liveDevices.postValue(new ArrayList<>(current));
                        scanProgress.postValue((scanned * 100) / total);

                        // Persist device to Room off main thread
                        ioExecutor.execute(() -> scanDao.insertDevice(foundDevice));
                    }

                    @Override
                    public void onScanComplete(List<Device> devices) {
                        // Update the device count on the ScanRecord
                        ioExecutor.execute(() -> {
                            ScanRecord sr = scanDao.getScanById(currentScanId);
                            if (sr != null) {
                                sr.deviceCount = devices.size();
                                scanDao.updateScan(sr);
                            }
                        });
                        scanProgress.postValue(100);
                        scanState.postValue(ScanState.DONE);
                    }

                    @Override
                    public void onScanError(String message) {
                        scanState.postValue(ScanState.ERROR);
                        errorMessage.postValue(message);
                    }
                }
            );
        });
    }

    public void cancelScan() {
        scanner.cancel();
        scanState.setValue(ScanState.IDLE);
    }

    // ---- History actions ----

    public void clearHistory() {
        ioExecutor.execute(scanDao::deleteAllScans);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        scanner.cancel();
        ioExecutor.shutdown();
    }
}
