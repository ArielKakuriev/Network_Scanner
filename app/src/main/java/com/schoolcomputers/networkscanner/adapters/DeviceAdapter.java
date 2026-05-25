package com.schoolcomputers.networkscanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.schoolcomputers.networkscanner.R;
import com.schoolcomputers.networkscanner.models.Device;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for a list of {@link Device} objects.

 * Uses {@link DiffUtil} for efficient diff-based updates —
 * satisfies the ObservableCollection + RecyclerView requirement.

 * Exposes a click listener callback for device detail navigation.
 */
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

    /** Functional interface for row click events. */
    public interface OnDeviceClickListener {
        void onDeviceClick(Device device);
    }

    private List<Device> devices;
    private final OnDeviceClickListener listener;

    public DeviceAdapter(List<Device> devices, OnDeviceClickListener listener) {
        this.devices  = devices != null ? devices : new ArrayList<>();
        this.listener = listener;
    }

    /**
     * Updates the dataset using DiffUtil to compute the minimal set of changes.
     */
    public void updateDevices(List<Device> newDevices) {
        if (newDevices == null) newDevices = new ArrayList<>();
        List<Device> finalNew = newDevices;

        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return devices.size(); }
            @Override public int getNewListSize() { return finalNew.size(); }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return devices.get(oldPos).id == finalNew.get(newPos).id;
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                Device o = devices.get(oldPos);
                Device n = finalNew.get(newPos);
                return safeEqual(o.ipAddress, n.ipAddress)
                    && safeEqual(o.macAddress, n.macAddress);
            }
        });

        this.devices = finalNew;
        result.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Device device = devices.get(position);
        holder.bind(device, listener);
    }

    @Override public int getItemCount() { return devices.size(); }

    // ---- ViewHolder ----

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvIp, tvMac, tvHostname;

        DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIp       = itemView.findViewById(R.id.tvDeviceIp);
            tvMac      = itemView.findViewById(R.id.tvDeviceMac);
            tvHostname = itemView.findViewById(R.id.tvDeviceHostname);
        }

        void bind(Device device, OnDeviceClickListener listener) {
            if (device == null) {
                tvIp.setText("—");
                tvMac.setText("Unknown MAC");
                tvHostname.setText("—");
                itemView.setOnClickListener(null);
                return;
            }
            tvIp.setText(device.ipAddress != null ? device.ipAddress : "—");
            tvMac.setText(device.macAddress != null ? device.macAddress : "Unknown MAC");
            tvHostname.setText(device.hostname != null ? device.hostname : device.ipAddress != null ? device.ipAddress : "—");
            itemView.setOnClickListener(v -> {
                if (listener != null && device != null) {
                    try {
                        listener.onDeviceClick(device);
                    } catch (Exception e) {
                        android.util.Log.e("DeviceAdapter", "Error on device click", e);
                    }
                }
            });
        }
    }

    // ---- Helpers ----

    private static boolean safeEqual(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
