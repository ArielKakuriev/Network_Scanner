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
 * Login fragment hosted inside AuthActivity's ViewPager2.
 */
public class LoginFragment extends Fragment {

    private AuthViewModel viewModel;
    private EditText etUsername, etPassword;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel   = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        etUsername  = view.findViewById(R.id.etLoginUsername);
        etPassword  = view.findViewById(R.id.etLoginPassword);
        progressBar = view.findViewById(R.id.progressLogin);

        MaterialButton btnLogin = view.findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> attemptLogin());

        viewModel.isLoading().observe(getViewLifecycleOwner(), loading ->
                progressBar.setVisibility(loading ? View.VISIBLE : View.GONE));

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (username.isEmpty()) { etUsername.setError("Required"); return; }
        if (password.isEmpty()) { etPassword.setError("Required"); return; }

        viewModel.login(username, password);
    }
}
