// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game.engine

/**
 * Static map of the Ludo board.
 *
 * The board is a 15×15 grid (225 cells). It is composed of:
 *  • 4 home-base quadrants (6×6 each) in the four corners — these hold the
 *    colored bases where pawns start.
 *  • A cross-shaped path through the middle — each arm is 3 cells wide × 6 tall.
 *  • A 3×3 center triangle (the final "home" goal).
 *
 * Each cell on the board has a (row, col) coordinate in 0..14.
 * The 52-cell main ring and the per-player 5-cell home columns are addressed
 * by [ringIndex] (0..51) and [homeColumnIndex] (0..4) respectively.
 *
 * Coordinate convention:
 *   row 0    = top of screen
 *   row 14   = bottom of screen
 *   col 0    = left of screen
 *   col 14   = right of screen
 *
 * Ring traced clockwise from Red's exit tile (6,1):
 *   (6,1) → (6,5)          — 5 cells, top of bottom-left arm, going right
 *   (5,6) → (1,6)          — 5 cells, left side of top arm, going up
 *   (0,6) (0,7) (0,8)      — 3 cells, top edge
 *   (1,8) → (5,8)          — 5 cells, right side of top arm, going down
 *   (6,9) → (6,14)         — 6 cells, top of bottom-right arm, going right
 *   (7,14)                 — 1 cell, right edge middle
 *   (8,14) → (8,9)         — 6 cells, bottom of top-right arm, going left
 *   (9,8) → (13,8)         — 5 cells, right side of bottom arm, going down
 *   (14,8) (14,7) (14,6)   — 3 cells, bottom edge
 *   (13,6) → (9,6)         — 5 cells, left side of bottom arm, going up
 *   (8,5) → (8,0)          — 6 cells, bottom of top-left arm, going left
 *   (7,0)                  — 1 cell, left edge middle
 *   (6,0)                  — 1 cell, closure corner
 *   back to (6,1)
 *   Total = 5+5+3+5+6+1+6+5+3+5+6+1+1 = 52 ✓
 */
object BoardMap {

    const val GRID_SIZE = 15
    const val TOTAL_CELLS = GRID_SIZE * GRID_SIZE
    const val RING_LENGTH = 52
    const val HOME_COLUMN_LENGTH = 5

    /**
     * The 52 cells of the main ring, in clockwise order, as (row, col) pairs.
     * Index 0 is Red's exit tile (row 6 col 1).
     */
    val RING_COORDS: List<Pair<Int, Int>> = listOf(
        // 0..4 — Red exit, going right along row 6
        Pair(6, 1), Pair(6, 2), Pair(6, 3), Pair(6, 4), Pair(6, 5),
        // 5..9 — left side of top arm, going up col 6
        Pair(5, 6), Pair(4, 6), Pair(3, 6), Pair(2, 6), Pair(1, 6),
        // 10..12 — top edge
        Pair(0, 6), Pair(0, 7), Pair(0, 8),
        // 13..17 — right side of top arm, going down col 8 (13 = Green exit)
        Pair(1, 8), Pair(2, 8), Pair(3, 8), Pair(4, 8), Pair(5, 8),
        // 18..23 — top of bottom-right arm, going right along row 6
        Pair(6, 9), Pair(6, 10), Pair(6, 11), Pair(6, 12), Pair(6, 13), Pair(6, 14),
        // 24 — right edge middle
        Pair(7, 14),
        // 25..30 — bottom of top-right arm, going left along row 8
        Pair(8, 14), Pair(8, 13), Pair(8, 12), Pair(8, 11), Pair(8, 10), Pair(8, 9),
        // 31..35 — right side of bottom arm, going down col 8
        Pair(9, 8), Pair(10, 8), Pair(11, 8), Pair(12, 8), Pair(13, 8),
        // 36..38 — bottom edge (36 = Yellow exit, going left)
        Pair(14, 8), Pair(14, 7), Pair(14, 6),
        // 39..43 — left side of bottom arm, going up col 6 (39 = Blue exit)
        Pair(13, 6), Pair(12, 6), Pair(11, 6), Pair(10, 6), Pair(9, 6),
        // 44..49 — bottom of top-left arm, going left along row 8
        Pair(8, 5), Pair(8, 4), Pair(8, 3), Pair(8, 2), Pair(8, 1), Pair(8, 0),
        // 50 — left edge middle
        Pair(7, 0),
        // 51 — closure corner
        Pair(6, 0)
    )

