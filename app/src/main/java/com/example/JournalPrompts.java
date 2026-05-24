package com.example;

import java.util.Calendar;

public final class JournalPrompts {

    private static final String[] PROMPTS = {
            "What made you smile today, even briefly?",
            "Name three things you're grateful for right now.",
            "What challenged you today, and how did you respond?",
            "Describe a moment today when you felt truly calm.",
            "What would you tell your past self from one year ago?",
            "Who positively impacted your day, and how?",
            "What's one small win you had today?",
            "What emotion showed up most today — and why?",
            "If today had a headline, what would it be?",
            "What are you looking forward to tomorrow?",
            "What did you learn about yourself today?",
            "Write about a place that makes you feel safe.",
            "What's draining your energy lately? Be honest.",
            "What habit do you want to build this week?",
            "Describe your ideal morning in detail.",
            "What fear did you face recently, even in a small way?",
            "List five things that went well this week.",
            "What would make next week feel successful?",
            "Who do you need to forgive — including yourself?",
            "What does rest actually look like for you?"
    };

    private JournalPrompts() {}

    public static String getDailyPrompt() {
        int dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        return PROMPTS[dayOfYear % PROMPTS.length];
    }
}
