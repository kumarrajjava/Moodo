package com.example;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.google.android.material.card.MaterialCardView;

public class SettingsActivity extends AppCompatActivity {

    private JournalDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dbHelper = new JournalDatabaseHelper(this);

        ImageButton btnBack = findViewById(R.id.btn_settings_back);
        SwitchCompat switchDarkMode = findViewById(R.id.switch_dark_mode);
        TextView textVersion = findViewById(R.id.text_app_version);
        MaterialCardView cardExport = findViewById(R.id.card_settings_export);
        MaterialCardView cardPrivacy = findViewById(R.id.card_settings_privacy);
        MaterialCardView cardAbout = findViewById(R.id.card_settings_about);
        MaterialCardView cardClearData = findViewById(R.id.card_settings_clear);

        btnBack.setOnClickListener(v -> finish());

        switchDarkMode.setChecked(JournalPrefs.isDarkMode(this));
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) ->
                JournalPrefs.setDarkMode(SettingsActivity.this, isChecked));

        textVersion.setText(getString(R.string.settings_version_format, getVersionName()));

        cardExport.setOnClickListener(v -> exportBackup());
        cardPrivacy.setOnClickListener(v -> showPrivacyPolicy());
        cardAbout.setOnClickListener(v -> showAboutDialog());
        cardClearData.setOnClickListener(v -> confirmClearAllData());
    }

    private String getVersionName() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            return info.versionName != null ? info.versionName : "1.0";
        } catch (PackageManager.NameNotFoundException e) {
            return "1.0";
        }
    }

    private void exportBackup() {
        int count = dbHelper.getTotalEntryCount();
        if (count == 0) {
            Toast.makeText(this, R.string.export_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        String json = dbHelper.exportAllEntriesAsJson();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.export_subject));
        shareIntent.putExtra(Intent.EXTRA_TEXT, json);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.export_journal)));
    }

    private void showPrivacyPolicy() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_privacy)
                .setMessage(R.string.privacy_policy_text)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_about)
                .setMessage(getString(R.string.about_dialog_text, getVersionName()))
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    private void confirmClearAllData() {
        if (dbHelper.getTotalEntryCount() == 0) {
            Toast.makeText(this, R.string.clear_data_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.clear_data_title)
                .setMessage(R.string.clear_data_message)
                .setPositiveButton(R.string.clear_data_confirm, (dialog, which) -> {
                    dbHelper.clearAllEntries();
                    Toast.makeText(this, R.string.clear_data_done, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
