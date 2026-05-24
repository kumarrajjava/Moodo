package com.example;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerJournals;
    private JournalAdapter adapter;
    private List<JournalEntry> entriesList = new ArrayList<>();
    private JournalDatabaseHelper dbHelper;

    private EditText editSearch;
    private LinearLayout layoutEmptyState;
    private FloatingActionButton fabAddEntry;

    // Stat Count Views
    private TextView statCountHappy;
    private TextView statCountCalm;
    private TextView statCountThoughtful;
    private TextView statCountEnergetic;
    private TextView statCountGrateful;
    private TextView statCountAnxious;

    // Mood Filter buttons
    private Button btnFilterAll;
    private Button btnFilterHappy;
    private Button btnFilterCalm;
    private Button btnFilterThoughtful;
    private Button btnFilterEnergetic;
    private Button btnFilterGrateful;
    private Button btnFilterAnxious;
    private Button[] filterButtons;

    private String selectedMoodFilter = "All";
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new JournalDatabaseHelper(this);

        // Bind Views
        recyclerJournals = findViewById(R.id.recycler_journals);
        editSearch = findViewById(R.id.edit_search);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        fabAddEntry = findViewById(R.id.fab_add_entry);

        // Stats Counters
        statCountHappy = findViewById(R.id.stat_count_happy);
        statCountCalm = findViewById(R.id.stat_count_calm);
        statCountThoughtful = findViewById(R.id.stat_count_thoughtful);
        statCountEnergetic = findViewById(R.id.stat_count_energetic);
        statCountGrateful = findViewById(R.id.stat_count_grateful);
        statCountAnxious = findViewById(R.id.stat_count_anxious);

        // Filter Buttons
        btnFilterAll = findViewById(R.id.btn_filter_all);
        btnFilterHappy = findViewById(R.id.btn_filter_happy);
        btnFilterCalm = findViewById(R.id.btn_filter_calm);
        btnFilterThoughtful = findViewById(R.id.btn_filter_thoughtful);
        btnFilterEnergetic = findViewById(R.id.btn_filter_energetic);
        btnFilterGrateful = findViewById(R.id.btn_filter_grateful);
        btnFilterAnxious = findViewById(R.id.btn_filter_anxious);
        filterButtons = new Button[]{btnFilterAll, btnFilterHappy, btnFilterCalm, btnFilterThoughtful, btnFilterEnergetic, btnFilterGrateful, btnFilterAnxious};

        // Initialize Recycler
        recyclerJournals.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JournalAdapter(entriesList, new JournalAdapter.OnEntryClickListener() {
            @Override
            public void onEntryClick(JournalEntry entry) {
                // Clicking an item opens write screen in EDIT mode
                Intent intent = new Intent(MainActivity.this, WriteEntryActivity.class);
                intent.putExtra(WriteEntryActivity.EXTRA_ENTRY_ID, entry.getId());
                startActivity(intent);
            }
        }, new JournalAdapter.OnEntryLongClickListener() {
            @Override
            public void onEntryLongClick(JournalEntry entry) {
                // Long press gives menu: Edit or Delete
                showOptionsDialog(entry);
            }
        });
        recyclerJournals.setAdapter(adapter);

        // Setup FAB Compose Click
        fabAddEntry.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WriteEntryActivity.class);
            startActivity(intent);
        });

        setupFilters();
        setupSearch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load data on resume to refresh results dynamically
        refreshData();
    }

    private void setupFilters() {
        btnFilterAll.setOnClickListener(v -> selectFilter("All"));
        btnFilterHappy.setOnClickListener(v -> selectFilter("Happy"));
        btnFilterCalm.setOnClickListener(v -> selectFilter("Calm"));
        btnFilterThoughtful.setOnClickListener(v -> selectFilter("Thoughtful"));
        btnFilterEnergetic.setOnClickListener(v -> selectFilter("Energetic"));
        btnFilterGrateful.setOnClickListener(v -> selectFilter("Grateful"));
        btnFilterAnxious.setOnClickListener(v -> selectFilter("Anxious"));

        // Highlight "All" by default
        highlightFilterButton("All");
    }

    private void selectFilter(String mood) {
        selectedMoodFilter = mood;
        highlightFilterButton(mood);
        refreshData();
    }

    private void highlightFilterButton(String mood) {
        for (Button btn : filterButtons) {
            String btnText = btn.getText().toString();
            boolean isMatch = (mood.equals("All") && btnText.equals(getString(R.string.mood_filter_all))) || btnText.startsWith(mood);
            if (isMatch) {
                btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.journal_primary)));
                btn.setTextColor(Color.WHITE);
            } else {
                btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.journal_primary_light)));
                btn.setTextColor(ContextCompat.getColor(this, R.color.journal_primary));
            }
        }
    }

    private void setupSearch() {
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                refreshData();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void refreshData() {
        // Query logic based on selected filter and text query
        List<JournalEntry> filteredEntries;

        if (!currentSearchQuery.isEmpty()) {
            // Text search (which matches against titles/contents from entire DB)
            List<JournalEntry> searchRaw = dbHelper.searchEntries(currentSearchQuery);
            filteredEntries = new ArrayList<>();
            // apply mood filter locally if search is active
            for (JournalEntry entry : searchRaw) {
                if (selectedMoodFilter.equals("All") || entry.getMood().equals(selectedMoodFilter)) {
                    filteredEntries.add(entry);
                }
            }
        } else if (!selectedMoodFilter.equals("All")) {
            // Mood filter active
            filteredEntries = dbHelper.getEntriesByMood(selectedMoodFilter);
        } else {
            // Unfiltered read
            filteredEntries = dbHelper.getAllEntries();
        }

        entriesList = filteredEntries;
        adapter.updateList(entriesList);

        // Update view visibility
        if (entriesList.isEmpty()) {
            recyclerJournals.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerJournals.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }

        // Always update insights stats card
        updateInsightsCard();
    }

    private void updateInsightsCard() {
        Map<String, Integer> distribution = dbHelper.getMoodDistribution();

        statCountHappy.setText(String.valueOf(distribution.get("Happy")));
        statCountCalm.setText(String.valueOf(distribution.get("Calm")));
        statCountThoughtful.setText(String.valueOf(distribution.get("Thoughtful")));
        statCountEnergetic.setText(String.valueOf(distribution.get("Energetic")));
        statCountGrateful.setText(String.valueOf(distribution.get("Grateful")));
        statCountAnxious.setText(String.valueOf(distribution.get("Anxious")));
    }

    private void showOptionsDialog(final JournalEntry entry) {
        String[] options = {"Edit Thoughts", "Delete Thoughts"};
        new AlertDialog.Builder(this)
                .setTitle("Select Option")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // Edit
                            Intent intent = new Intent(MainActivity.this, WriteEntryActivity.class);
                            intent.putExtra(WriteEntryActivity.EXTRA_ENTRY_ID, entry.getId());
                            startActivity(intent);
                        } else if (which == 1) {
                            // Delete
                            confirmDeletion(entry);
                        }
                    }
                })
                .show();
    }

    private void confirmDeletion(final JournalEntry entry) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Permanently?")
                .setMessage("Are you sure you want to permanently delete \"" + entry.getTitle() + "\"? This cannot be undone.")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int deleted = dbHelper.deleteEntry(entry.getId());
                        if (deleted > 0) {
                            Toast.makeText(MainActivity.this, "Passed thoughts deleted.", Toast.LENGTH_SHORT).show();
                            refreshData();
                        } else {
                            Toast.makeText(MainActivity.this, "Error deleting thoughts.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
