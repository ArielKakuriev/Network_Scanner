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

import com.schoolcomputers.networkscanner.R;
import com.schoolcomputers.networkscanner.models.NetworkInfo;
import com.schoolcomputers.networkscanner.viewmodels.ScanViewModel;

/**
 * "About Me" tab.
 * Displays this device's network-facing IP, MAC, connected SSID,
 * and the gateway IP and MAC.
 */
public class AboutMeFragment extends Fragment {

    private ScanViewModel viewModel;
    private TextView tvConnectedSsid, tvDeviceIp, tvDeviceMac,
                     tvRouterIp, tvRouterMac;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about_me, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ScanViewModel.class);

        tvConnectedSsid = view.findViewById(R.id.tvConnectedSsid);
        tvDeviceIp      = view.findViewById(R.id.tvDeviceIp);
        tvDeviceMac     = view.findViewById(R.id.tvDeviceMac);
        tvRouterIp      = view.findViewById(R.id.tvRouterIp);
        tvRouterMac     = view.findViewById(R.id.tvRouterMac);

        viewModel.getNetworkInfo().observe(getViewLifecycleOwner(), this::bindInfo);
        viewModel.refreshNetworkInfo();
    }

    private void bindInfo(NetworkInfo info) {
        if (info == null) return;
        tvConnectedSsid.setText(info.getSsid() != null ? info.getSsid() : "Not connected");
        tvDeviceIp.setText(info.getDeviceIp() != null ? info.getDeviceIp() : "—");
        tvDeviceMac.setText(info.getDeviceMac() != null ? info.getDeviceMac() : "—");
        tvRouterIp.setText(info.getGatewayIp() != null ? info.getGatewayIp() : "—");
        tvRouterMac.setText(info.getGatewayMac() != null ? info.getGatewayMac() : "—");
    }
}
