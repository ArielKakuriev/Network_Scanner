package com.schoolcomputers.networkscanner.util;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PermissionManager {

    public interface PermissionCallback {
        void onPermissionsGranted();
        void onPermissionsDenied(List<String> deniedPermissions);
    }

    private final AppCompatActivity activity;
    private final ActivityResultLauncher<String[]> requestPermissionLauncher;
    private PermissionCallback callback;

    public PermissionManager(@NonNull AppCompatActivity activity) {
        this.activity = activity;
        this.requestPermissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                this::handlePermissionResult
        );
    }

    public void checkAndRequestPermissions(PermissionCallback callback) {
        this.callback = callback;
        String[] permissions = getRequiredPermissions();
        List<String> permissionsToRequest = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (permissionsToRequest.isEmpty()) {
            if (callback != null) callback.onPermissionsGranted();
        } else {
            boolean shouldShowRationale = false;
            for (String permission : permissionsToRequest) {
                if (activity.shouldShowRequestPermissionRationale(permission)) {
                    shouldShowRationale = true;
                    break;
                }
            }

            if (shouldShowRationale) {
                showRationaleDialog(permissionsToRequest.toArray(new String[0]));
            } else {
                requestPermissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
            }
        }
    }

    private String[] getRequiredPermissions() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_WIFI_STATE);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        
        return permissions.toArray(new String[0]);
    }

    private void handlePermissionResult(Map<String, Boolean> result) {
        List<String> deniedPermissions = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : result.entrySet()) {
            if (!entry.getValue()) {
                deniedPermissions.add(entry.getKey());
            }
        }

        if (deniedPermissions.isEmpty()) {
            if (callback != null) callback.onPermissionsGranted();
        } else {
            if (callback != null) callback.onPermissionsDenied(deniedPermissions);
        }
    }

    private void showRationaleDialog(String[] permissions) {
        new AlertDialog.Builder(activity)
                .setTitle("Permissions Required")
                .setMessage("This app requires location and Wi-Fi permissions to scan your network, and notification permissions to keep you updated.")
                .setPositiveButton("Grant", (dialog, which) -> requestPermissionLauncher.launch(permissions))
                .setNegativeButton("Cancel", (dialog, which) -> {
                    if (callback != null) callback.onPermissionsDenied(Arrays.asList(permissions));
                })
                .setCancelable(false)
                .show();
    }
    
    public static boolean hasLocationPermission(AppCompatActivity activity) {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
