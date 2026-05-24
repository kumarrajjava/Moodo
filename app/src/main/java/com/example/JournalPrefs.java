package com.example;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public final class JournalPrefs {

    private static final String PREFS_NAME = "micro_journal_prefs";
    private static final String KEY_ONBOARDING_DONE = "onboarding_done";
    private static final String KEY_DARK_MODE = "dark_mode";

    private JournalPrefs() {}

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isOnboardingDone(Context context) {
        return prefs(context).getBoolean(KEY_ONBOARDING_DONE, false);
    }

    public static void setOnboardingDone(Context context) {
        prefs(context).edit().putBoolean(KEY_ONBOARDING_DONE, true).apply();
    }

    public static boolean isDarkMode(Context context) {
        return prefs(context).getBoolean(KEY_DARK_MODE, false);
    }

    public static void setDarkMode(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_DARK_MODE, enabled).apply();
        applyTheme(enabled);
    }

    public static void applySavedTheme(Context context) {
        applyTheme(isDarkMode(context));
    }

    public static void applyTheme(boolean darkMode) {
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}
