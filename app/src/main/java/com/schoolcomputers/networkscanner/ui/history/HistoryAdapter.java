package com.schoolcomputers.networkscanner.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.schoolcomputers.networkscanner.R;
import com.schoolcomputers.networkscanner.data.model.ScanSession;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class HistoryAdapter extends ListAdapter<ScanSession, HistoryAdapter.HistoryViewHolder> {

    private OnSessionClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault());

    public interface OnSessionClickListener {
        void onSessionClick(ScanSession session);
    }

    public void setOnSessionClickListener(OnSessionClickListener listener) {
        this.listener = listener;
    }

    public HistoryAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<ScanSession> DIFF_CALLBACK = new DiffUtil.ItemCallback<ScanSession>() {
        @Override
        public boolean areItemsTheSame(@NonNull ScanSession oldItem, @NonNull ScanSession newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ScanSession oldItem, @NonNull ScanSession newItem) {
            return oldItem.getStartTime() == newItem.getStartTime() &&
                    oldItem.getDeviceCount() == newItem.getDeviceCount() &&
                    Objects.equals(oldItem.getNetworkName(), newItem.getNetworkName());
        }
    };

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scan_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNetworkName;
        private final TextView tvScanDate;
        private final TextView tvDeviceCount;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNetworkName = itemView.findViewById(R.id.tvNetworkName);
            tvScanDate = itemView.findViewById(R.id.tvScanDate);
            tvDeviceCount = itemView.findViewById(R.id.tvDeviceCount);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onSessionClick(getItem(position));
                }
            });
        }

        public void bind(ScanSession session) {
            tvNetworkName.setText(session.getNetworkName() != null ? session.getNetworkName() : "Unknown Network");
            tvScanDate.setText(dateFormat.format(new Date(session.getStartTime())));
            tvDeviceCount.setText(String.format(Locale.getDefault(), "%d Devices", session.getDeviceCount()));
        }
    }

    // Helper method to maintain compatibility with existing Fragment code
    public void setSessions(java.util.List<ScanSession> sessions) {
        submitList(sessions);
    }
}
