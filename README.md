# Ludo Legends — Premium Jetpack Compose Edition

A complete, production-grade Ludo King-style game built with **Jetpack Compose**, **Kotlin 2.0**, and the **Material 3** design system. Dark sapphire navy gradient base, razor-sharp double-layered gold trim borders, solid glossy 3D pawn rendering, parabolic hop animation, fully authentic Ludo King rule engine, two distinct game modes, zero-latency SoundPool SFX + ExoPlayer BGM, DataStore-backed persistent coin economy, full 2v2 team mode, the "Game Juice" micro-animation suite, **Room SQLite match-state persistence with auto-save + resume history**, **accidental-exit prevention dialog**, **canvas-layer micro-interactions** (dotted spinning selection ring, active-turn pulse glow, embossed home bases), **regenerated audio (strike+whistle kill, royal brass victory fanfare)**, **free offline bypass for Local/Friends modes**, and the **real-time dynamic score matrix**.

> **v4.0 — Architectural & Behavioral Masterpiece**: Room SQLite persistence with async auto-save after every move + match-history recovery list; accidental-exit prevention AlertDialog; canvas-layer micro-interactions (dotted spinning selection ring via `PathEffect.dashPathEffect`, active-turn breathing glow 0.4f↔1.0f, embossed sunken home-base parking slots); regenerated SFX (kill = heavy strike + descending whistle, victory = royal brass orchestration fanfare); free-mode bypass for Local Pass & Play and Play With Friends (no coin deduction); real-time score matrix = blocks + captures×50 + home×100 with animated ticker transitions. JUnit regression tests included.

---

## 1. The Two Distinct Core Multiplayer Engines

### MODE 1 — STANDARD "LOCAL PASS & PLAY" (100% Ludo King Clone)
- **NO manual 1-6 dice injector panel.** An animated digital 3D rolling dice ([`AnimatedDice3D`](app/src/main/java/com/ludolegends/game/ui/components/AnimatedDice3D.kt)) triggers a crisp multi-frame tumbling animation on tap.
- **Fair Random Math Engine**: rolls driven by `Random.nextInt(1, 7)` with uniform probability distribution — zero bias, zero rigging.
- **Absolute Human Pass-and-Play Control**: all four slots are human (`isBot = false`). When a turn ends, the UI displays "Pass the device to {next player}".
- **Screen**: [`LocalPlayScreen`](app/src/main/java/com/ludolegends/game/ui/screens/LocalPlayScreen.kt)

### MODE 2 — "PLAY WITH FRIENDS" (Hybrid Physical Dice Engine)
- **Retains the Manual Dice Input Injector grid panel** (1-6 buttons) at the bottom.
- **NO bots** — all four slots are real human interactions. Players roll a physical wooden dice in real life, then tap the corresponding number on screen.
- The engine accepts the input, highlights legal movable pawns, and awaits human tap selection.
- **Screen**: [`PlayWithFriendsScreen`](app/src/main/java/com/ludolegends/game/ui/screens/PlayWithFriendsScreen.kt)

Both modes share the same canonical [`LudoGameState`](app/src/main/java/com/ludolegends/game/engine/LudoGameState.kt), [`LudoEngine`](app/src/main/java/com/ludolegends/game/engine/LudoEngine.kt), and [`LudoRules`](app/src/main/java/com/ludolegends/game/engine/LudoRules.kt) — the engine is perfectly mode-agnostic. Mode selection happens at the ViewModel layer via [`GameModeType`](app/src/main/java/com/ludolegends/game/engine/GameModeType.kt) and the dice source is swapped via `LudoEngine.setDiceRoller(...)`.

---

## 2. Project Architecture Map

