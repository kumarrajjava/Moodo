# Micro Journal

A beautiful, privacy-first micro-journaling app for Android. Capture daily thoughts, track moods, build streaks, and reflect on memories — all stored locally on your device.

## Features

- **Quick journaling** — Title, rich content, mood tags, and pastel canvas colors
- **Mood insights** — Weekly mood distribution dashboard
- **Writing streaks** — Stay motivated with daily streak tracking
- **Daily prompts** — Rotating inspiration to start writing
- **On This Day** — Rediscover entries from this date in past years
- **Pin favorites** — Keep important entries at the top
- **Search & filter** — Find entries by text or mood
- **Swipe to delete** — With undo support
- **Share entries** — Send reflections to friends or notes apps
- **JSON export** — Full backup you control
- **Dark mode** — Easy on the eyes at night
- **Onboarding** — Polished first-run experience
- **Privacy-first** — No accounts, no cloud, no tracking

## Play Store Listing (draft)

**Short description:**  
Capture daily thoughts & moods in seconds. Private, offline micro-journal.

**Full description:**  
Micro Journal is your personal space for quick, honest reflection. Write micro-entries in seconds, tag how you feel, and watch your emotional patterns emerge over time.

✨ Write with beautiful pastel canvases  
📊 Mood insights & writing streaks  
📅 "On This Day" memories  
🔒 100% private — data stays on your device  
🌙 Dark mode included  
📤 Export & share when you want  

No sign-up. No ads. No internet required.

## Build & Run

```bash
# Requires Android SDK + JDK 17
./gradlew installDebug
```

## Release Checklist

- [ ] Create upload keystore (`my-upload-key.jks`) and set env vars: `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`
- [ ] Add Play Store screenshots (phone + tablet)
- [ ] Host privacy policy URL (text included in-app under Settings)
- [ ] Fill Play Console content rating questionnaire
- [ ] Run `./gradlew bundleRelease` for AAB upload

## Tech Stack

- Java + XML layouts
- SQLite local database
- Material Design 3
- Min SDK 24, Target SDK 36

## License

All rights reserved.
