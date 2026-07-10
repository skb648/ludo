// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game.engine

/**
 * === SECTION 4 — REAL-TIME DYNAMIC SCORE MATRIX ===
 *
 * Computes the live score for every player using the formula:
 *
 *   Score = (Total Grid Blocks Covered by All 4 Player Tokens)
 *         + (Enemy Captures × 50 Points)
 *         + (Tokens Reached Home Triangle × 100 Points)
 *
 * Run on every move. The UI animates the numerical updates via ticker
 * transitions inside the horizontal profile banners.
 *
 * Capture counts are derived from the turn history — each [TurnRecord]
 * with a non-empty `captured` list contributes to the moving player's
 * capture total.
 */
object ScoreCalculator {

    /**
     * Compute the per-player score map for the given [state].
     *
     * Block coverage: tokens at BASE contribute 0, tokens on the ring
     * contribute their stepIndex, tokens in the home column contribute
     * 52 + (their home-column position), tokens at HOME contribute 57.
     */
    fun compute(state: LudoGameState): Map<Player, Int> {
        val capturesByPlayer = mutableMapOf<Player, Int>()
        for (record in state.turnHistory) {
            val current = capturesByPlayer[record.player] ?: 0
            capturesByPlayer[record.player] = current + record.captured.size
        }

        val scores = mutableMapOf<Player, Int>()
        for (player in state.turnOrder) {
            val tokens = state.tokens[player].orEmpty()
            val blocksCovered = tokens.sumOf { token ->
                when {
                    token.isAtBase -> 0
                    token.isOnRing -> token.stepIndex + 1   // stepIndex 0 = 1 block covered
                    token.isInHomeColumn -> 52 + (token.stepIndex - 51)
                    token.isHome -> 57
                    else -> 0
                }
            }
            val captures = capturesByPlayer[player] ?: 0
            val homeCount = tokens.count { it.isHome }
            val score = blocksCovered + (captures * 50) + (homeCount * 100)
            scores[player] = score
        }
        return scores
    }
}
