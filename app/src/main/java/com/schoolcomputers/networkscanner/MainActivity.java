package com.schoolcomputers.networkscanner;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.schoolcomputers.networkscanner.ui.history.HistoryFragment;
import com.schoolcomputers.networkscanner.ui.scanner.ScannerFragment;
import com.schoolcomputers.networkscanner.ui.settings.SettingsFragment;
import com.schoolcomputers.networkscanner.util.PermissionManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        permissionManager = new PermissionManager(this);
        permissionManager.checkAndRequestPermissions(new PermissionManager.PermissionCallback() {
            @Override
            public void onPermissionsGranted() {
                // Permissions granted, you can proceed with network scanning
            }

            @Override
            public void onPermissionsDenied(List<String> deniedPermissions) {
                // Handle denied permissions (e.g., show a message or disable features)
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = getString(R.string.app_name);
            int itemId = item.getItemId();

            if (itemId == R.id.nav_scanner) {
                selectedFragment = new ScannerFragment();
                title = "Dashboard";
            } else if (itemId == R.id.nav_history) {
                selectedFragment = new HistoryFragment();
                title = "History";
            } else if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
                title = "Settings";
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.nav_host_fragment, selectedFragment)
                        .commit();
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(title);
                }
                return true;
            }
            return false;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, new ScannerFragment())
                    .commit();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Dashboard");
            }
        }
    }
}
