package com.schoolcomputers.networkscanner.ui.scanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.schoolcomputers.networkscanner.R;
import com.schoolcomputers.networkscanner.data.model.Device;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {
    private List<Device> devices = new ArrayList<>();

    public void setDevices(List<Device> devices) {
        this.devices = new ArrayList<>(devices);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        Device device = devices.get(position);
        holder.tvDeviceIp.setText(device.getIpAddress());
        holder.tvDeviceHostname.setText(device.getHostname());
        holder.tvDeviceMac.setText(device.getMacAddress());
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView tvDeviceIp, tvDeviceHostname, tvDeviceMac;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDeviceIp = itemView.findViewById(R.id.tvDeviceIp);
            tvDeviceHostname = itemView.findViewById(R.id.tvDeviceHostname);
            tvDeviceMac = itemView.findViewById(R.id.tvDeviceMac);
        }
    }
}
