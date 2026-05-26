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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.schoolcomputers.networkscanner.R;
import com.schoolcomputers.networkscanner.models.NetworkInfo;
import com.schoolcomputers.networkscanner.utils.SpeedTestHelper;
import com.schoolcomputers.networkscanner.viewmodels.ScanViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * "My Network" tab.
 * Shows: SSID, device count, gateway IP, gateway MAC, link speed,
 * and a speed-test button that pings a public server and reports latency.
 */
public class MyNetworkFragment extends Fragment {

    private ScanViewModel viewModel;
    private TextView tvSsid, tvRouterIp, tvLinkSpeed, tvSignal, tvSpeedResult;
    private LinearProgressIndicator progressSpeed;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_network, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ScanViewModel.class);

        tvSsid        = view.findViewById(R.id.tvSsid);
        tvRouterIp    = view.findViewById(R.id.tvRouterIp);
        tvLinkSpeed   = view.findViewById(R.id.tvLinkSpeed);
        tvSignal      = view.findViewById(R.id.tvSignal);
        tvSpeedResult = view.findViewById(R.id.tvSpeedResult);
        progressSpeed = view.findViewById(R.id.progressSpeed);

        SwipeRefreshLayout swipe = view.findViewById(R.id.swipeRefresh);
        swipe.setOnRefreshListener(() -> {
            viewModel.refreshNetworkInfo();
            swipe.setRefreshing(false);
        });

        MaterialButton btnSpeedTest = view.findViewById(R.id.btnSpeedTest);
        btnSpeedTest.setOnClickListener(v -> runSpeedTest());

        viewModel.getNetworkInfo().observe(getViewLifecycleOwner(), this::bindNetworkInfo);
        viewModel.refreshNetworkInfo();


    }

    private void bindNetworkInfo(NetworkInfo info) {
        if (info == null) return;
        String ssid = info.getSsid(), gwIp = info.getGatewayIp();
        boolean validSsid = ssid != null && !ssid.isEmpty() && !ssid.equals("<unknown ssid>");
        tvSsid.setText(validSsid ? ssid : "<unknown ssid> - Please enable GPS");
        tvRouterIp.setText(gwIp != null ? gwIp : "—");
        tvLinkSpeed.setText(info.getLinkSpeedMbps() + " Mbps");
        tvSignal.setText(signalLabel(info.getSignalLevel()));
    }

    private String signalLabel(int level) {
        switch (level) {
            case 4: return "Excellent";
            case 3: return "Good";
            case 2: return "Fair";
            case 1: return "Weak";
            default: return "No signal";
        }
    }

    private void runSpeedTest() {
        progressSpeed.setVisibility(View.VISIBLE);
        tvSpeedResult.setText("Testing…");

        executor.execute(() -> {
            long latencyMs = SpeedTestHelper.measureLatency("8.8.8.8");
            requireActivity().runOnUiThread(() -> {
                progressSpeed.setVisibility(View.GONE);
                if (latencyMs < 0) {
                    tvSpeedResult.setText("Test failed — check connection");
                } else {
                    tvSpeedResult.setText("Latency: " + latencyMs + " ms");
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executor.shutdown();
    }
}