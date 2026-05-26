package com.schoolcomputers.networkscanner.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.schoolcomputers.networkscanner.R;
import com.schoolcomputers.networkscanner.adapters.DeviceAdapter;
import com.schoolcomputers.networkscanner.models.Device;
import com.schoolcomputers.networkscanner.utils.NotificationHelper;
import com.schoolcomputers.networkscanner.viewmodels.ScanViewModel;

import java.util.ArrayList;

/**
 * "Main" tab: large scan button + live device results RecyclerView.
 * After scan completes, prompts for a network name.
 */
public class MainFragment extends Fragment {

    private ScanViewModel viewModel;
    private DeviceAdapter adapter;
    private NotificationHelper notifHelper;

    private ExtendedFloatingActionButton btnScan;
    private ProgressBar progressBar;
    private TextView tvProgress;
    private TextView tvStatus;
    private RecyclerView rvDevices;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel    = new ViewModelProvider(requireActivity()).get(ScanViewModel.class);
        notifHelper  = new NotificationHelper(requireContext());

        btnScan    = view.findViewById(R.id.btnScan);
        progressBar= view.findViewById(R.id.progressBar);
        tvProgress = view.findViewById(R.id.tvProgress);
        tvStatus   = view.findViewById(R.id.tvStatus);
        rvDevices  = view.findViewById(R.id.rvDevices);

        adapter = new DeviceAdapter(new ArrayList<>(), device -> showDeviceDialog(device));
        rvDevices.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvDevices.setAdapter(adapter);

        btnScan.setOnClickListener(v -> handleScanButton());

        // Observe scan state
        viewModel.getScanState().observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case SCANNING:
                    btnScan.setText("Cancel Scan");
                    btnScan.setIconResource(R.drawable.ic_stop);
                    progressBar.setVisibility(View.VISIBLE);
                    tvStatus.setText("Scanning…");
                    notifHelper.showScanningNotification();
                    break;

                case DONE:
                    btnScan.setText("Scan Network");
                    btnScan.setIconResource(R.drawable.ic_scan);
                    progressBar.setVisibility(View.GONE);
                    tvStatus.setText("Scan complete");
                    int count = adapter.getItemCount();
                    notifHelper.showScanCompleteNotification(count);
                    promptForNetworkName();
                    break;

                case ERROR:
                    btnScan.setText("Scan Network");
                    btnScan.setIconResource(R.drawable.ic_scan);
                    progressBar.setVisibility(View.GONE);
                    tvStatus.setText("Scan failed");
                    break;

                case IDLE:
                default:
                    btnScan.setText("Scan Network");
                    btnScan.setIconResource(R.drawable.ic_scan);
                    progressBar.setVisibility(View.GONE);
                    tvStatus.setText("Tap to scan your network");
                    break;
            }
        });

        // Observe discovered devices
        viewModel.getLiveDevices().observe(getViewLifecycleOwner(), devices -> {
            adapter.updateDevices(devices);
        });

        // Observe scan progress percentage
        viewModel.getScanProgress().observe(getViewLifecycleOwner(), pct -> {
            progressBar.setProgress(pct);
            tvProgress.setText(pct + "%");
        });
    }

    private void handleScanButton() {
        ScanViewModel.ScanState state = viewModel.getScanState().getValue();
        if (state == ScanViewModel.ScanState.SCANNING) {
            viewModel.cancelScan();
        } else {
            viewModel.startScan(null); // name will be prompted after scan
        }
    }

    /** Shows a dialog to let the username the scan after it completes. */
    private void promptForNetworkName() {
        if (!isAdded()) return;

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_network_name, null);
        EditText etName = dialogView.findViewById(R.id.etNetworkName);

        new AlertDialog.Builder(requireContext())
            .setTitle("Name this scan")
            .setMessage("Give this network scan a memorable name:")
            .setView(dialogView)
            .setPositiveButton("Save", (d, w) -> {
                String name = etName.getText().toString().trim();
                viewModel.renameScan(name);
                if (!name.isEmpty()) {
                    Toast.makeText(requireContext(), "Saved as \"" + name + "\"",
                            Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Skip", (d, w) -> viewModel.renameScan(null))
            .show();
    }

    /** Shows device detail in a dialog when tapped. */
    private void showDeviceDialog(Device device) {
        Bundle args = new Bundle();
        args.putString("ip",         device.ipAddress);
        args.putString("mac",        device.macAddress);
        args.putString("hostname",   device.hostname);
        args.putString("routerIp",   device.routerIp);
        args.putString("routerMac",  device.routerMac);

        Navigation.findNavController(requireView())
                  .navigate(R.id.action_mainFragment_to_deviceDetailFragment, args);
    }
}
