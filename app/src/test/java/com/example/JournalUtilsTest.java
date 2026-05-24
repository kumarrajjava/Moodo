package com.example;

import org.junit.Test;
import static org.junit.Assert.*;

public class JournalUtilsTest {

    @Test
    public void countWords_empty_returnsZero() {
        assertEquals(0, JournalUtils.countWords(""));
        assertEquals(0, JournalUtils.countWords("   "));
    }

    @Test
    public void countWords_multipleWords() {
        assertEquals(3, JournalUtils.countWords("hello brave world"));
    }

    @Test
    public void readingTime_zeroWords_returnsZero() {
        assertEquals(0, JournalUtils.readingTimeMinutes(0));
    }

    @Test
    public void readingTime_shortText_returnsOneMinute() {
        assertEquals(1, JournalUtils.readingTimeMinutes(50));
    }

    @Test
    public void moodWithEmoji_knownMood() {
        assertTrue(JournalUtils.moodWithEmoji("Happy").contains("Happy"));
    }
}
