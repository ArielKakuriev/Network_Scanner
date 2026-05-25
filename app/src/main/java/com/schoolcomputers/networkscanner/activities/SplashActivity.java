package com.schoolcomputers.networkscanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.schoolcomputers.networkscanner.R;

/**
 * Splash screen.
 * Checks Firebase Auth for an existing session — no SQLite involved.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION_MS = 1800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo  = findViewById(R.id.ivSplashLogo);
        TextView  title = findViewById(R.id.tvSplashTitle);

        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(1000);
        fadeIn.setFillAfter(true);
        logo.startAnimation(fadeIn);
        title.startAnimation(fadeIn);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Firebase keeps the session alive automatically between app launches
            boolean isSignedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
            Intent intent = new Intent(this,
                    isSignedIn ? MainActivity.class : AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_DURATION_MS);
    }
}
