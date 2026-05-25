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
import com.schoolcomputers.networkscanner.utils.ValidationUtils;
import com.schoolcomputers.networkscanner.viewmodels.AuthViewModel;

/**
 * Register fragment (username, password, re-password, email).
 */
public class RegisterFragment extends Fragment {

    private AuthViewModel viewModel;
    private EditText etUsername, etEmail, etPassword, etRePassword;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel    = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        etUsername   = view.findViewById(R.id.etRegUsername);
        etEmail      = view.findViewById(R.id.etRegEmail);
        etPassword   = view.findViewById(R.id.etRegPassword);
        etRePassword = view.findViewById(R.id.etRegRePassword);
        progressBar  = view.findViewById(R.id.progressRegister);

        view.findViewById(R.id.btnRegister).setOnClickListener(v -> attemptRegister());

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

    private void attemptRegister() {
        String username   = etUsername.getText().toString().trim();
        String email      = etEmail.getText().toString().trim();
        String password   = etPassword.getText().toString();
        String rePassword = etRePassword.getText().toString();

        String usernameErr  = ValidationUtils.validateUsername(username);
        String emailErr     = ValidationUtils.validateEmail(email);
        String passwordErr  = ValidationUtils.validatePassword(password);
        String rePassErr    = ValidationUtils.validateRePassword(password, rePassword);

        if (usernameErr  != null) { etUsername.setError(usernameErr);   return; }
        if (emailErr     != null) { etEmail.setError(emailErr);         return; }
        if (passwordErr  != null) { etPassword.setError(passwordErr);   return; }
        if (rePassErr    != null) { etRePassword.setError(rePassErr);   return; }

        viewModel.register(username, email, password);
    }
}
