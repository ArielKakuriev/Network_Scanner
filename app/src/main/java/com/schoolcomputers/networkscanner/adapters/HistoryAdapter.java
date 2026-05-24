package com.networkscanner.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.networkscanner.R;
import com.networkscanner.models.ScanRecord;
import com.networkscanner.models.ScanWithDevices;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for the History tab — shows scan records.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ScanViewHolder> {

    public interface OnScanClickListener {
        void onScanClick(ScanRecord scan);
    }

    private List<ScanWithDevices> data;
    private final OnScanClickListener listener;
    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("dd/MM/yyyy  HH:mm", Locale.getDefault());

    public HistoryAdapter(List<ScanWithDevices> data, OnScanClickListener listener) {
        this.data     = data;
        this.listener = listener;
    }

    public void updateData(List<ScanWithDevices> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ScanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                               .inflate(R.layout.item_scan_record, parent, false);
        return new ScanViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ScanViewHolder holder, int position) {
        ScanWithDevices swd = data.get(position);
        holder.bind(swd.scan, listener);
    }

    @Override public int getItemCount() { return data != null ? data.size() : 0; }

    class ScanViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvCount;

        ScanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName  = itemView.findViewById(R.id.tvScanName);
            tvDate  = itemView.findViewById(R.id.tvScanDate);
            tvCount = itemView.findViewById(R.id.tvDeviceCount);
        }

        void bind(ScanRecord scan, OnScanClickListener listener) {
            tvName.setText(scan.networkName);
            tvDate.setText(DATE_FMT.format(new Date(scan.scannedAt)));
            tvCount.setText(scan.deviceCount + " device(s)");
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onScanClick(scan);
            });
        }
    }
}
