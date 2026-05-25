package com.schoolcomputers.networkscanner.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.schoolcomputers.networkscanner.R;
import com.schoolcomputers.networkscanner.viewmodels.AuthViewModel;

/**
 * Forgot-password fragment: username + email → sends Firebase reset email.
 */
public class ForgotPasswordFragment extends Fragment {

    private AuthViewModel viewModel;
    private EditText etUsername, etEmail;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel   = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        etUsername  = view.findViewById(R.id.etForgotUsername);
        etEmail     = view.findViewById(R.id.etForgotEmail);
        progressBar = view.findViewById(R.id.progressForgot);

        MaterialButton btnSend = view.findViewById(R.id.btnSendReset);
        btnSend.setOnClickListener(v -> sendReset());

        viewModel.isLoading().observe(getViewLifecycleOwner(), loading ->
                progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty())
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.getSuccessMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty())
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void sendReset() {
        String username = etUsername.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();

        if (username.isEmpty()) { etUsername.setError("Required"); return; }
        if (email.isEmpty())    { etEmail.setError("Required");    return; }

        viewModel.forgotPassword(username, email);
    }
}
