package com.example;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class JournalDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "micro_journal.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_NAME = "journal_entries";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_MOOD = "mood";
    public static final String COLUMN_COLOR_HEX = "color_hex";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_IS_PINNED = "is_pinned";

    private static final String ORDER_DEFAULT = COLUMN_IS_PINNED + " DESC, " + COLUMN_TIMESTAMP + " DESC";

    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TITLE + " TEXT, " +
            COLUMN_CONTENT + " TEXT, " +
            COLUMN_MOOD + " TEXT, " +
            COLUMN_COLOR_HEX + " TEXT, " +
            COLUMN_TIMESTAMP + " INTEGER, " +
            COLUMN_IS_PINNED + " INTEGER DEFAULT 0" +
            ")";

    public JournalDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_IS_PINNED + " INTEGER DEFAULT 0");
        }
    }

    public long insertEntry(String title, String content, String mood, String colorHex, long timestamp) {
        return insertEntry(title, content, mood, colorHex, timestamp, false);
    }

    public long insertEntry(String title, String content, String mood, String colorHex, long timestamp, boolean pinned) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_CONTENT, content);
        values.put(COLUMN_MOOD, mood);
        values.put(COLUMN_COLOR_HEX, colorHex);
        values.put(COLUMN_TIMESTAMP, timestamp);
        values.put(COLUMN_IS_PINNED, pinned ? 1 : 0);
        long id = db.insert(TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public long restoreEntry(JournalEntry entry) {
        return insertEntry(entry.getTitle(), entry.getContent(), entry.getMood(), entry.getColorHex(), entry.getTimestamp(), entry.isPinned());
    }

    public List<JournalEntry> getAllEntries() {
        return queryEntries(null, null);
    }

    public List<JournalEntry> getEntriesByMood(String mood) {
        return queryEntries(COLUMN_MOOD + " = ?", new String[]{mood});
    }

    public List<JournalEntry> searchEntries(String queryText) {
        String selection = COLUMN_TITLE + " LIKE ? OR " + COLUMN_CONTENT + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + queryText + "%", "%" + queryText + "%"};
        return queryEntries(selection, selectionArgs);
    }

    private List<JournalEntry> queryEntries(String selection, String[] selectionArgs) {
        List<JournalEntry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, ORDER_DEFAULT);

        if (cursor.moveToFirst()) {
            do {
                entries.add(createEntryFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return entries;
    }

    public JournalEntry getEntryById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        JournalEntry entry = null;
        if (cursor.moveToFirst()) {
            entry = createEntryFromCursor(cursor);
        }
        cursor.close();
        db.close();
        return entry;
    }

    public int updateEntry(long id, String title, String content, String mood, String colorHex) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_CONTENT, content);
        values.put(COLUMN_MOOD, mood);
        values.put(COLUMN_COLOR_HEX, colorHex);
        int rows = db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public int togglePin(long id) {
        JournalEntry entry = getEntryById(id);
        if (entry == null) {
            return 0;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_PINNED, entry.isPinned() ? 0 : 1);
        int rows = db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public int deleteEntry(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    public void clearAllEntries() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }

    /** Entries written on this calendar day in a previous year (memories). */
    public List<JournalEntry> getOnThisDayMemories(int limit) {
        List<JournalEntry> memories = new ArrayList<>();
        Calendar today = Calendar.getInstance();
        int month = today.get(Calendar.MONTH) + 1;
        int day = today.get(Calendar.DAY_OF_MONTH);

        Calendar startOfToday = Calendar.getInstance();
        startOfToday.set(Calendar.HOUR_OF_DAY, 0);
        startOfToday.set(Calendar.MINUTE, 0);
        startOfToday.set(Calendar.SECOND, 0);
        startOfToday.set(Calendar.MILLISECOND, 0);
        long todayStart = startOfToday.getTimeInMillis();

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME +
                " WHERE strftime('%m', datetime(" + COLUMN_TIMESTAMP + "/1000, 'unixepoch', 'localtime')) = ?" +
                " AND strftime('%d', datetime(" + COLUMN_TIMESTAMP + "/1000, 'unixepoch', 'localtime')) = ?" +
                " AND " + COLUMN_TIMESTAMP + " < ?" +
                " ORDER BY " + COLUMN_TIMESTAMP + " DESC LIMIT ?";
        String monthStr = String.format(Locale.US, "%02d", month);
        String dayStr = String.format(Locale.US, "%02d", day);
        Cursor cursor = db.rawQuery(query, new String[]{monthStr, dayStr, String.valueOf(todayStart), String.valueOf(limit)});

        if (cursor.moveToFirst()) {
            do {
                memories.add(createEntryFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return memories;
    }

    public int getTotalEntryCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    public int getCurrentStreak() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_TIMESTAMP}, null, null, null, null, COLUMN_TIMESTAMP + " DESC");
        Set<String> daysWithEntries = new HashSet<>();
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        if (cursor.moveToFirst()) {
            do {
                daysWithEntries.add(dayFormat.format(cursor.getLong(0)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        if (daysWithEntries.isEmpty()) {
            return 0;
        }

        Calendar cal = Calendar.getInstance();
        String today = dayFormat.format(cal.getTimeInMillis());
        if (!daysWithEntries.contains(today)) {
            cal.add(Calendar.DAY_OF_YEAR, -1);
            if (!daysWithEntries.contains(dayFormat.format(cal.getTimeInMillis()))) {
                return 0;
            }
        }

        int streak = 0;
        while (true) {
            String dayKey = dayFormat.format(cal.getTimeInMillis());
            if (daysWithEntries.contains(dayKey)) {
                streak++;
                cal.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                break;
            }
        }
        return streak;
    }

    public String getDominantMood() {
        Map<String, Integer> distribution = getMoodDistribution();
        String dominant = null;
        int max = 0;
        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                dominant = entry.getKey();
            }
        }
        return max > 0 ? dominant : null;
    }

    public Map<String, Integer> getMoodDistribution() {
        Map<String, Integer> map = new HashMap<>();
        map.put("Happy", 0);
        map.put("Calm", 0);
        map.put("Thoughtful", 0);
        map.put("Energetic", 0);
        map.put("Grateful", 0);
        map.put("Anxious", 0);

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_MOOD + ", COUNT(*) FROM " + TABLE_NAME + " GROUP BY " + COLUMN_MOOD;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                String mood = cursor.getString(0);
                int count = cursor.getInt(1);
                if (mood != null && map.containsKey(mood)) {
                    map.put(mood, count);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return map;
    }

    public String exportAllEntriesAsJson() {
        List<JournalEntry> entries = getAllEntries();
        JSONArray array = new JSONArray();
        try {
            for (JournalEntry entry : entries) {
                JSONObject obj = new JSONObject();
                obj.put("id", entry.getId());
                obj.put("title", entry.getTitle());
                obj.put("content", entry.getContent());
                obj.put("mood", entry.getMood());
                obj.put("colorHex", entry.getColorHex());
                obj.put("timestamp", entry.getTimestamp());
                obj.put("pinned", entry.isPinned());
                array.put(obj);
            }
            JSONObject root = new JSONObject();
            root.put("app", "Micro Journal");
            root.put("exportedAt", System.currentTimeMillis());
            root.put("entryCount", entries.size());
            root.put("entries", array);
            return root.toString(2);
        } catch (Exception e) {
            return "[]";
        }
    }

    private JournalEntry createEntryFromCursor(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
        String content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT));
        String mood = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOOD));
        String colorHex = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLOR_HEX));
        if (colorHex == null || colorHex.isEmpty()) {
            colorHex = "#FFFFFF";
        }
        long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));
        int pinnedCol = cursor.getColumnIndex(COLUMN_IS_PINNED);
        boolean pinned = pinnedCol >= 0 && cursor.getInt(pinnedCol) == 1;
        return new JournalEntry(id, title, content, mood, colorHex, timestamp, pinned);
    }
}
