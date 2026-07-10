// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game.engine

/**
 * The two distinct core multiplayer engines for Ludo Legends.
 *
 * === MODE 1 — LOCAL_PASS_PLAY (100% Ludo King clone) ===
 * No manual dice injector panel. An animated digital 3D rolling dice
 * drives the roll via secure mathematical randomness
 * (`Random.nextInt(1, 7)` — uniform distribution, zero bias).
 * All four player slots (Red, Green, Yellow, Blue) are humans
 * (`isBot = false`); the device is physically passed between teams.
 *
 * === MODE 2 — PLAY_WITH_FRIENDS (Hybrid Physical Dice Engine) ===
 * Retains the Manual Dice Input Injector grid (1–6 buttons) at the
 * bottom. Players roll a real wooden dice in physical space, then tap
 * the matching number on screen. NO bots — all four slots are human.
 *
 * Both modes share the same canonical [LudoGameState], [LudoEngine],
 * and [LudoRules]. The UI layer picks the appropriate screen based
 * on this enum.
 */
enum class GameModeType(val display: String, val subtitle: String) {
    LOCAL_PASS_PLAY(
        display = "Local Pass & Play",
        subtitle = "Animated digital 3D dice. Pass the device between turns."
    ),
    PLAY_WITH_FRIENDS(
        display = "Play With Friends",
        subtitle = "Roll a real wooden dice, tap the matching number."
    )
}
