package com.schoolcomputers.networkscanner.domain.usecase;

import com.schoolcomputers.networkscanner.data.repository.NetworkRepository;
import com.schoolcomputers.networkscanner.scanner.NetworkScanner;

public class StartScanUseCase {
    private final NetworkScanner scanner;
    private final NetworkRepository repository;

    public StartScanUseCase(NetworkRepository repository) {
        this.repository = repository;
        this.scanner = new NetworkScanner();
    }

    public void execute(String subnet, NetworkScanner.ScanCallback callback) {
        scanner.startScan(subnet, callback);
    }
    
    public void stop() {
        scanner.stopScan();
    }
}
