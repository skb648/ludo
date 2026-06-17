package com.ludolegends.game.engine

/**
 * Pure rule-checking functions implementing the authentic Ludo King rules.
 *
 * Every function here is side-effect free and operates on [LudoGameState]
 * snapshots. The [LudoEngine] delegates to these to compute legal moves,
 * captures, and turn transitions.
 *
 * The four rule pillars:
 *  1. UNLOCK SIX RULE            — see [canUnlock]
 *  2. THREE CONSECUTIVE SIXES    — see [shouldBurnTurn]
 *  3. SAFEZONE & ELIMINATION     — see [computeCapture]
 *  4. EXACT ROLL HOME ENTRY      — see [canMoveToHome]
 */
object LudoRules {

    /** Maximum number of consecutive sixes before the turn is burned. */
    const val MAX_CONSECUTIVE_SIXES = 3

    /**
     * === RULE 1 — UNLOCK SIX RULE (UNBREAKABLE) ===
     *
     * A pawn inside the home base quadrant is locked and completely
     * unclickable. It can only be deployed onto the active clockwise
     * track index when the player inputs an exact physical dice value
     * of '6'.
     *
     * Returns true iff the pawn at [token] is at BASE and [diceValue] == 6.
     * The ViewModel/UI layer uses this to filter tap targets — a locked
     * base token is NEVER added to [LudoGameState.selectableTokenIds].
     */
    fun canUnlock(token: Token, diceValue: Int): Boolean {
        return token.isAtBase && diceValue == 6
    }

    /**
     * Strict gate used by [LudoEngine.prepareMove] to reject any attempt
     * to move a BASE-locked token without a 6. Defensive — should never
     * return true in normal flow because the UI also filters it.
     */
    fun isBaseLocked(token: Token, diceValue: Int): Boolean {
        return token.isAtBase && diceValue != 6
    }

    /**
     * === RULE 2 — THREE CONSECUTIVE SIXES INVALIDATION ===
     *
     * If the player has rolled [consecutiveSixes] 6s already, the next 6
     * (the 3rd consecutive) invalidates the turn entirely. The 3rd roll
     * is burned, the turn ends with a penalty, and control passes to the
     * next player.
     *
     * Returns true if this roll (a 6) should burn the turn.
     */
    fun shouldBurnTurn(consecutiveSixes: Int, currentDiceValue: Int): Boolean {
        return currentDiceValue == 6 && consecutiveSixes >= (MAX_CONSECUTIVE_SIXES - 1)
    }

    /**
     * Compute the destination step index for [token] given [diceValue].
     * Returns null when the move is illegal.
     *
     * Illegal cases (all unbreakable):
     *  • Token at BASE and dice != 6 → cannot unlock (RULE 1).
     *  • Token already HOME → no movement allowed.
     *  • Move would overshoot HOME (stepIndex 57) → illegal (RULE 4).
     *
     * Legal cases:
     *  • Token at BASE and dice == 6 → moves to stepIndex 0 (exit tile).
     *  • Token on ring or home column → stepIndex + diceValue, if ≤ 57.
     */
    fun computeDestination(token: Token, diceValue: Int): Int? {
        if (token.isHome) return null
        // RULE 1 — locked base can ONLY be unlocked with a 6.
        if (token.isAtBase) {
            return if (diceValue == 6) 0 else null
        }
        // RULE 4 — exact roll required to reach home terminal (stepIndex 57).
        val newStep = token.stepIndex + diceValue
        if (newStep > Token.HOME) return null
        return newStep
    }

    /**
     * Returns true if [token] can legally be moved by [diceValue] under
     * the current state. The UI uses this to highlight selectable tokens.
     */
    fun isLegalMove(state: LudoGameState, token: Token, diceValue: Int): Boolean {
        if (state.phase != TurnPhase.AWAITING_TOKEN_PICK) return false
        if (token.ownerId != state.currentPlayer) return false
        if (token.isHome) return false
        return computeDestination(token, diceValue) != null
    }

