package com.example;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JournalDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "micro_journal.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "journal_entries";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_MOOD = "mood";
    public static final String COLUMN_COLOR_HEX = "color_hex";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TITLE + " TEXT, " +
            COLUMN_CONTENT + " TEXT, " +
            COLUMN_MOOD + " TEXT, " +
            COLUMN_COLOR_HEX + " TEXT, " +
            COLUMN_TIMESTAMP + " INTEGER" +
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Create
    public long insertEntry(String title, String content, String mood, String colorHex, long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_CONTENT, content);
        values.put(COLUMN_MOOD, mood);
        values.put(COLUMN_COLOR_HEX, colorHex);
        values.put(COLUMN_TIMESTAMP, timestamp);
        long id = db.insert(TABLE_NAME, null, values);
        db.close();
        return id;
    }

    // Read All (ordered by newest first)
    public List<JournalEntry> getAllEntries() {
        List<JournalEntry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, COLUMN_TIMESTAMP + " DESC");

        if (cursor.moveToFirst()) {
            do {
                entries.add(createEntryFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return entries;
    }

    // Filter by mood
    public List<JournalEntry> getEntriesByMood(String mood) {
        List<JournalEntry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                COLUMN_MOOD + " = ?",
                new String[]{mood},
                null,
                null,
                COLUMN_TIMESTAMP + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                entries.add(createEntryFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return entries;
    }

    // Text Search in Title or Content
    public List<JournalEntry> searchEntries(String queryText) {
        List<JournalEntry> entries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_TITLE + " LIKE ? OR " + COLUMN_CONTENT + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + queryText + "%", "%" + queryText + "%"};

        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                COLUMN_TIMESTAMP + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                entries.add(createEntryFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return entries;
    }

    // Read Single
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

    // Update
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

    // Delete
    public int deleteEntry(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    // Aggregates for Insights view - Mood Frequency breakdown
    public Map<String, Integer> getMoodDistribution() {
        Map<String, Integer> map = new HashMap<>();
        // initialize map with expected moods
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

    private JournalEntry createEntryFromCursor(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
        String content = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT));
        String mood = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOOD));
        String colorHex = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COLOR_HEX));
        long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));
        return new JournalEntry(id, title, content, mood, colorHex, timestamp);
    }
}
