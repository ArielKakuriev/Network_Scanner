package com.schoolcomputers.networkscanner.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.schoolcomputers.networkscanner.R;
import com.schoolcomputers.networkscanner.adapters.DeviceAdapter;
import com.schoolcomputers.networkscanner.viewmodels.ScanViewModel;

import java.util.ArrayList;

/**
 * Displays all devices that were found in a particular historical scan.
 * Navigated to from HistoryFragment with a scanId Bundle arg.
 */
public class ScanDevicesFragment extends Fragment {

    private ScanViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan_devices, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ScanViewModel.class);

        Bundle args = getArguments();
        long scanId      = args != null ? args.getLong("scanId", -1) : -1;
        String scanName  = args != null ? args.getString("scanName", "Devices") : "Devices";

        TextView tvTitle = view.findViewById(R.id.tvScanDevicesTitle);
        tvTitle.setText(scanName);

        RecyclerView rv = view.findViewById(R.id.rvScanDevices);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        DeviceAdapter adapter = new DeviceAdapter(new ArrayList<>(), device -> {
            try {
                if (device != null) {
                    Bundle detailArgs = new Bundle();
                    detailArgs.putString("ip",        device.ipAddress);
                    detailArgs.putString("mac",       device.macAddress);
                    detailArgs.putString("hostname",  device.hostname);
                    detailArgs.putString("routerIp",  device.routerIp);
                    detailArgs.putString("routerMac", device.routerMac);
                    Navigation.findNavController(view)
                              .navigate(R.id.action_scanDevicesFragment_to_deviceDetailFragment, detailArgs);
                }
            } catch (Exception e) {
                android.util.Log.e("ScanDevicesFragment", "Error navigating to device detail", e);
            }
        });
        rv.setAdapter(adapter);

        if (scanId >= 0) {
            viewModel.getDevicesForScan(scanId).observe(getViewLifecycleOwner(),
                    devices -> {
                        if (devices != null) {
                            adapter.updateDevices(devices);
                        } else {
                            adapter.updateDevices(new ArrayList<>());
                        }
                    });
        }
    }
}
