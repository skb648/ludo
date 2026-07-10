// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game.data.room

import com.ludolegends.game.engine.GameMode
import com.ludolegends.game.engine.GameModeType
import com.ludolegends.game.engine.LudoGameState
import com.ludolegends.game.engine.Player
import com.ludolegends.game.engine.Token
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [LudoMatchSerializer] — verifies that a serialized match
 * state can be deserialized back into an identical game state.
 */
class LudoMatchSerializerTest {

    @Test
    fun `serialize and deserialize round-trip preserves token positions`() {
        val red = Player.RED
        val green = Player.GREEN
        val state = LudoGameState(
            turnOrder = listOf(red, green),
            currentPlayerIndex = 1,
            tokens = mapOf(
                red to listOf(
                    Token(0, red, 5),
                    Token(1, red, Token.BASE),
                    Token(2, red, 12),
                    Token(3, red, Token.HOME)
                ),
                green to listOf(
                    Token(0, green, 3),
                    Token(1, green, Token.BASE),
                    Token(2, green, Token.BASE),
                    Token(3, green, 7)
                )
            ),
            gameMode = GameMode.STANDARD,
            gameModeType = GameModeType.LOCAL_PASS_PLAY,
            lastDiceValue = 4,
            consecutiveSixes = 1
        )
        val scoreMatrix = mapOf(red to 250, green to 80)

        val entity = LudoMatchSerializer.serialize(
            state = state,
            coinBalance = 12450,
            scoreMatrix = scoreMatrix,
            matchLabel = "Test Match"
        )

        val resumed = LudoMatchSerializer.deserialize(entity)

        assertEquals(listOf(red, green), resumed.state.turnOrder)
        assertEquals(1, resumed.state.currentPlayerIndex)
        assertEquals(5, resumed.state.tokens[red]!![0].stepIndex)
        assertEquals(Token.BASE, resumed.state.tokens[red]!![1].stepIndex)
        assertEquals(12, resumed.state.tokens[red]!![2].stepIndex)
        assertEquals(Token.HOME, resumed.state.tokens[red]!![3].stepIndex)
        assertEquals(3, resumed.state.tokens[green]!![0].stepIndex)
        assertEquals(Token.BASE, resumed.state.tokens[green]!![1].stepIndex)
        assertEquals(7, resumed.state.tokens[green]!![3].stepIndex)
        assertEquals(GameModeType.LOCAL_PASS_PLAY, resumed.state.gameModeType)
        assertEquals(GameMode.STANDARD, resumed.state.gameMode)
        assertEquals(12450, resumed.coinBalance)
        assertEquals(250, resumed.scoreMatrix[red])
        assertEquals(80, resumed.scoreMatrix[green])
    }

    @Test
    fun `deserialize handles empty turn history cleanly`() {
        val state = LudoGameState(
            turnOrder = listOf(Player.RED),
            currentPlayerIndex = 0,
            tokens = mapOf(
                Player.RED to List(4) { Token(it, Player.RED, Token.BASE) }
            )
        )
        val entity = LudoMatchSerializer.serialize(state, 1000, emptyMap())
        val resumed = LudoMatchSerializer.deserialize(entity)
        assertEquals(0, resumed.state.turnHistory.size)
        assertEquals(0, resumed.scoreMatrix.size)
    }
}
