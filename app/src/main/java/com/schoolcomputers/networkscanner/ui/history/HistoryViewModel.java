package com.schoolcomputers.networkscanner.ui.history;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.schoolcomputers.networkscanner.data.model.ScanSession;
import com.schoolcomputers.networkscanner.data.repository.NetworkRepository;

import java.util.List;

public class HistoryViewModel extends AndroidViewModel {
    private final NetworkRepository repository;
    private final LiveData<List<ScanSession>> allSessions;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        repository = new NetworkRepository(application);
        allSessions = repository.getAllSessions();
    }

    public LiveData<List<ScanSession>> getAllSessions() {
        return allSessions;
    }
}
