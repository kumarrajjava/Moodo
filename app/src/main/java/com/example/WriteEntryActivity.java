package com.example;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.card.MaterialCardView;
import java.util.HashMap;
import java.util.Map;

public class WriteEntryActivity extends AppCompatActivity {

    public static final String EXTRA_ENTRY_ID = "com.example.EXTRA_ENTRY_ID";
    public static final String EXTRA_PROMPT = "com.example.EXTRA_PROMPT";

    private EditText editTitle;
    private EditText editContent;
    private TextView textWordCount;
    private ImageButton btnBack;
    private Button btnSave;
    private Button btnCancel;
    private MaterialCardView cardWritingPad;
    private TextView textToolbarTitle;

    // Mood selection buttons
    private Button btnMoodHappy;
    private Button btnMoodCalm;
    private Button btnMoodThoughtful;
    private Button btnMoodEnergetic;
    private Button btnMoodGrateful;
    private Button btnMoodAnxious;
    private Button[] moodButtons;

    // Color palette circular selectors
    private View colorWhite;
    private View colorPeach;
    private View colorMint;
    private View colorThoughtful;
    private View colorRose;
    private View colorBlue;
    private View colorLavender;
    private View[] colorSelectors;

    private JournalDatabaseHelper dbHelper;
    private long entryId = -1;

    private String selectedMood = "Happy";
    private String selectedColorHex = "#FFFFFF";

