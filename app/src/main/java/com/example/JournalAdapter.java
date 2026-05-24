package com.example;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.JournalViewHolder> {

    private List<JournalEntry> entries = new ArrayList<>();
    private final OnEntryClickListener clickListener;
    private final OnEntryLongClickListener longClickListener;

    public interface OnEntryClickListener {
        void onEntryClick(JournalEntry entry);
    }

    public interface OnEntryLongClickListener {
        void onEntryLongClick(JournalEntry entry);
    }

    public JournalAdapter(List<JournalEntry> entries, OnEntryClickListener clickListener, OnEntryLongClickListener longClickListener) {
        if (entries != null) {
            this.entries = entries;
        }
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    public void updateList(List<JournalEntry> newEntries) {
        this.entries = newEntries != null ? newEntries : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Nullable
    public JournalEntry getEntryAt(int position) {
        if (position < 0 || position >= entries.size()) {
            return null;
        }
        return entries.get(position);
    }

    @NonNull
    @Override
    public JournalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_journal_entry, parent, false);
        return new JournalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalViewHolder holder, int position) {
        holder.bind(entries.get(position), clickListener, longClickListener);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class JournalViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardEntry;
        private final TextView textTitle;
        private final TextView textContent;
        private final TextView textMood;
        private final TextView textDate;
        private final TextView textMeta;
        private final ImageView iconPinned;
        private final LinearLayout layoutMoodBadge;
        private final Context context;

        public JournalViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            cardEntry = itemView.findViewById(R.id.card_entry);
            textTitle = itemView.findViewById(R.id.text_entry_title);
            textContent = itemView.findViewById(R.id.text_entry_content);
            textMood = itemView.findViewById(R.id.text_entry_mood);
            textDate = itemView.findViewById(R.id.text_entry_date);
            textMeta = itemView.findViewById(R.id.text_entry_meta);
            iconPinned = itemView.findViewById(R.id.icon_pinned);
            layoutMoodBadge = itemView.findViewById(R.id.layout_mood_badge);
        }

        public void bind(final JournalEntry entry, final OnEntryClickListener clickListener, final OnEntryLongClickListener longClickListener) {
            textTitle.setText(entry.getTitle());
            textContent.setText(entry.getContent());
            textMood.setText(JournalUtils.moodWithEmoji(entry.getMood()));
            textDate.setText(formatDate(entry.getTimestamp()));

            int words = entry.getWordCount();
            if (words == 0) {
                textMeta.setText(R.string.word_count_zero);
            } else {
                textMeta.setText(context.getString(R.string.word_count_format, words, entry.getReadingTimeMinutes()));
            }

            iconPinned.setVisibility(entry.isPinned() ? View.VISIBLE : View.GONE);

            try {
                int cardBgColor = Color.parseColor(entry.getColorHex());
                cardEntry.setCardBackgroundColor(ColorStateList.valueOf(cardBgColor));
            } catch (Exception e) {
                cardEntry.setCardBackgroundColor(ColorStateList.valueOf(Color.WHITE));
            }

            if (entry.isPinned()) {
                cardEntry.setStrokeWidth((int) (2 * context.getResources().getDisplayMetrics().density));
                cardEntry.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.journal_primary)));
            } else {
                cardEntry.setStrokeWidth((int) (1 * context.getResources().getDisplayMetrics().density));
                cardEntry.setStrokeColor(ColorStateList.valueOf(0x08000000));
            }

            styleMoodBadge(entry.getMood());

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onEntryClick(entry);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onEntryLongClick(entry);
                    return true;
                }
                return false;
            });
        }

        private void styleMoodBadge(String mood) {
            int bgRes;
            int textRes;

            switch (mood) {
                case "Happy":
                    bgRes = R.color.bg_mood_happy;
                    textRes = R.color.text_mood_happy;
                    break;
                case "Calm":
                    bgRes = R.color.bg_mood_calm;
                    textRes = R.color.text_mood_calm;
                    break;
                case "Thoughtful":
                    bgRes = R.color.bg_mood_thoughtful;
                    textRes = R.color.text_mood_thoughtful;
                    break;
                case "Energetic":
                    bgRes = R.color.bg_mood_energetic;
                    textRes = R.color.text_mood_energetic;
                    break;
                case "Grateful":
                    bgRes = R.color.bg_mood_grateful;
                    textRes = R.color.text_mood_grateful;
                    break;
                case "Anxious":
                    bgRes = R.color.bg_mood_anxious;
                    textRes = R.color.text_mood_anxious;
                    break;
                default:
                    bgRes = R.color.purple_200;
                    textRes = R.color.black;
                    break;
            }

            layoutMoodBadge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, bgRes)));
            textMood.setTextColor(ContextCompat.getColor(context, textRes));
        }

        private String formatDate(long timestamp) {
            Calendar cal = Calendar.getInstance(Locale.getDefault());
            cal.setTimeInMillis(timestamp);
            return DateFormat.format("MMM dd, yyyy · hh:mm a", cal).toString();
        }
    }
}
