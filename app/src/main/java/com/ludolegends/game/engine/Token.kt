// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game.engine

/**
 * A single pawn on the board.
 *
 * Position model:
 *  - [stepIndex] == [Token.BASE]      → pawn is locked inside the home-base quadrant, not on the track.
 *  - [stepIndex] in 0..51             → pawn is on the main ring.
 *  - [stepIndex] in 52..56            → pawn is on this player's home column (5 cells).
 *  - [stepIndex] == [Token.HOME]      → pawn has reached the center triangle (winner for this pawn).
 *
 * The absolute position on the ring is computed relative to the owning player's [Player.startIndex].
 * That is, ring position = (player.startIndex + stepIndex) mod 52, for stepIndex in 0..51.
 *
 * For the home column, [stepIndex] 52..56 maps directly to the 5 cells leading into the center.
 */
data class Token(
    val id: Int,                  // 0..3 — identifies which pawn of the player
    val ownerId: Player,
    var stepIndex: Int = BASE
) {
    /** True when this pawn is still locked in the home-base quadrant. */
    val isAtBase: Boolean get() = stepIndex == BASE

    /** True when this pawn has successfully entered the center triangle. */
    val isHome: Boolean get() = stepIndex == HOME

    /** True when this pawn is on the home column (final stretch). */
    val isInHomeColumn: Boolean get() = stepIndex in 52..56

    /** True when this pawn is on the main 52-cell ring. */
    val isOnRing: Boolean get() = stepIndex in 0..51

    /**
     * Absolute ring index (0..51) used for capture / safe-cell checks.
     * Returns -1 when the pawn is not on the ring.
     */
    fun absoluteRingIndex(): Int {
        if (!isOnRing) return -1
        return (ownerId.startIndex + stepIndex) % 52
    }

    companion object {
        const val BASE = -1
        const val HOME = 57
        const val HOME_COLUMN_ENTRY = 51
        const val HOME_COLUMN_END = 56
    }
}
