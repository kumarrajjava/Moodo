package com.example;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerJournals;
    private JournalAdapter adapter;
    private List<JournalEntry> entriesList = new ArrayList<>();
    private JournalDatabaseHelper dbHelper;

    private SwipeRefreshLayout swipeRefresh;
    private EditText editSearch;
    private LinearLayout layoutEmptyState;
    private TextView textEmptyHeading;
    private TextView textEmptySubheading;
    private FloatingActionButton fabAddEntry;
    private ImageButton btnExport;
    private ImageButton btnSettings;
    private MaterialCardView cardDailyPrompt;
    private MaterialCardView cardOnThisDay;
    private TextView textDailyPrompt;
    private TextView textOnThisDay;
    private TextView textStreakCount;
    private TextView textTotalEntries;
    private TextView textDominantMood;
    private TextView textStreakMessage;

    private TextView statCountHappy;
    private TextView statCountCalm;
    private TextView statCountThoughtful;
    private TextView statCountEnergetic;
    private TextView statCountGrateful;
    private TextView statCountAnxious;

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

        swipeRefresh = findViewById(R.id.swipe_refresh);
        recyclerJournals = findViewById(R.id.recycler_journals);
        editSearch = findViewById(R.id.edit_search);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        textEmptyHeading = findViewById(R.id.text_empty_heading);
        textEmptySubheading = findViewById(R.id.text_empty_subheading);
        fabAddEntry = findViewById(R.id.fab_add_entry);
        btnExport = findViewById(R.id.btn_export);
        btnSettings = findViewById(R.id.btn_settings);
        cardDailyPrompt = findViewById(R.id.card_daily_prompt);
        cardOnThisDay = findViewById(R.id.card_on_this_day);
        textDailyPrompt = findViewById(R.id.text_daily_prompt);
        textOnThisDay = findViewById(R.id.text_on_this_day);
        textStreakCount = findViewById(R.id.text_streak_count);
        textTotalEntries = findViewById(R.id.text_total_entries);
        textDominantMood = findViewById(R.id.text_dominant_mood);
        textStreakMessage = findViewById(R.id.text_streak_message);

        statCountHappy = findViewById(R.id.stat_count_happy);
        statCountCalm = findViewById(R.id.stat_count_calm);
        statCountThoughtful = findViewById(R.id.stat_count_thoughtful);
        statCountEnergetic = findViewById(R.id.stat_count_energetic);
        statCountGrateful = findViewById(R.id.stat_count_grateful);
        statCountAnxious = findViewById(R.id.stat_count_anxious);

        btnFilterAll = findViewById(R.id.btn_filter_all);
        btnFilterHappy = findViewById(R.id.btn_filter_happy);
        btnFilterCalm = findViewById(R.id.btn_filter_calm);
        btnFilterThoughtful = findViewById(R.id.btn_filter_thoughtful);
        btnFilterEnergetic = findViewById(R.id.btn_filter_energetic);
        btnFilterGrateful = findViewById(R.id.btn_filter_grateful);
        btnFilterAnxious = findViewById(R.id.btn_filter_anxious);
        filterButtons = new Button[]{btnFilterAll, btnFilterHappy, btnFilterCalm, btnFilterThoughtful, btnFilterEnergetic, btnFilterGrateful, btnFilterAnxious};

        recyclerJournals.setLayoutManager(new LinearLayoutManager(this));
        adapter = new JournalAdapter(entriesList, entry -> {
            Intent intent = new Intent(MainActivity.this, WriteEntryActivity.class);
            intent.putExtra(WriteEntryActivity.EXTRA_ENTRY_ID, entry.getId());
            startActivity(intent);
        }, this::showOptionsDialog);
        recyclerJournals.setAdapter(adapter);

        setupSwipeToDelete();
        swipeRefresh.setColorSchemeResources(R.color.journal_primary);
        swipeRefresh.setOnRefreshListener(this::refreshData);

        fabAddEntry.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, WriteEntryActivity.class)));
        btnExport.setOnClickListener(v -> exportJournalBackup());
        btnSettings.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));

        cardDailyPrompt.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WriteEntryActivity.class);
            intent.putExtra(WriteEntryActivity.EXTRA_PROMPT, JournalPrompts.getDailyPrompt());
            startActivity(intent);
        });

        setupFilters();
        setupSearch();
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private final ColorDrawable background = new ColorDrawable(Color.parseColor("#E53935"));
            private final Drawable deleteIcon = ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_menu_delete);

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    return;
                }
                JournalEntry deleted = adapter.getEntryAt(position);
                if (deleted == null) {
                    refreshData();
                    return;
                }
                dbHelper.deleteEntry(deleted.getId());
                refreshData();
                Snackbar.make(recyclerJournals, R.string.entry_deleted, Snackbar.LENGTH_LONG)
                        .setAction("Undo", v -> {
                            dbHelper.restoreEntry(deleted);
                            refreshData();
                        })
                        .show();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                if (dX < 0) {
                    background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    background.draw(c);
                    if (deleteIcon != null) {
                        int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;
                        int iconTop = itemView.getTop() + iconMargin;
                        int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
                        deleteIcon.setBounds(iconLeft, iconTop, iconLeft + deleteIcon.getIntrinsicWidth(), iconTop + deleteIcon.getIntrinsicHeight());
                        deleteIcon.draw(c);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerJournals);
    }

    private void exportJournalBackup() {
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

    @Override
    protected void onResume() {
        super.onResume();
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
        List<JournalEntry> filteredEntries;

        if (!currentSearchQuery.isEmpty()) {
            List<JournalEntry> searchRaw = dbHelper.searchEntries(currentSearchQuery);
            filteredEntries = new ArrayList<>();
            for (JournalEntry entry : searchRaw) {
                if (selectedMoodFilter.equals("All") || entry.getMood().equals(selectedMoodFilter)) {
                    filteredEntries.add(entry);
                }
            }
        } else if (!selectedMoodFilter.equals("All")) {
            filteredEntries = dbHelper.getEntriesByMood(selectedMoodFilter);
        } else {
            filteredEntries = dbHelper.getAllEntries();
        }

        entriesList = filteredEntries;
        adapter.updateList(entriesList);

        int totalInDb = dbHelper.getTotalEntryCount();
        boolean isFilteredView = !currentSearchQuery.isEmpty() || !selectedMoodFilter.equals("All");

        if (entriesList.isEmpty()) {
            recyclerJournals.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
            if (totalInDb == 0) {
                textEmptyHeading.setText(R.string.empty_journal_heading);
                textEmptySubheading.setText(R.string.empty_journal_subheading);
            } else if (isFilteredView) {
                textEmptyHeading.setText(R.string.empty_filter_heading);
                textEmptySubheading.setText(R.string.empty_filter_subheading);
            } else {
                textEmptyHeading.setText(R.string.empty_journal_heading);
                textEmptySubheading.setText(R.string.empty_journal_subheading);
            }
        } else {
            recyclerJournals.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }

        updateInsightsCard();
        updateQuickStats();
        updateOnThisDayCard();
        textDailyPrompt.setText(JournalPrompts.getDailyPrompt());
        swipeRefresh.setRefreshing(false);
    }

    private void updateOnThisDayCard() {
        List<JournalEntry> memories = dbHelper.getOnThisDayMemories(3);
        if (memories.isEmpty()) {
            cardOnThisDay.setVisibility(View.GONE);
            return;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < memories.size(); i++) {
            JournalEntry entry = memories.get(i);
            if (i > 0) {
                builder.append("\n\n");
            }
            Calendar cal = Calendar.getInstance(Locale.getDefault());
            cal.setTimeInMillis(entry.getTimestamp());
            String year = DateFormat.format("yyyy", cal).toString();
            builder.append(getString(R.string.on_this_day_format, year, entry.getTitle()));
        }
        textOnThisDay.setText(builder.toString());
        cardOnThisDay.setVisibility(View.VISIBLE);
    }

    private void updateQuickStats() {
        int streak = dbHelper.getCurrentStreak();
        int total = dbHelper.getTotalEntryCount();
        textStreakCount.setText(String.valueOf(streak));
        textTotalEntries.setText(String.valueOf(total));
        textStreakMessage.setText(streak > 0 ? R.string.streak_message_active : R.string.streak_message_zero);

        String dominant = dbHelper.getDominantMood();
        if (dominant != null) {
            textDominantMood.setText(JournalUtils.moodWithEmoji(dominant));
        } else {
            textDominantMood.setText(getString(R.string.no_dominant_mood));
        }
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
        String pinLabel = entry.isPinned() ? getString(R.string.unpin_entry) : getString(R.string.pin_entry);
        String[] options = {
                pinLabel,
                getString(R.string.share_entry),
                getString(R.string.edit_thoughts),
                getString(R.string.delete_thoughts)
        };
        new AlertDialog.Builder(this)
                .setTitle(entry.getTitle())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        boolean wasPinned = entry.isPinned();
                        dbHelper.togglePin(entry.getId());
                        Toast.makeText(MainActivity.this,
                                wasPinned ? R.string.unpinned_toast : R.string.pinned_toast,
                                Toast.LENGTH_SHORT).show();
                        refreshData();
                    } else if (which == 1) {
                        shareEntry(entry);
                    } else if (which == 2) {
                        Intent intent = new Intent(MainActivity.this, WriteEntryActivity.class);
                        intent.putExtra(WriteEntryActivity.EXTRA_ENTRY_ID, entry.getId());
                        startActivity(intent);
                    } else if (which == 3) {
                        confirmDeletion(entry);
                    }
                })
                .show();
    }

    private void shareEntry(JournalEntry entry) {
        String shareText = entry.getTitle() + "\n\n"
                + entry.getContent() + "\n\n— "
                + JournalUtils.moodWithEmoji(entry.getMood()) + "\n"
                + getString(R.string.share_entry_footer);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_entry)));
    }

    private void confirmDeletion(final JournalEntry entry) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(getString(R.string.delete_confirm_message, entry.getTitle()))
                .setPositiveButton(R.string.delete_thoughts, (dialog, which) -> {
                    int deleted = dbHelper.deleteEntry(entry.getId());
                    if (deleted > 0) {
                        Toast.makeText(MainActivity.this, R.string.entry_deleted, Toast.LENGTH_SHORT).show();
                        refreshData();
                    } else {
                        Toast.makeText(MainActivity.this, R.string.delete_error, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
