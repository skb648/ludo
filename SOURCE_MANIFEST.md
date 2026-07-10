# Ludo Legends v5.0 — Complete Source Manifest

Synchronized on **10 July 2026**.

## Included

- Complete Kotlin/Jetpack Compose Android source
- Local Pass & Play animated dice mode
- Play With Friends manual dice mode
- Standard and 2v2 team rule engines
- Safe cells, captures, triple-six penalty, exact home entry, bonus turns
- Team pawn redirection after a teammate finishes
- Premium canvas board, glossy tokens, hop animation, capture particles, confetti
- SoundPool effects, ExoPlayer ambient music, haptics
- DataStore wallet/economy and Room match persistence
- Unit and regression tests
- Gradle wrapper and GitHub Actions APK build workflow
- All launcher, theme, XML, and WAV resources

## Build

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Debug APK output: `app/build/outputs/apk/debug/app-debug.apk`.
