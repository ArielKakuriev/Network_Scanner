package com.schoolcomputers.networkscanner.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.schoolcomputers.networkscanner.R;
import com.schoolcomputers.networkscanner.data.model.ScanSession;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.SessionViewHolder> {
    private List<ScanSession> sessions = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    private OnSessionClickListener listener;

    public interface OnSessionClickListener {
        void onSessionClick(ScanSession session);
    }

    public void setOnSessionClickListener(OnSessionClickListener listener) {
        this.listener = listener;
    }

    public void setSessions(List<ScanSession> sessions) {
        this.sessions = sessions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scan_history, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        ScanSession session = sessions.get(position);
        holder.tvNetworkName.setText(session.getNetworkName());
        holder.tvScanDate.setText(dateFormat.format(new Date(session.getStartTime())));
        holder.tvDeviceCount.setText(String.format(Locale.getDefault(), "%d Devices", session.getDeviceCount()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSessionClick(session);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView tvNetworkName, tvScanDate, tvDeviceCount;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNetworkName = itemView.findViewById(R.id.tvNetworkName);
            tvScanDate = itemView.findViewById(R.id.tvScanDate);
            tvDeviceCount = itemView.findViewById(R.id.tvDeviceCount);
        }
    }
}
