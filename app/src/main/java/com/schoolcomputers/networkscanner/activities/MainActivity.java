package com.schoolcomputers.networkscanner.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.schoolcomputers.networkscanner.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Main container activity.
 * Hosts all five bottom-nav tabs:
 *   Settings | History | Main | My Network | About Me
 *
 * Uses ActivityResultContract (satisfies that topic) for runtime permissions.
 * NavController + NavigationUI handle fragment back-stack correctly.
 */
public class MainActivity extends AppCompatActivity {

    private NavController navController;

    /**
     * ActivityResultContract launcher for multiple runtime permissions.
     * Satisfies the "Using ActivityResultContract" requirement.
     */
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean allGranted = !result.containsValue(false);
                    if (!allGranted) {
                        Toast.makeText(this,
                            "Location permission is required to scan the network.",
                            Toast.LENGTH_LONG).show();
                    }
                }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Wire NavController to BottomNavigationView
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navHostFragment);
        if (navHost != null) {
            navController = navHost.getNavController();
            BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
            NavigationUI.setupWithNavController(bottomNav, navController);
        }

        requestRequiredPermissions();
    }

    /**
     * Requests location + notification permissions using the ActivityResultContract launcher.
     */
    private void requestRequiredPermissions() {
        List<String> needed = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            needed.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                needed.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!needed.isEmpty()) {
            permissionLauncher.launch(needed.toArray(new String[0]));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController != null && navController.navigateUp()
                || super.onSupportNavigateUp();
    }
}