    init {
        check(RING_COORDS.size == RING_LENGTH) {
            "Ring must be exactly $RING_LENGTH cells, got ${RING_COORDS.size}"
        }
        check(RING_COORDS.toSet().size == RING_LENGTH) {
            "Ring coordinates contain duplicates"
        }
    }

    /**
     * Home-column coordinates for each player — 5 cells leading into center.
     * Index 0 is closest to the ring entrance, index 4 is closest to center triangle.
     */
    val HOME_COLUMN_COORDS: Map<Player, List<Pair<Int, Int>>> = mapOf(
        Player.RED to listOf(
            Pair(7, 1), Pair(7, 2), Pair(7, 3), Pair(7, 4), Pair(7, 5)
        ),
        Player.GREEN to listOf(
            Pair(1, 7), Pair(2, 7), Pair(3, 7), Pair(4, 7), Pair(5, 7)
        ),
        Player.YELLOW to listOf(
            Pair(7, 13), Pair(7, 12), Pair(7, 11), Pair(7, 10), Pair(7, 9)
        ),
        Player.BLUE to listOf(
            Pair(13, 7), Pair(12, 7), Pair(11, 7), Pair(10, 7), Pair(9, 7)
        )
    )

    /** The center home triangle cells (3×3 in the middle of the board). */
    val CENTER_TRIANGLE: List<Pair<Int, Int>> = buildList {
        for (r in 6..8) for (c in 6..8) add(Pair(r, c))
    }

    /** The 4 parking slots inside each player's home-base quadrant. */
    val BASE_SLOTS: Map<Player, List<Pair<Int, Int>>> = mapOf(
        Player.RED to listOf(Pair(1, 1), Pair(1, 4), Pair(4, 1), Pair(4, 4)),
        Player.GREEN to listOf(Pair(1, 10), Pair(1, 13), Pair(4, 10), Pair(4, 13)),
        Player.YELLOW to listOf(Pair(10, 10), Pair(10, 13), Pair(13, 10), Pair(13, 13)),
        Player.BLUE to listOf(Pair(10, 1), Pair(10, 4), Pair(13, 1), Pair(13, 4))
    )

    /** Quadrant bounds for each player (rowRange × colRange). */
    val QUADRANTS: Map<Player, Pair<IntRange, IntRange>> = mapOf(
        Player.RED to (0..5 to 0..5),
        Player.GREEN to (0..5 to 9..14),
        Player.YELLOW to (9..14 to 9..14),
        Player.BLUE to (9..14 to 0..5)
    )

    /**
     * Convert a Token to its (row, col) position on the 15×15 grid.
     * Returns null when the token is already HOME (center triangle).
     */
    fun tokenToCell(token: Token): Pair<Int, Int>? {
        return when {
            token.isAtBase -> BASE_SLOTS[token.ownerId]!![token.id]
            token.isOnRing -> {
                val absoluteRing = token.absoluteRingIndex()
                RING_COORDS[absoluteRing]
            }
            token.isInHomeColumn -> {
                val homeIdx = token.stepIndex - 52   // 0..4
                HOME_COLUMN_COORDS[token.ownerId]!![homeIdx]
            }
            else -> null  // HOME — center triangle, no single cell
        }
    }

    /** Returns the (row, col) of a ring index. */
    fun ringCell(absoluteRingIndex: Int): Pair<Int, Int> =
        RING_COORDS[absoluteRingIndex]

    /** Returns the (row, col) of a home-column cell for [player] at [homeIdx]. */
    fun homeColumnCell(player: Player, homeIdx: Int): Pair<Int, Int> =
        HOME_COLUMN_COORDS[player]!![homeIdx]

    /** True if the (row, col) is inside the player's home-base quadrant. */
    fun isInBaseQuadrant(player: Player, row: Int, col: Int): Boolean {
        val (rRange, cRange) = QUADRANTS[player]!!
        return row in rRange && col in cRange
    }

    /** Linear index for a (row, col) pair — useful for Canvas composition. */
    fun linearIndex(row: Int, col: Int): Int = row * GRID_SIZE + col
}
