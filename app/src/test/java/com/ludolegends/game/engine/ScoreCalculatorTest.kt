// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for the [ScoreCalculator] real-time dynamic score matrix.
 *
 * Formula: Score = (Blocks Covered) + (Captures × 50) + (Home Tokens × 100)
 */
class ScoreCalculatorTest {

    @Test
    fun `score is zero at game start (all tokens at BASE)`() {
        val state = LudoGameState(
            turnOrder = listOf(Player.RED, Player.GREEN),
            currentPlayerIndex = 0,
            tokens = mapOf(
                Player.RED to List(4) { Token(it, Player.RED, Token.BASE) },
                Player.GREEN to List(4) { Token(it, Player.GREEN, Token.BASE) }
            )
        )
        val scores = ScoreCalculator.compute(state)
        assertEquals(0, scores[Player.RED])
        assertEquals(0, scores[Player.GREEN])
    }

    @Test
    fun `score counts blocks covered on the ring`() {
        // Red token 0 at stepIndex 4 (5 blocks covered), others at BASE
        val state = LudoGameState(
            turnOrder = listOf(Player.RED),
            currentPlayerIndex = 0,
            tokens = mapOf(
                Player.RED to listOf(
                    Token(0, Player.RED, 4),
                    Token(1, Player.RED, Token.BASE),
                    Token(2, Player.RED, Token.BASE),
                    Token(3, Player.RED, Token.BASE)
                )
            )
        )
        val scores = ScoreCalculator.compute(state)
        assertEquals(5, scores[Player.RED])
    }

    @Test
    fun `score counts home column blocks correctly`() {
        // Token at stepIndex 54 (home column index 2) = 52 + (54-51) = 55 blocks
        val state = LudoGameState(
            turnOrder = listOf(Player.RED),
            currentPlayerIndex = 0,
            tokens = mapOf(
                Player.RED to listOf(
                    Token(0, Player.RED, 54),
                    Token(1, Player.RED, Token.BASE),
                    Token(2, Player.RED, Token.BASE),
                    Token(3, Player.RED, Token.BASE)
                )
            )
        )
        val scores = ScoreCalculator.compute(state)
        assertEquals(55, scores[Player.RED])
    }

    @Test
    fun `score counts a home token as 57 blocks plus 100 bonus`() {
        // Token at HOME (stepIndex 57) → 57 blocks + 100 bonus = 157
        val state = LudoGameState(
            turnOrder = listOf(Player.RED),
            currentPlayerIndex = 0,
            tokens = mapOf(
                Player.RED to listOf(
                    Token(0, Player.RED, Token.HOME),
                    Token(1, Player.RED, Token.BASE),
                    Token(2, Player.RED, Token.BASE),
                    Token(3, Player.RED, Token.BASE)
                )
            )
        )
        val scores = ScoreCalculator.compute(state)
        assertEquals(157, scores[Player.RED])
    }

    @Test
    fun `score counts captures at 50 points each`() {
        val red = Player.RED
        val green = Player.GREEN
        // Red has a turn record capturing 2 green tokens
        val record = TurnRecord(
            player = red,
            diceValue = 3,
            tokenId = 0,
            fromStep = 1,
            toStep = 4,
            captured = listOf(Pair(green, 0), Pair(green, 1))
        )
        val state = LudoGameState(
            turnOrder = listOf(red, green),
            currentPlayerIndex = 0,
            tokens = mapOf(
                red to listOf(
                    Token(0, red, 4),
                    Token(1, red, Token.BASE),
                    Token(2, red, Token.BASE),
                    Token(3, red, Token.BASE)
                ),
                green to List(4) { Token(it, green, Token.BASE) }
            ),
            turnHistory = listOf(record),
            historyPointer = 0
        )
        val scores = ScoreCalculator.compute(state)
        // Red: 5 blocks + 2 captures × 50 = 105
        assertEquals(105, scores[red])
    }
}
