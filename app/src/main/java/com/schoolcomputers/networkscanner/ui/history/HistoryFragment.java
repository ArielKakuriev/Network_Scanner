package com.schoolcomputers.networkscanner.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.schoolcomputers.networkscanner.R;

public class HistoryFragment extends Fragment {
    private HistoryViewModel viewModel;
    private HistoryAdapter adapter;
    private RecyclerView rvHistory;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        rvHistory = view.findViewById(R.id.rvHistory);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        try {
            adapter = new HistoryAdapter();
            rvHistory.setAdapter(adapter);

            viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
            viewModel.getAllSessions().observe(getViewLifecycleOwner(), sessions -> {
                if (sessions == null || sessions.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rvHistory.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rvHistory.setVisibility(View.VISIBLE);
                    adapter.setSessions(sessions);
                }
            });

            adapter.setOnSessionClickListener(session -> {
                // Handle session click
            });
        } catch (Exception e) {
            e.printStackTrace();
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Error loading history: " + e.getMessage());
        }
    }
}
