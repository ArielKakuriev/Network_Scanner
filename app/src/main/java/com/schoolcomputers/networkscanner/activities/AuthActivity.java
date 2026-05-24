package com.networkscanner.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.networkscanner.R;
import com.networkscanner.adapters.AuthPagerAdapter;
import com.networkscanner.viewmodels.AuthViewModel;

/**
 * Container activity for authentication flows.
 * Uses ViewPager2 + TabLayout to host Login, Register, and Forgot-Password fragments.
 * This satisfies the "ViewPager in combination with Fragment" requirement.
 */
public class AuthActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // If already signed in, go straight to main
        if (authViewModel.isSignedIn()) {
            navigateToMain();
            return;
        }

        ViewPager2 viewPager = findViewById(R.id.viewPagerAuth);
        TabLayout  tabLayout = findViewById(R.id.tabLayoutAuth);

        viewPager.setAdapter(new AuthPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Login");           break;
                case 1: tab.setText("Register");        break;
                case 2: tab.setText("Forgot Password"); break;
            }
        }).attach();

        // Observe successful login → navigate to main
        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null) navigateToMain();
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