```
LudoLegends/
├── settings.gradle.kts                 # Module declarations
├── build.gradle.kts                    # Root build (AGP 8.5.2 + Kotlin 2.0.20)
├── gradle.properties                   # JVM args + AndroidX flags
├── gradle/wrapper/
│   └── gradle-wrapper.properties       # Gradle 8.9
└── app/
    ├── build.gradle.kts                # App module: Compose, Lifecycle, Navigation, Coroutines
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml         # Single activity, portrait, splash theme
        ├── res/
        │   ├── values/colors.xml       # Brand + player palettes
        │   ├── values/strings.xml      # All UI copy
        │   ├── values/themes.xml       # Splash + base themes
        │   ├── values-night/themes.xml
        │   ├── raw/                    # === SYNTHESIZED AUDIO CLIPS ===
        │   │   ├── dice_roll.wav       # Multi-frame dice rattle (0.45s)
        │   │   ├── token_hop.wav       # Soft pluck at landing apex (0.12s)
        │   │   ├── token_kill.wav      # Hard impact + descending sweep (0.35s)
        │   │   └── victory.wav         # Ascending C-major arpeggio fanfare (1.2s)
        │   ├── drawable/ic_splash_logo.xml
        │   ├── drawable/ic_launcher_foreground.xml
        │   └── mipmap-anydpi-v26/ic_launcher.xml
        │       ic_launcher_round.xml
        └── java/com/ludolegends/game/
            ├── LudoApplication.kt       # Custom Application
            ├── MainActivity.kt          # Single-activity host (splash + mode routing + audio provider)
            ├── audio/                   # === SOUNDPOOL AUDIO LAYER ===
            │   └── LudoAudioManager.kt  # Zero-latency multi-threaded SoundPool wrapper
            ├── engine/                  # === RULE ENGINE (pure Kotlin, mode-agnostic) ===
            │   ├── Player.kt            # 4 player enum + startIndex / homeEntryRingIndex
            │   ├── Token.kt             # Pawn model (BASE / 0..51 / 52..56 / HOME)
            │   ├── DiceRoller.kt        # FairRandomDiceRoller (MODE 1) + ManualDiceRoller (MODE 2)
            │   ├── GameModeType.kt      # LOCAL_PASS_PLAY vs PLAY_WITH_FRIENDS enum
            │   ├── BoardMap.kt          # 15x15 grid + 52-cell ring + home columns + base slots
            │   ├── SafeCells.kt         # 8 safe cells (4 exit tiles + 4 star cells)
            │   ├── LudoRules.kt         # RULES 1-4: unlock-6 / three-sixes / safezone / exact-home
            │   ├── LudoGameState.kt     # Immutable state + PendingMove + TurnRecord + gameModeType
            │   └── LudoEngine.kt        # UDF MutableStateFlow state machine: prepare → commit, undo/redo
            ├── viewmodel/
            │   └── LudoViewModel.kt     # MVI bridge + mode-aware dice input + hop animation + audio sync
            └── ui/
                ├── theme/
                │   ├── Color.kt         # Sapphire + gold + player palettes
                │   ├── Type.kt          # Bold sans-serif typography
                │   ├── Gradients.kt     # All premium gradient brushes
                │   └── Theme.kt         # MaterialTheme wrapper
                ├── components/
                │   ├── GoldBorder.kt        # Double-layered gold trim border
                │   ├── SafeStar.kt          # 5-pointed star marker for safe cells
                │   ├── Token3D.kt           # Premium 3D pawn (standalone composable)
                │   ├── LudoBoard.kt         # 15x15 Canvas board + token rendering + hop overlay
                │   ├── TokenTouchOverlay.kt # Hit-testing overlay for token taps
                │   ├── AnimatedDice3D.kt    # MODE 1: animated digital 3D tumbling dice
                │   ├── DicePanel.kt         # MODE 2: 6 manual dice buttons (pips + numbers)
                │   ├── FooterNav.kt         # [Menu][Undo][Redo][Settings] bar
                │   ├── PlayerStatusBar.kt   # Top chip strip showing all players
                │   └── NotificationBanner.kt# Engine message toast
                └── screens/
                    ├── LobbyScreen.kt       # Main menu (3 cards + token preview)
                    ├── SetupSheet.kt        # Dismissable bottom-sheet: mode type + game mode + player count
                    ├── LocalPlayScreen.kt   # MODE 1: animated 3D dice + pass-the-device indicator
                    ├── PlayWithFriendsScreen.kt # MODE 2: manual dice injector panel
                    └── SettingsSheet.kt     # In-game settings (restart, return to menu)
```

---

## 3. Build & Run Instructions

### Requirements
- **Android Studio Koala (2024.1.1)** or newer
- **JDK 17** (bundled with Android Studio)
- **Android SDK** with `compileSdk 34` and `minSdk 24`
- Internet connection (Gradle will download Compose BOM 2024.09.02 and dependencies)

### Steps

1. **Open the project**
   - Launch Android Studio → `File` → `Open` → select the `LudoLegends/` folder.
   - Wait for Gradle sync to complete.

2. **Build a debug APK**
   - From the menu: `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`.
   - OR from terminal:
     ```bash
     cd LudoLegends
     ./gradlew assembleDebug
     ```
   - The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

3. **Run on device/emulator**
   - Connect a device (USB debugging on) or start an emulator.
   - Press `Shift+F10` (or click the green play button).

4. **Build a release APK**
   ```bash
   ./gradlew assembleRelease
   ```
   - Output: `app/build/outputs/apk/release/app-release-unsigned.apk`.
   - Sign with your keystore before publishing.

---

## 3. Rule Engine Implementation

### RULE 1 — Unlock Six Rule
- A pawn at `stepIndex = Token.BASE (-1)` is locked.
- It can only be deployed onto the ring when the dice value is **exactly 6**.
- See `LudoRules.canUnlock(token, diceValue)` and `LudoRules.computeDestination()`.

### RULE 2 — Three Consecutive Sixes Invalidation
- The engine tracks `consecutiveSixes` per turn.
- On a 6, the player gets a bonus roll.
- On the **3rd consecutive 6**, the roll is burned, the turn forfeits, control passes to the next player.
- See `LudoRules.shouldBurnTurn(consecutiveSixes, currentDiceValue)`.

