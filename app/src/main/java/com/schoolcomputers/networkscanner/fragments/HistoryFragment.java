package com.schoolcomputers.networkscanner.fragments;

import android.app.AlertDialog;
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

import com.google.android.material.button.MaterialButton;
import com.schoolcomputers.networkscanner.R;
import com.schoolcomputers.networkscanner.adapters.HistoryAdapter;
import com.schoolcomputers.networkscanner.viewmodels.ScanViewModel;

import java.util.ArrayList;

/**
 * "History" tab.
 * Shows all scanned networks. Tapping a network shows its devices.
 * Tapping a device shows device details.
 * Includes a "Clear History" button with confirmation dialog.
 */
public class HistoryFragment extends Fragment {

    private ScanViewModel viewModel;
    private HistoryAdapter adapter;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(ScanViewModel.class);
        tvEmpty   = view.findViewById(R.id.tvEmpty);

        RecyclerView rvHistory = view.findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new HistoryAdapter(
            new ArrayList<>(),
            // On scan row clicked → expand to show devices
            scan -> {
                Bundle args = new Bundle();
                args.putLong("scanId", scan.id);
                args.putString("scanName", scan.networkName);
                Navigation.findNavController(view)
                          .navigate(R.id.action_historyFragment_to_scanDevicesFragment, args);
            }
        );
        rvHistory.setAdapter(adapter);

        // Observe history
        viewModel.getHistory().observe(getViewLifecycleOwner(), scansWithDevices -> {
            adapter.updateData(scansWithDevices);
            tvEmpty.setVisibility(
                    scansWithDevices == null || scansWithDevices.isEmpty()
                            ? View.VISIBLE : View.GONE);
        });

        // Clear history button
        MaterialButton btnClear = view.findViewById(R.id.btnClearHistory);
        btnClear.setOnClickListener(v -> confirmClearHistory());
    }

    private void confirmClearHistory() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Clear History")
            .setMessage("This will permanently delete all scan records. Continue?")
            .setPositiveButton("Delete All", (d, w) -> viewModel.clearHistory())
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }
}
