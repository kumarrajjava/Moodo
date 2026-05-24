package com.example;

public final class JournalUtils {

    private JournalUtils() {}

    public static int countWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }

    public static int readingTimeMinutes(int wordCount) {
        if (wordCount <= 0) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(wordCount / 200.0));
    }

    public static String moodWithEmoji(String mood) {
        if (mood == null) {
            return "";
        }
        switch (mood) {
            case "Happy": return "Happy 🌟";
            case "Calm": return "Calm 🍃";
            case "Thoughtful": return "Thoughtful 🤔";
            case "Energetic": return "Energetic ⚡";
            case "Grateful": return "Grateful 🙏";
            case "Anxious": return "Anxious 😟";
            default: return mood;
        }
    }
}