### RULE 3 — Safezone Star Extraction & Elimination
- 8 safe cells defined in `SafeCells.SAFE_RING_INDICES`:
  - 4 colored exit tiles (Red=0, Green=13, Yellow=36, Blue=39)
  - 4 star cells (8, 21, 44, 47)
- Landing on a non-safe cell with opponents present → opponents sent back to BASE, bonus roll granted.
- Landing on a safe cell → tokens stack neatly in a 2×2 mini-grid, no captures.
- See `LudoRules.computeCapture(state, movingPlayer, tokenId, destinationStep)`.

### RULE 4 — Exact Roll Home Entry
- A token must land **exactly** on `stepIndex = 57` (the center triangle) to finish.
- If the dice value overshoots 57, the move is **illegal** — the token stays frozen, no move.
- See `LudoRules.canMoveToHome(token, diceValue)` and `LudoRules.computeDestination()`.

### Bonus Roll Triggers
A bonus roll is granted when ANY of:
- Dice value is 6 (subject to the 3-sixes rule)
- A capture occurred on this move
- A pawn reached home on this move

See `LudoRules.shouldGrantBonusRoll(...)`.

---

## 4. Premium Rendering Features

### Board Container (SECTION 1.1)
- `LudoBoard` uses `Modifier.fillMaxWidth().aspectRatio(1f)` so the 15×15 grid occupies the **maximum available screen width**.
- No dead space — gold trim sits directly on the screen edge with a thin 4dp safety margin.

### Luxury Sapphire + Gold Trim (SECTION 1.2)
- Dark sapphire navy radial gradient base on every screen (`Theme.kt`).
- `GoldTrimBorder` composable draws a razor-sharp **double-layered gold outline** (outer gold gradient + inner darker trim).
- Used on every primary layout card, selection tile, and board profile frame.

### 3D Pawn Rendering (SECTION 1.2)
- `Token3D` composable + `DrawScope.drawTokenAt()` render pawns with:
  - Soft blurred drop shadow (radial alpha gradient)
  - Base disc with radial gradient (light → dark)
  - Gold neck ring
  - Dome with glossy highlight ellipse
  - Gold top jewel
- Replaces flat circles entirely.

### HUD Control Panel (SECTION 1.3)
- `DicePanel` renders 6 dice buttons as **glowing dark sapphire rectangles** with bright white borders, crisp pip arrangements at the top, and a bold numeric label at the bottom.
- `FooterNav` renders `[Menu] [Undo] [Redo] [Settings]` as compact dark tiles with gold icons.

### Hopping Animation (SECTION 3.1)
- The engine uses a **prepare / commit** pattern:
  - `LudoEngine.prepareMove()` stashes a `PendingMove` in state without mutating tokens.
  - `LudoViewModel.animatePendingMove()` runs a cell-by-cell hop coroutine.
  - For a roll of X, the token hops X times; each hop uses a parabolic arc eased via `FastOutSlowInEasing`.
  - On completion, `LudoEngine.commitPendingMove()` finalizes the state transition.

### Dismissable Setup Sheet (SECTION 3.2)
- Tapping "Local Pass & Play" in `LobbyScreen` slides up `SetupSheet` from the bottom via `AnimatedVisibility` + `slideInVertically`.
- Tapping the glowing green "START MATCH" button dismisses the sheet and calls `LudoViewModel.startMatchFromSetup()` which initializes the game loop and navigates to `GameScreen`.

---

## 5. Tech Stack

| Layer            | Library / Version                              |
|------------------|------------------------------------------------|
| UI               | Jetpack Compose (BOM 2024.09.02), Material 3   |
| Language         | Kotlin 2.0.20 (Compose plugin enabled)         |
| Build            | AGP 8.5.2, Gradle 8.9                          |
| Min SDK          | 24 (Android 7.0)                               |
| Target SDK       | 34 (Android 14)                                |
| Lifecycle        | `androidx.lifecycle:lifecycle-*:2.8.6`         |
| Navigation       | `androidx.navigation:navigation-compose:2.8.1` |
| Coroutines       | `kotlinx-coroutines-android:1.8.1`             |
| Splashscreen     | `androidx.core:core-splashscreen:1.0.1`        |

---

## 6. Extending the Project

### Add Online Multiplayer
Replace `ManualDiceRoller` with a network-backed `DiceRoller` that fetches rolls from your server. The engine doesn't care about the source — only the value.

### Add AI Opponents
Implement a `BotStrategy` that picks a token automatically when the engine enters `AWAITING_TOKEN_PICK`. Hook it into `LudoViewModel` via a coroutine that observes state changes.

### Add Sound Effects
Drop audio files into `res/raw/` and trigger playback from `LudoViewModel`'s animation method (e.g. play a "hop" sound for each step, a "capture" sound when `pending.captured.isNotEmpty()`).

### Add Themes
Add new `Color.kt` palettes and expose a theme switcher in `Settings`. The `MaterialTheme` wrapper in `Theme.kt` already supports runtime color scheme swaps.

---

## 7. License

Provided as-is for the Ludo Legends project. Replace with your preferred license before publishing.
