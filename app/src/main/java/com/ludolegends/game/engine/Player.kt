// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game.engine

import androidx.compose.ui.graphics.Color
import com.ludolegends.game.ui.theme.PlayerBlue
import com.ludolegends.game.ui.theme.PlayerBlueDark
import com.ludolegends.game.ui.theme.PlayerBlueLight
import com.ludolegends.game.ui.theme.PlayerGreen
import com.ludolegends.game.ui.theme.PlayerGreenDark
import com.ludolegends.game.ui.theme.PlayerGreenLight
import com.ludolegends.game.ui.theme.PlayerRed
import com.ludolegends.game.ui.theme.PlayerRedDark
import com.ludolegends.game.ui.theme.PlayerRedLight
import com.ludolegends.game.ui.theme.PlayerYellow
import com.ludolegends.game.ui.theme.PlayerYellowDark
import com.ludolegends.game.ui.theme.PlayerYellowLight

/**
 * The four Ludo player colors. Their ordinal matters because
 * [LudoEngine] uses it to derive turn order, board entry index,
 * and home-column entry via [Player.startIndex].
 *
 * Layout on the 15×15 board (verified against [BoardMap.RING_COORDS]):
 *   RED    exits at absoluteRing  0  (cell 6,1) — top-left quadrant
 *   GREEN  exits at absoluteRing 13  (cell 1,8) — top-right quadrant
 *   YELLOW exits at absoluteRing 36  (cell 14,8) — bottom-right quadrant
 *   BLUE   exits at absoluteRing 39  (cell 13,6) — bottom-left quadrant
 *
 * This produces the standard Ludo King clockwise turn order
 * Red → Green → Yellow → Blue, with each player's home-column
 * entrance one cell before their own exit tile.
 */
enum class Player(
    val display: String,
    val primary: Color,
    val light: Color,
    val dark: Color,
    val startIndex: Int         // absolute ring index (0..51) where this player's pawn enters
) {
    RED(
        display = "Red",
        primary = PlayerRed, light = PlayerRedLight, dark = PlayerRedDark,
        startIndex = 0
    ),
    GREEN(
        display = "Green",
        primary = PlayerGreen, light = PlayerGreenLight, dark = PlayerGreenDark,
        startIndex = 13
    ),
    YELLOW(
        display = "Yellow",
        primary = PlayerYellow, light = PlayerYellowLight, dark = PlayerYellowDark,
        startIndex = 36
    ),
    BLUE(
        display = "Blue",
        primary = PlayerBlue, light = PlayerBlueLight, dark = PlayerBlueDark,
        startIndex = 39
    );

    /**
     * The absolute ring index of the cell just before this player's
     * own exit tile — i.e. the cell from which the pawn turns into
     * its home column on the next step.
     */
    val homeEntryRingIndex: Int get() = (startIndex + 51) % 52

    companion object {
        /** Default 4-player turn order matches Ludo King: Red → Green → Yellow → Blue. */
        val DefaultOrder: List<Player> = listOf(RED, GREEN, YELLOW, BLUE)

        /** Two-player setup uses diagonally opposite players. */
        val TwoPlayerOrder: List<Player> = listOf(RED, YELLOW)

        /** Three-player setup. */
        val ThreePlayerOrder: List<Player> = listOf(RED, GREEN, YELLOW)

        /** Returns the active player order for the given player count. */
        fun orderFor(playerCount: Int): List<Player> = when (playerCount) {
            2 -> TwoPlayerOrder
            3 -> ThreePlayerOrder
            else -> DefaultOrder
        }

        /**
         * === SECTION 4 — FULL 2V2 TEAM MODE RIGIDIFICATION ===
         *
         * Team pairing: Red+Yellow are teammates; Green+Blue are teammates.
         * Used to:
         *   • Block captures between teammates (safety & stacking).
         *   • Redirect turns when one teammate has finished all 4 pawns.
         */
        fun teamOf(player: Player): Team = when (player) {
            RED, YELLOW -> Team.RED_YELLOW
            GREEN, BLUE -> Team.GREEN_BLUE
        }

        fun isTeammate(a: Player, b: Player): Boolean =
            a != b && teamOf(a) == teamOf(b)
    }
}

/**
 * 2v2 alliance bonds. In Team Mode, captures between teammates are
 * forbidden and the pieces stack via the safe-zone mini-grid layout.
 */
enum class Team { RED_YELLOW, GREEN_BLUE }
