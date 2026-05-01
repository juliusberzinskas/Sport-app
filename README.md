# Fitness Tracker — Android App

An Android fitness tracking app built with Kotlin and Firebase.
Tracks workouts in real time using GPS and device step counter sensors,
stores history in Firestore, and visualises routes on Google Maps.

Built as a group college project.

## Features

- **Live step tracking** — device step counter sensor with daily goal and progress bar
- **Workout sessions** — start a run or walk, track duration, distance, calories and speed in real time
- **GPS route mapping** — draws your workout route as a polyline on Google Maps
- **Workout history** — browse past sessions with stats and saved map screenshots
- **Statistics** — overview of activity across days
- **Calendar view** — see activity per day
- **Reminders** — scheduled notifications to stay active
- **User accounts** — register, login, profile with weight/height used for calorie calculations
- **Settings** — step goals, app preferences, account management

## Tech Stack

| Area | Technology |
|------|------------|
| Language | Kotlin |
| Platform | Android |
| Database | Firebase Firestore |
| Auth | Firebase Authentication |
| Maps | Google Maps SDK |
| Location | FusedLocationProviderClient |
| Sensors | Android TYPE_STEP_COUNTER |
| Build | Gradle (Kotlin DSL) |

## Calorie Formula
Calories = (MET × 3.5 × weight_kg) / 200 × duration_min
MET = 6.0 for running, lower for walking

## Setup

1. Clone the repo
2. Add your own `google-services.json` from Firebase Console into `app/`
3. Enable Google Maps SDK and add your API key to the project
4. Open in Android Studio and run on a device or emulator
