package com.trantrongmanh.lab6;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.trantrongmanh.lab6.utils.AnimUtil;
import com.trantrongmanh.lab6.utils.FirebaseAuthHelper;

/**
 * Splash screen with animated logo and tagline.
 * Checks auth state and routes to Login or MainActivity.
 * Demonstrates: Java-based UI animation, Handler for delayed navigation, Intent (explicit).
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Reference views
        ImageView ivLogo = findViewById(R.id.ivLogo);
        TextView tvAppName = findViewById(R.id.tvAppName);
        TextView tvTagline = findViewById(R.id.tvTagline);

        // Apply programmatic animations (Java-based UI animation)
        AnimUtil.popIn(ivLogo, 800);
        AnimUtil.fadeIn(tvAppName, 800);
        AnimUtil.slideInFromBottom(tvTagline, 800, 400);

        // Delayed navigation using Handler
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseAuthHelper authHelper = new FirebaseAuthHelper();
            Intent intent;
            if (authHelper.isLoggedIn()) {
                // User already logged in -> go to gallery
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // Not logged in -> go to login
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            // Activity transition animation
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }, SPLASH_DELAY);
    }
}
