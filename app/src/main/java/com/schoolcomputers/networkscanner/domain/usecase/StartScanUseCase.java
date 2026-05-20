package com.schoolcomputers.networkscanner.domain.usecase;

import android.content.Context;
import com.schoolcomputers.networkscanner.data.repository.NetworkRepository;
import com.schoolcomputers.networkscanner.scanner.NetworkScanner;

public class StartScanUseCase {
    private final NetworkScanner scanner;
    private final NetworkRepository repository;

    public StartScanUseCase(Context context, NetworkRepository repository) {
        this.repository = repository;
        this.scanner = new NetworkScanner(context);
    }

    public void execute(String subnet, int timeout, NetworkScanner.ScanCallback callback) {
        scanner.setReachableTimeout(timeout);
        scanner.startScan(subnet, callback);
    }
    
    public void stop() {
        scanner.stopScan();
    }
}
