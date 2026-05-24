package com.example;

public class JournalEntry {
    private long id;
    private String title;
    private String content;
    private String mood;
    private String colorHex;
    private long timestamp;

    public JournalEntry(long id, String title, String content, String mood, String colorHex, long timestamp) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.mood = mood;
        this.colorHex = colorHex;
        this.timestamp = timestamp;
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
}
