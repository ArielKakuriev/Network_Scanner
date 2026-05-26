package com.schoolcomputers.networkscanner.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.schoolcomputers.networkscanner.R;
import com.schoolcomputers.networkscanner.adapters.AuthPagerAdapter;
import com.schoolcomputers.networkscanner.viewmodels.AuthViewModel;

/**
 * Hosts Login / Register / Forgot-Password tabs in a ViewPager2.
 * Observes FirebaseUser (not an integer userId) from AuthViewModel.
 */

/** // This class uses ViewPager + Fragment // **/
/** // Firebase: Auth flow // **/
public class AuthActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // If already signed in via Firebase, skip auth screens
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

        // FirebaseUser non-null → login/register succeeded → go to main
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
