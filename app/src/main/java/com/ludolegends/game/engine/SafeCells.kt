// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game.engine

/**
 * Safe cells on the Ludo ring.
 *
 * In Ludo King, exactly 8 cells on the 52-cell main ring are "safe":
 *   • The 4 colored exit tiles (where pawns deploy from base) — these are also the
 *     4 colored squares with directional arrows on the reference board.
 *   • The 4 star-marked cells, located 8 steps after each colored exit tile.
 *
 * Absolute ring indices (0..51) of safe cells, verified against [BoardMap.RING_COORDS]:
 *   Exit tiles:   0 (Red),    13 (Green),  36 (Yellow), 39 (Blue)
 *   Star cells:   8,          21,          44,          47
 *
 * On safe cells, opponents stack without capturing each other.
 */
object SafeCells {

    /** Absolute ring indices (0..51) of all 8 safe cells. */
    val SAFE_RING_INDICES: Set<Int> = setOf(0, 8, 13, 21, 36, 39, 44, 47)

    /** The four colored exit tiles (start positions of each player). */
    val EXIT_TILES: Set<Int> = setOf(0, 13, 36, 39)

    /** The four star-marked safe cells. */
    val STAR_TILES: Set<Int> = setOf(8, 21, 44, 47)

    /** Returns true if [absoluteRingIndex] is a safe cell. */
    fun isSafe(absoluteRingIndex: Int): Boolean =
        absoluteRingIndex in SAFE_RING_INDICES

    /** Returns true if [absoluteRingIndex] is a star-marked safe cell. */
    fun isStar(absoluteRingIndex: Int): Boolean =
        absoluteRingIndex in STAR_TILES

    /** Returns true if [absoluteRingIndex] is a colored exit tile. */
    fun isExit(absoluteRingIndex: Int): Boolean =
        absoluteRingIndex in EXIT_TILES
}
