package com.schoolcomputers.networkscanner;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

/**
 * Application class — runs once when the app process starts.
 * Initializes Firebase and configures Firestore settings.
 */
public class NetworkScannerApp extends Application {

    private static final String TAG = "NetworkScannerApp";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase explicitly (usually automatic, but this ensures it)
        FirebaseApp.initializeApp(this);

        // Configure Firestore — disable offline persistence to avoid
        // the "hangs forever on first call" issue during development.
        // Firestore will go straight to the network instead of checking
        // a local cache first.
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)   // go straight to network
                .build();
        db.setFirestoreSettings(settings);

        Log.d(TAG, "Firebase and Firestore initialized.");
    }
}
