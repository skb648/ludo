package com.ludolegends.game.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Hardened rule-engine regression tests. These lock in the four Ludo King
 * rule pillars so any future refactor that breaks them will fail CI.
 */
class LudoRulesTest {

    @Test
    fun `RULE 1 - locked base token can only move on a 6`() {
        val redBase = Token(id = 0, ownerId = Player.RED, stepIndex = Token.BASE)
        // Dice values 1-5 cannot unlock
        for (dice in 1..5) {
            assertNull("BASE token with dice=$dice should have no destination",
                LudoRules.computeDestination(redBase, dice))
            assertFalse("canUnlock should be false for dice=$dice",
                LudoRules.canUnlock(redBase, dice))
            assertTrue("isBaseLocked should be true for dice=$dice",
                LudoRules.isBaseLocked(redBase, dice))
        }
        // Dice value 6 unlocks
        assertEquals(0, LudoRules.computeDestination(redBase, 6))
        assertTrue(LudoRules.canUnlock(redBase, 6))
        assertFalse(LudoRules.isBaseLocked(redBase, 6))
    }

    @Test
    fun `RULE 2 - three consecutive sixes burn the turn`() {
        // After 0 sixes, rolling a 6 is fine
        assertFalse(LudoRules.shouldBurnTurn(0, 6))
        // After 1 six, rolling a 6 is fine (gives bonus)
        assertFalse(LudoRules.shouldBurnTurn(1, 6))
        // After 2 sixes, the 3rd six burns the turn
        assertTrue(LudoRules.shouldBurnTurn(2, 6))
        // Non-6 never burns
        assertFalse(LudoRules.shouldBurnTurn(2, 5))
        assertFalse(LudoRules.shouldBurnTurn(0, 5))
    }

    @Test
    fun `RULE 4 - exact roll required to reach home`() {
        // Token 1 step from home (stepIndex 56, HOME = 57)
        val nearHome = Token(id = 0, ownerId = Player.RED, stepIndex = 56)
        assertEquals(57, LudoRules.computeDestination(nearHome, 1))
        assertNull("Over-roll should be illegal", LudoRules.computeDestination(nearHome, 2))
        assertNull("Over-roll should be illegal", LudoRules.computeDestination(nearHome, 6))

        // Token 5 steps from home (stepIndex 52)
        val midHome = Token(id = 0, ownerId = Player.RED, stepIndex = 52)
        assertEquals(57, LudoRules.computeDestination(midHome, 5))
        assertNull(LudoRules.computeDestination(midHome, 6))
    }

    @Test
    fun `RULE 4 - token at HOME cannot move`() {
        val homeToken = Token(id = 0, ownerId = Player.RED, stepIndex = Token.HOME)
        for (dice in 1..6) {
            assertNull(LudoRules.computeDestination(homeToken, dice))
        }
    }

    @Test
    fun `RULE 3 - safe cells block captures`() {
        // Build a minimal state with Red at ring index 8 (star cell)
        // and Green also at ring index 8 (via its own stepIndex).
        val red = Player.RED
        val green = Player.GREEN
        // Red's stepIndex that maps to absolute ring index 8: 8 - 0 = 8.
        val redToken = Token(id = 0, ownerId = red, stepIndex = 8)
        // Green's stepIndex that maps to absolute ring 8: (8 - 13 + 52) % 52 = 47.
        val greenToken = Token(id = 0, ownerId = green, stepIndex = 47)
        val state = LudoGameState(
            turnOrder = listOf(red, green),
            currentPlayerIndex = 0,
            tokens = mapOf(
                red to listOf(redToken, Token(1, red), Token(2, red), Token(3, red)),
                green to listOf(greenToken, Token(1, green), Token(2, green), Token(3, green))
            ),
            phase = TurnPhase.AWAITING_TOKEN_PICK,
            lastDiceValue = 1
        )
        // Red moves to ring 8 (already there in this synthetic test — destination 8)
        val captured = LudoRules.computeCapture(state, red, tokenId = 0, destinationStep = 8)
        // Should NOT capture because ring index 8 is a star cell (safe).
        assertTrue("Safe cell should block captures", captured.isEmpty())
    }

    @Test
    fun `RULE 3 - non-safe cell captures opponent`() {
        val red = Player.RED
        val green = Player.GREEN
        // Red at ring index 7 (non-safe), Green also at ring index 7.
        val redToken = Token(id = 0, ownerId = red, stepIndex = 7)
        val greenToken = Token(id = 0, ownerId = green, stepIndex = (7 - 13 + 52) % 52)
        val state = LudoGameState(
            turnOrder = listOf(red, green),
            currentPlayerIndex = 0,
            tokens = mapOf(
                red to listOf(redToken, Token(1, red), Token(2, red), Token(3, red)),
                green to listOf(greenToken, Token(1, green), Token(2, green), Token(3, green))
            ),
            phase = TurnPhase.AWAITING_TOKEN_PICK,
            lastDiceValue = 1
        )
        val captured = LudoRules.computeCapture(state, red, tokenId = 0, destinationStep = 7)
        assertEquals(1, captured.size)
        assertEquals(green, captured.first().first)
        assertEquals(0, captured.first().second)
    }

