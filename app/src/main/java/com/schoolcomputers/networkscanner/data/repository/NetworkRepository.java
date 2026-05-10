package com.schoolcomputers.networkscanner.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.schoolcomputers.networkscanner.data.local.AppDatabase;
import com.schoolcomputers.networkscanner.data.local.DeviceDao;
import com.schoolcomputers.networkscanner.data.local.ScanSessionDao;
import com.schoolcomputers.networkscanner.data.model.Device;
import com.schoolcomputers.networkscanner.data.model.ScanSession;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkRepository {
    private final DeviceDao deviceDao;
    private final ScanSessionDao scanSessionDao;
    private final ExecutorService executorService;

    public NetworkRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        deviceDao = db.deviceDao();
        scanSessionDao = db.scanSessionDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insertDevice(Device device) {
        executorService.execute(() -> deviceDao.insert(device));
    }

    public void insertSession(ScanSession session, OnSessionInsertedListener listener) {
        executorService.execute(() -> {
            long id = scanSessionDao.insert(session);
            if (listener != null) {
                listener.onInserted(id);
            }
        });
    }

    public interface OnSessionInsertedListener {
        void onInserted(long sessionId);
    }

    public void updateSession(ScanSession session) {
        executorService.execute(() -> scanSessionDao.update(session));
    }

    public LiveData<List<ScanSession>> getAllSessions() {
        return scanSessionDao.getAllSessions();
    }

    public LiveData<List<Device>> getDevicesForSession(int sessionId) {
        return deviceDao.getDevicesForSession(sessionId);
    }
}
