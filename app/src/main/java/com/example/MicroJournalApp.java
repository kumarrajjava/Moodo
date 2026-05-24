package com.example;

import android.app.Application;

public class MicroJournalApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        JournalPrefs.applySavedTheme(this);
    }
}
