package com.example;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1200L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::routeNext, SPLASH_DELAY_MS);
    }

    private void routeNext() {
        Intent intent;
        if (JournalPrefs.isOnboardingDone(this)) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, OnboardingActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