    /**
     * Compute the set of tokens of the given [player] that can legally
     * be moved with [diceValue]. Empty when no move is possible (in which
     * case the engine will auto-pass the turn).
     *
     * The [player] parameter is overridable so that in 2v2 team mode,
     * when the current player has all 4 pawns home, the engine can
     * compute legal moves against the teammate instead (turn redirection).
     */
    fun legalTokenIdsFor(
        state: LudoGameState,
        player: Player,
        diceValue: Int
    ): Set<Pair<Player, Int>> {
        val tokens = state.tokens[player].orEmpty()
        return tokens.mapIndexedNotNull { idx, token ->
            if (token.isHome) return@mapIndexedNotNull null
            val dest = computeDestination(token, diceValue) ?: return@mapIndexedNotNull null
            // Validate the destination isn't blocked by a teammate stack of 2+
            // (Standard Ludo King allows stacking; we always allow it.)
            Pair(player, idx) to dest
        }.map { it.first }.toSet()
    }

    /**
     * Compute the set of tokens of the current player that can legally
     * be moved with [diceValue]. Empty when no move is possible (in which
     * case the engine will auto-pass the turn).
     */
    fun legalTokenIds(state: LudoGameState, diceValue: Int): Set<Pair<Player, Int>> {
        return legalTokenIdsFor(state, state.currentPlayer, diceValue)
    }

    /**
     * === RULE 3 — SAFEZONE STAR EXTRACTION & ELIMINATION ===
     * === SECTION 4 — TEAMMATE SAFETY (2v2 mode) ===
     *
     * After a pawn lands on a cell, check for opponent tokens on the same
     * absolute ring index. Captures are blocked when:
     *   • Destination is a safe cell (star/exit) — pieces stack.
     *   • The opposing token is a teammate (2v2 team mode).
     *
     * On a non-safe cell with a true opponent, the opponent is eliminated
     * (sent back to BASE) and the active player earns a bonus roll.
     *
     * Returns the list of (opponent, tokenId) pairs that get captured.
     * Returns empty list when landing on a safe cell or no opponents present.
     */
    fun computeCapture(
        state: LudoGameState,
        movingPlayer: Player,
        tokenId: Int,
        destinationStep: Int
    ): List<Pair<Player, Int>> {
        if (destinationStep < 0 || destinationStep > Token.HOME_COLUMN_END) return emptyList()

        val absoluteRing = if (destinationStep in 0..51) {
            (movingPlayer.startIndex + destinationStep) % 52
        } else {
            return emptyList()
        }

        // Safe cells block all captures — pieces stack.
        if (SafeCells.isSafe(absoluteRing)) return emptyList()

        val captured = mutableListOf<Pair<Player, Int>>()
        for (player in state.turnOrder) {
            if (player == movingPlayer) continue
            // === SECTION 4 — Teammate safety ===
            // In team mode (2v2), teammates cannot capture each other.
            val isTeamMode = state.gameMode == GameMode.TEAM_2V2
            if (isTeamMode && Player.isTeammate(movingPlayer, player)) continue
            for ((idx, token) in state.tokens[player].orEmpty().withIndex()) {
                if (!token.isOnRing) continue
                if (token.absoluteRingIndex() == absoluteRing) {
                    captured += Pair(player, idx)
                }
            }
        }
        return captured
    }

    /**
     * === RULE 4 — EXACT ROLL HOME ENTRY ===
     *
     * Returns true iff the move would land the token exactly on the
     * terminal home step (stepIndex == 57). Overshoot is illegal.
     */
    fun canMoveToHome(token: Token, diceValue: Int): Boolean {
        if (token.isAtBase) return false
        val dest = token.stepIndex + diceValue
        return dest == Token.HOME
    }

    /**
     * Returns true if the current player must roll again (bonus roll)
     * given the outcome of this move. Bonus triggers:
     *   • Dice value was 6 (and not the third consecutive 6)
     *   • At least one opponent was captured
     *   • A pawn reached home on this move
     */
    fun shouldGrantBonusRoll(
        diceValue: Int,
        capturedCount: Int,
        reachedHome: Boolean,
        consecutiveSixes: Int
    ): Boolean {
        if (shouldBurnTurn(consecutiveSixes, diceValue)) return false
        if (diceValue == 6) return true
        if (capturedCount > 0) return true
        if (reachedHome) return true
        return false
    }

    /**
     * Computes the next player index after a turn ends. Used when the
     * active player cannot move or has exhausted their bonus rolls.
     */
    fun nextPlayerIndex(state: LudoGameState): Int {
        val next = (state.currentPlayerIndex + 1) % state.turnOrder.size
        return next
    }
}
