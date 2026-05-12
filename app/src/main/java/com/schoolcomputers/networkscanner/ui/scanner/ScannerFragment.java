package com.schoolcomputers.networkscanner.ui.scanner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.schoolcomputers.networkscanner.R;
import com.schoolcomputers.networkscanner.data.model.Device;
import com.schoolcomputers.networkscanner.util.NetworkUtils;
import android.widget.TextView;

public class ScannerFragment extends Fragment {
    private ScannerViewModel viewModel;
    private DeviceAdapter adapter;
    private MaterialButton btnScan;
    private LinearProgressIndicator progressIndicator;

    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                Boolean fineLocation = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                if (fineLocation != null && fineLocation) {
                    startScan();
                } else {
                    Toast.makeText(getContext(), "Location permission is required for scanning", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        btnScan = view.findViewById(R.id.btnScan);
        progressIndicator = view.findViewById(R.id.progressIndicator);
        RecyclerView rvDevices = view.findViewById(R.id.rvDevices);
        
        adapter = new DeviceAdapter();
        rvDevices.setAdapter(adapter);

        adapter.setOnDeviceClickListener(this::showDeviceDetails);
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ScannerViewModel.class);

        btnScan.setOnClickListener(v -> checkPermissionsAndScan());

        viewModel.getDiscoveredDevices().observe(getViewLifecycleOwner(), devices -> {
            adapter.setDevices(devices);
        });

        viewModel.getIsScanning().observe(getViewLifecycleOwner(), isScanning -> {
            btnScan.setEnabled(!isScanning);
            btnScan.setText(isScanning ? "Scanning..." : "Start Scan");
            progressIndicator.setVisibility(isScanning ? View.VISIBLE : View.INVISIBLE);
        });

        viewModel.getProgress().observe(getViewLifecycleOwner(), progress -> {
            progressIndicator.setProgress(progress);
        });
    }

    private void checkPermissionsAndScan() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startScan();
        } else {
            permissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void startScan() {
        String ip = NetworkUtils.getLocalIpAddress(requireContext());
        if (ip.equals("0.0.0.0")) {
            Toast.makeText(getContext(), "Please connect to Wi-Fi", Toast.LENGTH_SHORT).show();
            return;
        }
        String subnet = NetworkUtils.getSubnet(ip);
        viewModel.startScan(subnet);
    }

    private void showDeviceDetails(Device device) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_device_details, null);
        
        ((TextView) dialogView.findViewById(R.id.detailIp)).setText(device.getIpAddress());
        ((TextView) dialogView.findViewById(R.id.detailMac)).setText(device.getMacAddress());
        ((TextView) dialogView.findViewById(R.id.detailHostname)).setText(
                device.getHostname() != null && !device.getHostname().isEmpty() ? device.getHostname() : "N/A");
        ((TextView) dialogView.findViewById(R.id.detailVendor)).setText(
                device.getVendor() != null && !device.getVendor().isEmpty() ? device.getVendor() : "Unknown");

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
}
