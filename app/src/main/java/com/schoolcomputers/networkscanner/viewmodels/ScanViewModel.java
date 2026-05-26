package com.schoolcomputers.networkscanner.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
 * All DB operations are scoped to the currently signed-in Firebase user,
 * so each user sees only their own scan history.
 */
public class ScanViewModel extends AndroidViewModel {

    private static final String TAG = "ScanViewModel";

    public enum ScanState { IDLE, SCANNING, DONE, ERROR }

    private final ScanDao         scanDao;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final NetworkScanner  scanner    = new NetworkScanner();

    private final MutableLiveData<ScanState>    scanState    = new MutableLiveData<>(ScanState.IDLE);
    private final MutableLiveData<List<Device>> liveDevices  = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer>      scanProgress = new MutableLiveData<>(0);
    private final MutableLiveData<NetworkInfo>  networkInfo  = new MutableLiveData<>();
    private final MutableLiveData<String>       errorMessage = new MutableLiveData<>();

    private long currentScanId = -1;

    public ScanViewModel(@NonNull Application application) {
        super(application);
        scanDao = AppDatabase.getInstance(application).scanDao();
    }

    // ---- Helpers ----

    /** Returns the UID of the signed-in user, or null if nobody is signed in. */
    private String currentUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // ---- Accessors ----

    public LiveData<ScanState>    getScanState()    { return scanState; }
    public LiveData<List<Device>> getLiveDevices()  { return liveDevices; }
    public LiveData<Integer>      getScanProgress() { return scanProgress; }
    public LiveData<NetworkInfo>  getNetworkInfo()  { return networkInfo; }
    public LiveData<String>       getErrorMessage() { return errorMessage; }

    /**
     * Returns history for the currently signed-in user only.
     * Returns an empty LiveData if no user is signed in.
     */
    public LiveData<List<ScanWithDevices>> getHistory() {
        String uid = currentUid();
        if (uid == null) return new MutableLiveData<>(new ArrayList<>());
        return scanDao.getAllScansWithDevices(uid);
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

    public void startScan(String networkName) {
        if (scanState.getValue() == ScanState.SCANNING) return;

        String uid = currentUid();
        if (uid == null) {
            errorMessage.setValue("No user signed in.");
            return;
        }

        scanState.setValue(ScanState.SCANNING);
        liveDevices.setValue(new ArrayList<>());
        scanProgress.setValue(0);

        ioExecutor.execute(() -> {
            NetworkInfo info = NetworkUtils.getCurrentNetworkInfo(getApplication());
            if (info == null) {
                scanState.postValue(ScanState.ERROR);
                errorMessage.postValue("Unable to get network information.");
                return;
            }
            networkInfo.postValue(info);

            String ssid  = info.getSsid() != null ? info.getSsid() : "Unknown";
            String label = (networkName != null && !networkName.isEmpty())
                    ? networkName : ssid + " " + android.text.format.DateFormat
                            .format("dd/MM/yyyy HH:mm", System.currentTimeMillis());

            // Scope the new scan record to the current user
            ScanRecord record = new ScanRecord(uid, label, ssid, System.currentTimeMillis(), 0);
            try {
                currentScanId = scanDao.insertScan(record);
            } catch (Exception e) {
                Log.e(TAG, "Failed to insert scan record", e);
                scanState.postValue(ScanState.ERROR);
                errorMessage.postValue("Database error: " + e.getMessage());
                return;
            }

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
                        if (foundDevice == null) return;
                        List<Device> current = liveDevices.getValue();
                        if (current == null) current = new ArrayList<>();
                        current.add(foundDevice);
                        liveDevices.postValue(new ArrayList<>(current));
                        scanProgress.postValue((scanned * 100) / total);

                        ioExecutor.execute(() -> {
                            try {
                                scanDao.insertDevice(foundDevice);
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to insert device: " + foundDevice.ipAddress, e);
                            }
                        });
                    }

                    @Override
                    public void onScanComplete(List<Device> devices) {
                        ioExecutor.execute(() -> {
                            try {
                                ScanRecord sr = scanDao.getScanById(currentScanId);
                                if (sr != null) {
                                    sr.deviceCount = devices != null ? devices.size() : 0;
                                    scanDao.updateScan(sr);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to update scan record", e);
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

    /** Clears only the current user's scans. */
    public void clearHistory() {
        String uid = currentUid();
        if (uid == null) return;
        ioExecutor.execute(() -> {
            try {
                scanDao.deleteAllScansForUser(uid);
                Log.d(TAG, "History cleared for user: " + uid);
            } catch (Exception e) {
                Log.e(TAG, "Failed to clear history", e);
                errorMessage.postValue("Failed to clear history: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        scanner.cancel();
        ioExecutor.shutdown();
    }
}
