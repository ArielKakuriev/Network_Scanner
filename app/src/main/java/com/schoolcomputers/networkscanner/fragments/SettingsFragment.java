package com.schoolcomputers.networkscanner.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.schoolcomputers.networkscanner.R;
import com.schoolcomputers.networkscanner.activities.AuthActivity;
import com.schoolcomputers.networkscanner.models.User;
import com.schoolcomputers.networkscanner.viewmodels.AuthViewModel;

/**
 * "Settings" tab.
 * Section 1: Account info (username, email, change password link).
 * Section 2: App preferences (notifications toggle, scan timeout).
 */
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

        // Load user profile
        authViewModel.getUserProfile().observe(getViewLifecycleOwner(), this::bindProfile);

        // Change password
        MaterialButton btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        // Notifications toggle
        SwitchMaterial switchNotifications = view.findViewById(R.id.switchNotifications);
        switchNotifications.setChecked(loadNotificationPref());
        switchNotifications.setOnCheckedChangeListener((btn, checked) ->
                saveNotificationPref(checked));

        // Sign out
        MaterialButton btnSignOut = view.findViewById(R.id.btnSignOut);
        btnSignOut.setOnClickListener(v -> confirmSignOut());
    }

    private void bindProfile(User user) {
        if (user == null) return;
        tvUsername.setText(user.getUsername());
        tvEmail.setText(user.getEmail());
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_change_password, null);
        EditText etEmail = dialogView.findViewById(R.id.etEmailForReset);

        // Pre-fill email
        User cachedUser = authViewModel.getUserProfile().getValue();
        if (cachedUser != null) etEmail.setText(cachedUser.getEmail());

        new AlertDialog.Builder(requireContext())
            .setTitle("Reset Password")
            .setMessage("We'll send a reset link to your email.")
            .setView(dialogView)
            .setPositiveButton("Send", (d, w) -> {
                String email = etEmail.getText().toString().trim();
                if (!email.isEmpty()) {
                    // Reuse forgot-password flow: username will be validated server-side
                    authViewModel.forgotPassword("", email);
                    Toast.makeText(requireContext(),
                            "Reset email sent to " + email, Toast.LENGTH_SHORT).show();
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

    // ---- SharedPreferences helpers ----

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