    @Test
    fun `bonus roll granted on 6, capture, or reaching home`() {
        // 6 grants bonus (unless burning)
        assertTrue(LudoRules.shouldGrantBonusRoll(6, 0, false, 0))
        assertTrue(LudoRules.shouldGrantBonusRoll(6, 0, false, 1))
        // 3rd consecutive 6 does NOT grant bonus (burns instead)
        assertFalse(LudoRules.shouldGrantBonusRoll(6, 0, false, 2))
        // Capture grants bonus
        assertTrue(LudoRules.shouldGrantBonusRoll(3, 1, false, 0))
        // Reaching home grants bonus
        assertTrue(LudoRules.shouldGrantBonusRoll(4, 0, true, 0))
        // Otherwise no bonus
        assertFalse(LudoRules.shouldGrantBonusRoll(3, 0, false, 0))
    }

    @Test
    fun `legalTokenIds filters out locked base tokens without a 6`() {
        val red = Player.RED
        // All 4 tokens at BASE, dice = 3 → no legal moves
        val state = LudoGameState(
            turnOrder = listOf(red),
            currentPlayerIndex = 0,
            tokens = mapOf(red to List(4) { Token(it, red, Token.BASE) }),
            phase = TurnPhase.AWAITING_TOKEN_PICK,
            lastDiceValue = 3
        )
        assertTrue(LudoRules.legalTokenIds(state, 3).isEmpty())
        // With dice = 6, all 4 tokens can unlock
        assertEquals(4, LudoRules.legalTokenIds(state, 6).size)
    }

    // ============================================================
    // === SECTION 4 — 2v2 TEAM MODE TESTS ===
    // ============================================================

    @Test
    fun `SECTION 4 - team pairing is Red+Yellow vs Green+Blue`() {
        assertEquals(Player.teamOf(Player.RED), Player.teamOf(Player.YELLOW))
        assertEquals(Player.teamOf(Player.GREEN), Player.teamOf(Player.BLUE))
        assertNotEquals(Player.teamOf(Player.RED), Player.teamOf(Player.GREEN))
        assertTrue(Player.isTeammate(Player.RED, Player.YELLOW))
        assertTrue(Player.isTeammate(Player.GREEN, Player.BLUE))
        assertFalse(Player.isTeammate(Player.RED, Player.GREEN))
        assertFalse(Player.isTeammate(Player.RED, Player.BLUE))
    }

    @Test
    fun `SECTION 4 - teammates cannot capture each other in team mode`() {
        val red = Player.RED
        val yellow = Player.YELLOW
        // Red on ring index 7 (non-safe), Yellow also on ring index 7.
        // Yellow's stepIndex that maps to absolute ring 7: (7 - 36 + 52) % 52 = 23
        val redToken = Token(id = 0, ownerId = red, stepIndex = 7)
        val yellowToken = Token(id = 0, ownerId = yellow, stepIndex = 23)
        val state = LudoGameState(
            turnOrder = listOf(red, yellow),
            currentPlayerIndex = 0,
            tokens = mapOf(
                red to listOf(redToken, Token(1, red), Token(2, red), Token(3, red)),
                yellow to listOf(yellowToken, Token(1, yellow), Token(2, yellow), Token(3, yellow))
            ),
            phase = TurnPhase.AWAITING_TOKEN_PICK,
            lastDiceValue = 1,
            gameMode = GameMode.TEAM_2V2
        )
        // Red moves to ring 7 where Yellow is — Yellow is Red's teammate.
        // In team mode, NO capture should happen.
        val captured = LudoRules.computeCapture(state, red, tokenId = 0, destinationStep = 7)
        assertTrue("Teammates should not capture each other in team mode", captured.isEmpty())
    }

    @Test
    fun `SECTION 4 - non-teammates can still capture in team mode`() {
        val red = Player.RED
        val green = Player.GREEN
        val redToken = Token(id = 0, ownerId = red, stepIndex = 7)
        // Green's stepIndex that maps to absolute ring 7: (7 - 13 + 52) % 52 = 46
        val greenToken = Token(id = 0, ownerId = green, stepIndex = 46)
        val state = LudoGameState(
            turnOrder = listOf(red, green),
            currentPlayerIndex = 0,
            tokens = mapOf(
                red to listOf(redToken, Token(1, red), Token(2, red), Token(3, red)),
                green to listOf(greenToken, Token(1, green), Token(2, green), Token(3, green))
            ),
            phase = TurnPhase.AWAITING_TOKEN_PICK,
            lastDiceValue = 1,
            gameMode = GameMode.TEAM_2V2
        )
        val captured = LudoRules.computeCapture(state, red, tokenId = 0, destinationStep = 7)
        assertEquals(1, captured.size)
        assertEquals(green, captured.first().first)
    }
}
