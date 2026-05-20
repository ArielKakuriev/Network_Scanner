package com.schoolcomputers.networkscanner.ui.scanner;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.schoolcomputers.networkscanner.R;
import com.schoolcomputers.networkscanner.data.model.Device;
import com.schoolcomputers.networkscanner.util.NetworkUtils;
import com.schoolcomputers.networkscanner.util.NetworkStateReceiver;
import com.schoolcomputers.networkscanner.util.PermissionManager;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.FrameLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.util.stream.Collectors;

public class ScannerFragment extends Fragment {
    private ScannerViewModel viewModel;
    private DeviceAdapter adapter;
    private MaterialButton btnScan;
    private LinearProgressIndicator progressIndicator;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout emptyState;
    private TextView tvNetworkName;
    private TextView tvIpAddress;
    private NetworkStateReceiver networkStateReceiver;
    private PermissionManager permissionManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionManager = new PermissionManager(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        btnScan = view.findViewById(R.id.btnScan);
        progressIndicator = view.findViewById(R.id.progressIndicator);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        emptyState = view.findViewById(R.id.emptyState);
        tvNetworkName = view.findViewById(R.id.tvNetworkName);
        tvIpAddress = view.findViewById(R.id.tvIpAddress);
        RecyclerView rvDevices = view.findViewById(R.id.rvDevices);
        
        adapter = new DeviceAdapter();
        rvDevices.setAdapter(adapter);

        adapter.setOnDeviceClickListener(this::showDeviceDetails);
        
        setupNetworkReceiver();
        
        return view;
    }

    private void setupNetworkReceiver() {
        networkStateReceiver = new NetworkStateReceiver((isConnected, isWifi, name) -> {
            if (viewModel != null) {
                viewModel.updateNetworkState(isConnected, isWifi, name);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null) {
            ContextCompat.registerReceiver(getContext(), networkStateReceiver, 
                    NetworkStateReceiver.getIntentFilter(), ContextCompat.RECEIVER_NOT_EXPORTED);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getContext() != null) {
            getContext().unregisterReceiver(networkStateReceiver);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ScannerViewModel.class);

        btnScan.setOnClickListener(v -> checkPermissionsAndScan());
        
        swipeRefresh.setOnRefreshListener(this::checkPermissionsAndScan);

        viewModel.getDiscoveredDevices().observe(getViewLifecycleOwner(), devices -> {
            adapter.setDevices(devices);
            emptyState.setVisibility(devices == null || devices.isEmpty() ? View.VISIBLE : View.GONE);
        });

        viewModel.getIsScanning().observe(getViewLifecycleOwner(), isScanning -> {
            btnScan.setEnabled(!isScanning && Boolean.TRUE.equals(viewModel.getIsWifiConnected().getValue()));
            btnScan.setText(isScanning ? "Scanning..." : "Start Scan");
            progressIndicator.setVisibility(isScanning ? View.VISIBLE : View.INVISIBLE);
            swipeRefresh.setRefreshing(isScanning);
            
            if (!isScanning && viewModel.getDiscoveredDevices().getValue() != null && !viewModel.getDiscoveredDevices().getValue().isEmpty()) {
                Snackbar.make(view, "Scan complete: " + viewModel.getDiscoveredDevices().getValue().size() + " devices found", Snackbar.LENGTH_SHORT).show();
                showRenameDialog();
            }
        });

        viewModel.getProgress().observe(getViewLifecycleOwner(), progress -> {
            progressIndicator.setProgress(progress);
        });

        viewModel.getIsWifiConnected().observe(getViewLifecycleOwner(), isConnected -> {
            if (!Boolean.TRUE.equals(viewModel.getIsScanning().getValue())) {
                btnScan.setEnabled(isConnected);
            }
            if (!isConnected) {
                Snackbar.make(view, "No Wi-Fi connection", Snackbar.LENGTH_SHORT)
                        .setAction("Dismiss", v -> {})
                        .show();
            }
        });

        viewModel.getNetworkName().observe(getViewLifecycleOwner(), name -> {
            tvNetworkName.setText(name);
            tvIpAddress.setText("IP: " + NetworkUtils.getLocalIpAddress(requireContext()));
            
            if (name.equals("Wi-Fi Connected") || name.equals("<unknown ssid>")) {
                if (!NetworkUtils.isLocationEnabled(requireContext())) {
                    Snackbar.make(requireView(), "Enable Location to see Wi-Fi name (SSID)", Snackbar.LENGTH_LONG)
                            .setAction("Settings", v -> {
                                startActivity(new android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }).show();
                }
            }
        });
    }

    private void checkPermissionsAndScan() {
        permissionManager.checkAndRequestPermissions(new PermissionManager.PermissionCallback() {
            @Override
            public void onPermissionsGranted() {
                startScan();
            }

            @Override
            public void onPermissionsDenied(List<String> deniedPermissions) {
                Snackbar.make(requireView(), "Permissions required for scanning", Snackbar.LENGTH_LONG).show();
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void startScan() {
        String ip = NetworkUtils.getLocalIpAddress(requireContext());
        if (ip.equals("0.0.0.0")) {
            Snackbar.make(requireView(), "Please connect to Wi-Fi", Snackbar.LENGTH_SHORT).show();
            swipeRefresh.setRefreshing(false);
            return;
        }
        String subnet = NetworkUtils.getSubnet(ip);
        viewModel.startScan(subnet);
    }

    private void showRenameDialog() {
        if (!isAdded()) return;

        final EditText input = new EditText(requireContext());
        input.setHint("Enter network name");
        input.setSingleLine(true);
        
        FrameLayout container = new FrameLayout(requireContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        input.setLayoutParams(params);
        container.addView(input);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Save Scan")
                .setMessage("What would you like to name this network?")
                .setView(container)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        viewModel.renameCurrentSession(name);
                    }
                })
                .setNegativeButton("Keep Default", null)
                .show();
    }

    private void showDeviceDetails(Device device) {
        displayDetailsDialog(device);
    }

    private void displayDetailsDialog(Device device) {
        if (!isAdded()) return;
        
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_device_details, null);
        
        setupDetailItem(dialogView.findViewById(R.id.itemHostname), "Hostname", 
                device.getHostname() != null && !device.getHostname().isEmpty() ? device.getHostname() : "N/A");
        setupDetailItem(dialogView.findViewById(R.id.itemIp), "IP Address", device.getIpAddress());

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btnClose).setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    private void setupDetailItem(View view, String label, String value) {
        ((TextView) view.findViewById(R.id.label)).setText(label);
        ((TextView) view.findViewById(R.id.value)).setText(value);
    }
}