    // Maps moods to background colors for auto-coordination
    private final Map<String, String> moodToColorMap = new HashMap<String, String>() {{
        put("Happy", "#FFF5CC");      // Peach
        put("Calm", "#E2F0D9");       // Mint
        put("Thoughtful", "#E5E8EB");   // Slate grey
        put("Energetic", "#FFE6E6");   // Soft rose
        put("Grateful", "#E1F0FF");    // Light blue
        put("Anxious", "#F2E6FF");     // Lavender
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_entry);

        dbHelper = new JournalDatabaseHelper(this);

        // Bind layouts
        editTitle = findViewById(R.id.edit_entry_title);
        editContent = findViewById(R.id.edit_entry_content);
        textWordCount = findViewById(R.id.text_word_count);
        btnBack = findViewById(R.id.btn_back);
        btnSave = findViewById(R.id.btn_save);
        btnCancel = findViewById(R.id.btn_cancel);
        cardWritingPad = findViewById(R.id.card_writing_pad);
        textToolbarTitle = findViewById(R.id.text_toolbar_title);

        // Mood Buttons
        btnMoodHappy = findViewById(R.id.btn_mood_happy);
        btnMoodCalm = findViewById(R.id.btn_mood_calm);
        btnMoodThoughtful = findViewById(R.id.btn_mood_thoughtful);
        btnMoodEnergetic = findViewById(R.id.btn_mood_energetic);
        btnMoodGrateful = findViewById(R.id.btn_mood_grateful);
        btnMoodAnxious = findViewById(R.id.btn_mood_anxious);
        moodButtons = new Button[]{btnMoodHappy, btnMoodCalm, btnMoodThoughtful, btnMoodEnergetic, btnMoodGrateful, btnMoodAnxious};

        // Color selectors
        colorWhite = findViewById(R.id.color_selector_white);
        colorPeach = findViewById(R.id.color_selector_peach);
        colorMint = findViewById(R.id.color_selector_mint);
        colorThoughtful = findViewById(R.id.color_selector_thoughtful);
        colorRose = findViewById(R.id.color_selector_rose);
        colorBlue = findViewById(R.id.color_selector_blue);
        colorLavender = findViewById(R.id.color_selector_lavender);
        colorSelectors = new View[]{colorWhite, colorPeach, colorMint, colorThoughtful, colorRose, colorBlue, colorLavender};

        setupMoodClickListeners();
        setupColorClickListeners();
        setupWordCounter();

        // Check if editing or adding
        if (getIntent().hasExtra(EXTRA_ENTRY_ID)) {
            entryId = getIntent().getLongExtra(EXTRA_ENTRY_ID, -1);
        }

        if (entryId != -1) {
            textToolbarTitle.setText("Edit Reflections");
            loadEntryData(entryId);
        } else {
            textToolbarTitle.setText("New Thoughts");
            selectMood("Happy");
            updateCanvasColor("#FFFFFF");
            if (getIntent().hasExtra(EXTRA_PROMPT)) {
                String prompt = getIntent().getStringExtra(EXTRA_PROMPT);
                if (prompt != null && !prompt.isEmpty()) {
                    editContent.setText(prompt + "\n\n");
                    editContent.setSelection(editContent.getText().length());
                }
            }
        }

        btnBack.setOnClickListener(v -> handleBackNavigation());
        btnCancel.setOnClickListener(v -> handleBackNavigation());
        btnSave.setOnClickListener(v -> saveEntry());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackNavigation();
            }
        });
    }

    private void setupMoodClickListeners() {
        btnMoodHappy.setOnClickListener(v -> selectMoodAndCoordinateColor("Happy"));
        btnMoodCalm.setOnClickListener(v -> selectMoodAndCoordinateColor("Calm"));
        btnMoodThoughtful.setOnClickListener(v -> selectMoodAndCoordinateColor("Thoughtful"));
        btnMoodEnergetic.setOnClickListener(v -> selectMoodAndCoordinateColor("Energetic"));
        btnMoodGrateful.setOnClickListener(v -> selectMoodAndCoordinateColor("Grateful"));
        btnMoodAnxious.setOnClickListener(v -> selectMoodAndCoordinateColor("Anxious"));
    }

    private void setupWordCounter() {
        editContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateWordCount(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        updateWordCount(editContent.getText().toString());
    }

    private void updateWordCount(String text) {
        int words = JournalUtils.countWords(text);
        if (words == 0) {
            textWordCount.setText(R.string.word_count_zero);
        } else {
            textWordCount.setText(getString(R.string.word_count_format, words, JournalUtils.readingTimeMinutes(words)));
        }
    }

    private void setupColorClickListeners() {
        colorWhite.setOnClickListener(v -> updateCanvasColor("#FFFFFF"));
        colorPeach.setOnClickListener(v -> updateCanvasColor("#FFF5CC"));
        colorMint.setOnClickListener(v -> updateCanvasColor("#E2F0D9"));
        colorThoughtful.setOnClickListener(v -> updateCanvasColor("#E5E8EB"));
        colorRose.setOnClickListener(v -> updateCanvasColor("#FFE6E6"));
        colorBlue.setOnClickListener(v -> updateCanvasColor("#E1F0FF"));
        colorLavender.setOnClickListener(v -> updateCanvasColor("#F2E6FF"));
    }

    private void selectMood(String mood) {
        selectedMood = mood;
        for (Button btn : moodButtons) {
            String btnText = btn.getText().toString();
            if (btnText.startsWith(mood)) {
                // Highlight Selected Button
                btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.journal_primary)));
                btn.setTextColor(Color.WHITE);
            } else {
                // Reset standard style
                btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.journal_primary_light)));
                btn.setTextColor(ContextCompat.getColor(this, R.color.journal_primary));
            }
        }
    }

    // Auto-select corresponding pastel bg matching clicked emotion, but let them change custom color later
    private void selectMoodAndCoordinateColor(String mood) {
        selectMood(mood);
        if (moodToColorMap.containsKey(mood)) {
            String colorHex = moodToColorMap.get(mood);
            updateCanvasColor(colorHex);
        }
    }

    private void updateCanvasColor(String hexColor) {
        selectedColorHex = hexColor;

        // Apply background tint to writing pad
        try {
            cardWritingPad.setCardBackgroundColor(ColorStateList.valueOf(Color.parseColor(hexColor)));
        } catch (Exception e) {
            cardWritingPad.setCardBackgroundColor(ColorStateList.valueOf(Color.WHITE));
        }

        // Highlight chosen color selector
        for (View v : colorSelectors) {
            // Check if this view matches the selected color
            ColorStateList tintList = v.getBackgroundTintList();
            if (tintList != null) {
                int tintColor = tintList.getDefaultColor();
                int targetColor = Color.parseColor(hexColor);
                if (tintColor == targetColor) {
                    v.setScaleX(1.15f);
                    v.setScaleY(1.15f);
                    // Add outline
                    v.setElevation(8f);
                } else {
                    v.setScaleX(1.0f);
                    v.setScaleY(1.0f);
                    v.setElevation(0f);
                }
            }
        }
    }

    private void loadEntryData(long id) {
        JournalEntry entry = dbHelper.getEntryById(id);
        if (entry != null) {
            editTitle.setText(entry.getTitle());
            editContent.setText(entry.getContent());
            selectMood(entry.getMood());
            updateCanvasColor(entry.getColorHex());
            updateWordCount(entry.getContent());
        } else {
            Toast.makeText(this, "Could not load journal data.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void saveEntry() {
        String title = editTitle.getText().toString().trim();
        String content = editContent.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a journal title.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (content.isEmpty()) {
            Toast.makeText(this, "Please start writing your journal thoughts.", Toast.LENGTH_SHORT).show();
            return;
        }

        long result;
        if (entryId == -1) {
            // New Entry
            result = dbHelper.insertEntry(title, content, selectedMood, selectedColorHex, System.currentTimeMillis());
            if (result != -1) {
                Toast.makeText(this, "Thoughts recorded successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error saving thoughts.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Edit Entry
            int rows = dbHelper.updateEntry(entryId, title, content, selectedMood, selectedColorHex);
            if (rows > 0) {
                Toast.makeText(this, "Thoughts updated successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error updating thoughts.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleBackNavigation() {
        String title = editTitle.getText().toString().trim();
        String content = editContent.getText().toString().trim();

        if (!title.isEmpty() || !content.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Discard Changes?")
                    .setMessage("Are you sure you want to exit without saving your journal?")
                    .setPositiveButton("Discard", (dialog, which) -> finish())
                    .setNegativeButton("Keep Writing", null)
                    .show();
        } else {
            finish();
        }
    }
}
