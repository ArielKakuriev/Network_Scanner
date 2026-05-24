package com.networkscanner.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.networkscanner.R;

/**
 * Detail screen for a single device.
 * Receives device data via Bundle args (navigated to from Main or History).
 */
public class DeviceDetailFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args == null) return;

        bindField(view, R.id.tvDetailIp,        args.getString("ip",        "—"));
        bindField(view, R.id.tvDetailMac,        args.getString("mac",       "—"));
        bindField(view, R.id.tvDetailHostname,   args.getString("hostname",  "—"));
        bindField(view, R.id.tvDetailRouterIp,   args.getString("routerIp",  "—"));
        bindField(view, R.id.tvDetailRouterMac,  args.getString("routerMac", "—"));
    }

    private void bindField(View root, int viewId, String value) {
        TextView tv = root.findViewById(viewId);
        if (tv != null) tv.setText(value != null ? value : "—");
    }
}
