package com.schoolcomputers.networkscanner.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.schoolcomputers.networkscanner.R;

public class SettingsFragment extends Fragment {
    private SettingsViewModel viewModel;
    private SwitchMaterial switchDarkMode;
    private Slider sliderTimeout;
    private MaterialButton btnClearHistory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        switchDarkMode = view.findViewById(R.id.switchDarkMode);
        sliderTimeout = view.findViewById(R.id.sliderTimeout);
        btnClearHistory = view.findViewById(R.id.btnClearHistory);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        viewModel.getDarkMode().observe(getViewLifecycleOwner(), enabled -> {
            switchDarkMode.setChecked(enabled);
            AppCompatDelegate.setDefaultNightMode(enabled ? 
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        viewModel.getScanTimeout().observe(getViewLifecycleOwner(), timeout -> {
            sliderTimeout.setValue(timeout);
        });

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setDarkMode(isChecked);
        });

        sliderTimeout.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                viewModel.setScanTimeout((int) value);
            }
        });

        btnClearHistory.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Clear History")
                    .setMessage("Are you sure you want to delete all scan records?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        viewModel.clearHistory();
                        com.google.android.material.snackbar.Snackbar.make(requireView(), 
                                "History cleared", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}
