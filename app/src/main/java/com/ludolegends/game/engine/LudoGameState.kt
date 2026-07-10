// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game.engine

/**
 * Immutable snapshot of the entire Ludo match state.
 *
 * Held inside a StateFlow by [LudoViewModel] and observed by the
 * Compose UI. Mutations are produced exclusively by [LudoEngine].
 *
 * Path / step model (per [Token]):
 *   stepIndex == -1           → pawn locked in home base
 *   stepIndex  0..51          → pawn on the main ring (relative to player.startIndex)
 *   stepIndex 52..56          → pawn on the 5-cell home column
 *   stepIndex == 57           → pawn reached the center triangle (HOME)
 *
 * Turn / dice model:
 *   • The active player rolls; if the value is 6, they get a bonus roll
 *     (subject to the three-consecutive-sixes rule).
 *   • A pawn may only leave base on an exact roll of 6.
 *   • Capturing an opponent's pawn (on a non-safe cell) grants a bonus roll.
 *   • Three consecutive 6s invalidate the turn entirely.
 *   • A pawn must land exactly on stepIndex 57 to finish; over-rolls are illegal.
 *
 * Animation flow:
 *   • When a move is prepared (dice rolled + token picked/auto-picked), the
 *     engine stashes a [PendingMove] in [pendingMove] and enters
 *     [TurnPhase.PENDING_ANIMATION]. The ViewModel drives the hop animation
 *     using [PendingMove] info, then calls `engine.commitPendingMove()`
 *     to apply the final state transition.
 */
data class LudoGameState(
    val turnOrder: List<Player> = Player.DefaultOrder,
    val currentPlayerIndex: Int = 0,
    val tokens: Map<Player, List<Token>> = emptyMap(),
    val lastDiceValue: Int = 0,
    val consecutiveSixes: Int = 0,
    val phase: TurnPhase = TurnPhase.AWAITING_DICE,
    val selectableTokenIds: Set<Pair<Player, Int>> = emptySet(),
    val notification: GameNotification? = null,
    val winner: Player? = null,
    val turnHistory: List<TurnRecord> = emptyList(),
    val historyPointer: Int = -1,        // for undo/redo support
    val gameMode: GameMode = GameMode.STANDARD,
    val gameModeType: GameModeType = GameModeType.LOCAL_PASS_PLAY,
    val isAnimating: Boolean = false,
    val pendingMove: PendingMove? = null,
    val lastRollIsBot: Boolean = false   // always false in both modes — no bots
) {

    /** The player whose turn it currently is. */
    val currentPlayer: Player get() = turnOrder[currentPlayerIndex.coerceIn(0, turnOrder.lastIndex)]

    /** All tokens flattened — useful for board rendering. */
    val allTokens: List<Token>
        get() = turnOrder.flatMap { tokens[it].orEmpty() }

    /** Number of pawns the given player has already brought home. */
    fun homeCount(player: Player): Int =
        tokens[player].orEmpty().count { it.isHome }

    /** True when every pawn of [player] has reached HOME. */
    fun hasWon(player: Player): Boolean =
        tokens[player].orEmpty().isNotEmpty() && tokens[player]!!.all { it.isHome }
}

/** What the engine expects the user (or AI) to do next. */
enum class TurnPhase {
    AWAITING_DICE,         // waiting for player to tap a dice button
    AWAITING_TOKEN_PICK,   // dice rolled, player must choose a token to move
    PENDING_ANIMATION,     // move chosen, waiting for hop animation to finish
    ANIMATING_HOP,         // token is hopping — input frozen (alias kept for back-compat)
    TURN_TRANSITION,       // moving to next player
    GAME_OVER
}

/** Modes selectable from the New Game Setup sheet. */
enum class GameMode(val display: String) {
    STANDARD("Standard Ludo"),
    TEAM_2V2("2v2 Team Mode")
}

/** One-shot UI notifications emitted by the engine. */
data class GameNotification(
    val type: NotificationType,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class NotificationType {
        UNLOCK,             // pawn released from base on a 6
        BONUS_ROLL,         // extra dice granted
        THREE_SIXES_BURN,   // three 6s — turn forfeited
        CAPTURE,            // opponent pawn sent back to base
        BLOCKED_MOVE,       // dice value too high to land exactly on home
        HOME_REACHED,       // pawn reached center
        WINNER,             // player won
        INFO
    }
}

/** Record of one completed turn, for undo/redo. */
data class TurnRecord(
    val player: Player,
    val diceValue: Int,
    val tokenId: Int,
    val fromStep: Int,
    val toStep: Int,
    val captured: List<Pair<Player, Int>>,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Pre-computed move awaiting the hop animation to finish before being
 * committed to the canonical [LudoGameState].
 *
 * The ViewModel reads [fromStep], [toStep], [player], [tokenId] to drive
 * the cell-by-cell hop animation. When the animation completes, it calls
 * `engine.commitPendingMove()` to finalize the state transition.
 */
data class PendingMove(
    val player: Player,
    val tokenId: Int,
    val diceValue: Int,
    val fromStep: Int,
    val toStep: Int,
    val captured: List<Pair<Player, Int>>,
    val isUnlock: Boolean,
    val reachedHome: Boolean,
    val isWinningMove: Boolean,
    val grantBonus: Boolean
)
