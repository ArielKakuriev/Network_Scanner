package com.schoolcomputers.networkscanner.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.schoolcomputers.networkscanner.R;
import com.schoolcomputers.networkscanner.activities.AuthActivity;
import com.schoolcomputers.networkscanner.models.User;
import com.schoolcomputers.networkscanner.viewmodels.AuthViewModel;

/**
 * Settings tab — account info and app preferences.
 */

/** // Firebase: Read user info, logout // **/
public class SettingsFragment extends Fragment {

    private AuthViewModel authViewModel;
    private TextView tvUsername, tvEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        tvUsername = view.findViewById(R.id.tvSettingsUsername);
        tvEmail    = view.findViewById(R.id.tvSettingsEmail);

        authViewModel.getUserProfile().observe(getViewLifecycleOwner(), this::bindProfile);

        MaterialButton btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        SwitchMaterial switchNotifications = view.findViewById(R.id.switchNotifications);
        switchNotifications.setChecked(loadNotificationPref());
        switchNotifications.setOnCheckedChangeListener((btn, checked) ->
                saveNotificationPref(checked));

        MaterialButton btnSignOut = view.findViewById(R.id.btnSignOut);
        btnSignOut.setOnClickListener(v -> confirmSignOut());

        authViewModel.getSuccessMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty())
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
        });

        authViewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty())
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
        });
    }

    private void bindProfile(User user) {
        if (user == null) return;
        tvUsername.setText(user.getUsername());
        tvEmail.setText(user.getEmail());
    }

    private void showChangePasswordDialog() {
        // Get the signed-in user's email directly from Firebase Auth — no Firestore needed.
        String currentEmail = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getEmail() : "";

        new AlertDialog.Builder(requireContext())
                .setTitle("Reset Password")
                .setMessage("We will send a password reset link to:\n" + currentEmail)
                .setPositiveButton("Send Reset Email", (d, w) -> {
                    if (currentEmail != null && !currentEmail.isEmpty()) {
                        // Pass empty username — AuthViewModel.forgotPassword handles this
                        // by skipping the Firestore lookup and sending the email directly.
                        authViewModel.forgotPassword("", currentEmail);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmSignOut() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out", (d, w) -> {
                    authViewModel.signOut();
                    Intent intent = new Intent(requireContext(), AuthActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean loadNotificationPref() {
        return requireContext()
                .getSharedPreferences("app_prefs", 0)
                .getBoolean("notifications_enabled", true);
    }

    private void saveNotificationPref(boolean enabled) {
        requireContext()
                .getSharedPreferences("app_prefs", 0)
                .edit()
                .putBoolean("notifications_enabled", enabled)
                .apply();
    }
}
