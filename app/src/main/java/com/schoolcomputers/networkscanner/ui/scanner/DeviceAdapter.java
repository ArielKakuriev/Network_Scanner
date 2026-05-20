package com.schoolcomputers.networkscanner.ui.scanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.schoolcomputers.networkscanner.R;
import com.schoolcomputers.networkscanner.data.model.Device;

import java.util.Objects;

public class DeviceAdapter extends ListAdapter<Device, DeviceAdapter.DeviceViewHolder> {

    private OnDeviceClickListener listener;

    public interface OnDeviceClickListener {
        void onDeviceClick(Device device);
    }

    public void setOnDeviceClickListener(OnDeviceClickListener listener) {
        this.listener = listener;
    }

    public DeviceAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Device> DIFF_CALLBACK = new DiffUtil.ItemCallback<Device>() {
        @Override
        public boolean areItemsTheSame(@NonNull Device oldItem, @NonNull Device newItem) {
            // Use IP Address as a fallback identity if ID is not set (e.g., before saving to DB)
            if (oldItem.getId() != 0 && newItem.getId() != 0) {
                return oldItem.getId() == newItem.getId();
            }
            return Objects.equals(oldItem.getIpAddress(), newItem.getIpAddress());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Device oldItem, @NonNull Device newItem) {
            return Objects.equals(oldItem.getIpAddress(), newItem.getIpAddress()) &&
                    Objects.equals(oldItem.getHostname(), newItem.getHostname()) &&
                    oldItem.isGateway() == newItem.isGateway() &&
                    oldItem.isLocalDevice() == newItem.isLocalDevice();
        }
    };

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public class DeviceViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvIp;
        private final TextView tvHostname;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIp = itemView.findViewById(R.id.tvDeviceIp);
            tvHostname = itemView.findViewById(R.id.tvDeviceHostname);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onDeviceClick(getItem(position));
                }
            });
        }

        public void bind(Device device) {
            String ipText = device.getIpAddress();
            if (device.isLocalDevice()) ipText += " (This Device)";
            else if (device.isGateway()) ipText += " (Gateway)";
            
            tvIp.setText(ipText);
            
            String hostname = device.getHostname() != null && !device.getHostname().isEmpty() ? 
                    device.getHostname() : "Unknown Device";
            if (device.getDeviceType() != null) {
                hostname += " [" + device.getDeviceType() + "]";
            }
            tvHostname.setText(hostname);
        }
    }

    // Helper method to maintain compatibility with existing Fragment code
    public void setDevices(java.util.List<Device> devices) {
        submitList(devices);
    }
}
