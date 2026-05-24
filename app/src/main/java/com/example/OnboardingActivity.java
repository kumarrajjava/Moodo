package com.example;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {

    private int step = 0;
    private TextView textTitle;
    private TextView textDescription;
    private ImageView imageOnboarding;
    private TextView textStepIndicator;
    private Button btnNext;

    private final int[] titles = {
            R.string.onboarding_title_1,
            R.string.onboarding_title_2,
            R.string.onboarding_title_3
    };
    private final int[] descriptions = {
            R.string.onboarding_desc_1,
            R.string.onboarding_desc_2,
            R.string.onboarding_desc_3
    };
    private final int[] images = {
            R.drawable.ic_journal_book,
            R.drawable.ic_streak,
            R.drawable.ic_star_filled
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        textTitle = findViewById(R.id.text_onboarding_title);
        textDescription = findViewById(R.id.text_onboarding_description);
        imageOnboarding = findViewById(R.id.image_onboarding);
        textStepIndicator = findViewById(R.id.text_step_indicator);
        btnNext = findViewById(R.id.btn_onboarding_next);

        findViewById(R.id.btn_onboarding_skip).setOnClickListener(v -> finishOnboarding());
        btnNext.setOnClickListener(v -> {
            if (step < titles.length - 1) {
                step++;
                updateStep();
            } else {
                finishOnboarding();
            }
        });

        updateStep();
    }

    private void updateStep() {
        textTitle.setText(titles[step]);
        textDescription.setText(descriptions[step]);
        imageOnboarding.setImageResource(images[step]);
        textStepIndicator.setText(getString(R.string.onboarding_step_format, step + 1, titles.length));
        btnNext.setText(step == titles.length - 1 ? R.string.onboarding_get_started : R.string.onboarding_next);
    }

    private void finishOnboarding() {
        JournalPrefs.setOnboardingDone(this);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
