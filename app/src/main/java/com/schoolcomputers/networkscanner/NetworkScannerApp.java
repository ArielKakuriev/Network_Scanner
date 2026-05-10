package com.schoolcomputers.networkscanner;

import android.app.Application;
import com.schoolcomputers.networkscanner.data.repository.NetworkRepository;

public class NetworkScannerApp extends Application {
    private NetworkRepository repository;

    @Override
    public void onCreate() {
        super.onCreate();
        repository = new NetworkRepository(this);
    }

    public NetworkRepository getRepository() {
        return repository;
    }
}
