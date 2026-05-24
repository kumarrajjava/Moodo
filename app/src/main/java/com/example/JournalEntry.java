package com.example;

public class JournalEntry {
    private long id;
    private String title;
    private String content;
    private String mood;
    private String colorHex;
    private long timestamp;
    private boolean pinned;

    public JournalEntry(long id, String title, String content, String mood, String colorHex, long timestamp) {
        this(id, title, content, mood, colorHex, timestamp, false);
    }

    public JournalEntry(long id, String title, String content, String mood, String colorHex, long timestamp, boolean pinned) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.mood = mood;
        this.colorHex = colorHex;
        this.timestamp = timestamp;
        this.pinned = pinned;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getMood() {
        return mood;
    }

    public String getColorHex() {
        return colorHex;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public int getWordCount() {
        return JournalUtils.countWords(content);
    }

    public int getReadingTimeMinutes() {
        return JournalUtils.readingTimeMinutes(getWordCount());
    }
}
