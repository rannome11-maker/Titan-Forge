# Titan Forge Android MVP

An offline-first gamified fitness prototype where consistency evolves a warrior avatar.

## Included

- Black, gold and white visual system
- 10 male and 10 female hero archetypes
- 8 evolution tiers per hero (160 procedural forms)
- Age, weight, goal, target-area and experience onboarding
- Rule-based personalized daily workout
- Daily checklist, XP, power, streaks and weekly progress
- Weekly evolution when the selected training-day target is met
- Daily local notifications at 8 AM, noon or 8 PM
- Persistent offline progress with SharedPreferences
- Main Station with avatar statistics and equipment-aware avatar card
- 50-coin daily completion reward
- Store, equipment slots, inventory, event vault and rep-based quests
- World Chat interface prepared for a secure real-time backend
- Lightweight procedural OpenGL 3D avatars with touch rotation and idle animation
- Distinct character palettes, eight increasingly armored evolution models, and 3D equipment previews

## Build

Open the `TitanForge` folder in Android Studio (JDK 17), allow Gradle sync, then choose **Build > Build APK(s)**. The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

### Automatic GitHub build

Every push to `main` runs **Build Android APK**. Open the repository's **Actions** tab, select the completed run, and download the `Titan-Forge-debug-apk` artifact. Unzip it on Android and open `app-debug.apk` to install.

## Prototype boundaries

This MVP uses rule-based recommendations and procedural character cards. A production release should add illustrated character assets, exercise demonstrations, accessibility QA, encrypted/cloud backup, account sync, medical screening, adaptive progression, analytics consent, notification rescheduling after reboot, tests and a signed release configuration.

Workout suggestions are general fitness content, not medical advice. Users with injuries, symptoms, pregnancy, or medical conditions should consult a qualified professional.
