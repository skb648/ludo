# Ludo Legends v6 — Current Complete Source

This is the current Jetpack Compose/Kotlin game source corresponding to the newly rebuilt Ludo Legends app — not the legacy repository implementation.

## Current architecture
- `engine/LudoModels.kt`, `LudoRules.kt`, `BoardGeometry.kt` — strict unit-tested rules
- `vm/GameViewModel.kt` — MVI StateFlow, animated moves, undo/redo, economy
- `ui/board` — 15×15 premium canvas board and game-juice effects
- `ui/screens` — splash, lobby, setup, roller/manual game modes
- `audio` — SoundPool SFX, ExoPlayer BGM, haptics
- `data` — persistent DataStore wallet

## Modes
1. **Local Pass & Play** — animated fair digital dice, all-human seats
2. **Play With Friends** — manual physical-dice 1–6 input
3. **Classic** and **2v2 Team** rules, including teammate pawn redirection

## Build
```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```
GitHub Actions builds and uploads `Ludo-Legends-v6-latest-debug` from this exact source tree.
