// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Hardened Ludo King rule engine — rewritten as a strict Unidirectional
 * Data Flow (UDF) state machine exposed via [MutableStateFlow].
 *
 * === Architecture ===
 *  • The single source of truth is [_stateFlow] — a [MutableStateFlow<LudoGameState>].
 *  • Every mutation passes through [apply] which atomically `update{}`s the flow.
 *  • The ViewModel observes [state] and never holds a separate copy.
 *  • All input methods are synchronous and pure; side effects (animation,
 *    notifications) live in the ViewModel layer.
 *
 * === Enforced Rules ===
 *   RULE 1 — Locked Base Status: tokens at [Token.BASE] are non-clickable
 *            unless the current dice value is exactly 6.
 *   RULE 2 — Consecutive 6s Penalty: tracked by an isolated per-turn counter
 *            [LudoGameState.consecutiveSixes]. A third consecutive 6 burns
 *            the roll, posts a banner notification, and skips to the next
 *            player — no partial path is ever drawn.
 *   RULE 3 — Safe-Zone Multi-Stacking: the 8 safe cells are non-kill zones.
 *            Multiple tokens (same or opposing team) land on them without
 *            captures. The UI is responsible for scaling them to 0.65f and
 *            positioning them in an inner mini-grid so they remain individual
 *            click targets.
 *   RULE 4 — Precision Entry: a token must land exactly on stepIndex 57
 *            (Token.HOME) to finish. Over-rolls freeze the token and pass
 *            the turn without consuming the bonus.
 *
 * === Animation / Undo / Redo ===
 *  The engine uses a prepare / commit pattern:
 *    `submitDiceValue` / `selectToken` → stash a [PendingMove], enter
 *    [TurnPhase.PENDING_ANIMATION]. The ViewModel runs the hop coroutine.
 *    On completion it calls [commitPendingMove]. If the user hits Undo or
 *    Redo mid-animation, the ViewModel cancels its animation Job scope and
 *    then calls [undo] / [redo] — the engine's state is untouched during
 *    the animation, so no 'ghost token' can complete a phantom jump.
 */
class LudoEngine(
    private var diceRoller: DiceRoller = FairRandomDiceRoller()
) {

    private val _stateFlow = MutableStateFlow(LudoGameState())
    val state: StateFlow<LudoGameState> = _stateFlow.asStateFlow()

    /** Current snapshot — convenience accessor for non-Compose callers. */
    val currentState: LudoGameState get() = _stateFlow.value

    // ============================================================
    // Lifecycle
    // ============================================================

    /**
     * Initialize a new match. Each player gets 4 tokens locked at BASE.
     * The turn order is derived from [Player.orderFor].
     *
     * [modeType] selects between LOCAL_PASS_PLAY (animated 3D digital
     * dice, fair random) and PLAY_WITH_FRIENDS (manual dice injector).
     * Both modes are 100% human — no bots.
     */
    fun startNewGame(
        playerCount: Int,
        mode: GameMode = GameMode.STANDARD,
        modeType: GameModeType = GameModeType.LOCAL_PASS_PLAY
    ) {
        val order = Player.orderFor(playerCount)
        val tokens = order.associateWith { player ->
            List(4) { idx -> Token(id = idx, ownerId = player, stepIndex = Token.BASE) }
        }
        apply {
            it.copy(
                turnOrder = order,
                currentPlayerIndex = 0,
                tokens = tokens,
                phase = TurnPhase.AWAITING_DICE,
                gameMode = mode,
                gameModeType = modeType,
                lastDiceValue = 0,
                consecutiveSixes = 0,
                selectableTokenIds = emptySet(),
                notification = null,
                winner = null,
                turnHistory = emptyList(),
                historyPointer = -1,
                pendingMove = null,
                isAnimating = false,
                lastRollIsBot = false
            )
        }
    }

    // ============================================================
    // Dice input
    // ============================================================

    /**
     * Apply a manual dice value (from the on-screen dice-button panel).
     * Returns true if the roll was accepted, false if rejected due to phase
     * or animation lock.
     *
     * Behavior:
     *   • Phase must be AWAITING_DICE and not animating and no winner.
     *   • RULE 2 — if this is the 3rd consecutive 6, burn the roll, post a
     *     THREE_SIXES_BURN notification, advance to the next player.
     *   • Otherwise compute legal tokens:
     *       - No legal moves → either bonus retry (if 6) or pass turn.
     *       - Exactly one legal move → prepare pending move (auto-execute).
     *       - Multiple legal moves → enter AWAITING_TOKEN_PICK.
     */
    fun submitDiceValue(value: Int): Boolean {
        if (value !in 1..6) return false
        val s = _stateFlow.value
        if (s.isAnimating) return false
        if (s.winner != null) return false
        if (s.phase != TurnPhase.AWAITING_DICE) return false

        // === SECTION 4 — Team Mode Turn Redirection ===
        // If the current player has all 4 pawns home, they are NOT eliminated
        // from the turn list. Their dice roll is applied to the teammate's
        // legal tokens instead. We compute legal tokens against the teammate.
        val effectivePlayer = effectivePlayerForTurn(s)

        // === RULE 2 — Three consecutive sixes invalidation ===
        val newConsecutive = if (value == 6) s.consecutiveSixes + 1 else 0
        if (LudoRules.shouldBurnTurn(s.consecutiveSixes, value)) {
            val nextIdx = nextPlayerIndex(s)
            apply {
                it.copy(
                    lastDiceValue = value,
                    consecutiveSixes = 0,
                    phase = TurnPhase.AWAITING_DICE,
                    currentPlayerIndex = nextIdx,
                    selectableTokenIds = emptySet(),
                    pendingMove = null,
                    notification = GameNotification(
                        type = GameNotification.NotificationType.THREE_SIXES_BURN,
                        message = "Three consecutive sixes! ${s.currentPlayer.display}'s turn forfeited."
                    )
                )
            }
            return true
        }

        val legal = if (effectivePlayer == s.currentPlayer) {
            LudoRules.legalTokenIds(s, value)
        } else {
            // Team-mode turn redirection: highlight teammate's pieces instead.
            LudoRules.legalTokenIdsFor(s, effectivePlayer, value)
        }
        if (legal.isEmpty()) {
            // No legal move — pass turn unless the dice was 6 (bonus that they can't use,
            // but they keep the turn per Ludo King behavior).
            val bonusRoll = value == 6 && newConsecutive < LudoRules.MAX_CONSECUTIVE_SIXES
            if (bonusRoll) {
                apply {
                    it.copy(
                        lastDiceValue = value,
                        consecutiveSixes = newConsecutive,
                        phase = TurnPhase.AWAITING_DICE,
                        pendingMove = null,
                        notification = GameNotification(
                            type = GameNotification.NotificationType.INFO,
                            message = "No moves available — bonus roll granted."
                        )
                    )
                }
            } else {
                val nextIdx = nextPlayerIndex(s)
                apply {
                    it.copy(
                        lastDiceValue = value,
                        consecutiveSixes = 0,
                        phase = TurnPhase.AWAITING_DICE,
                        currentPlayerIndex = nextIdx,
                        pendingMove = null,
                        notification = GameNotification(
                            type = GameNotification.NotificationType.INFO,
                            message = "No moves available — turn passes to ${s.turnOrder[nextIdx].display}."
                        )
                    )
                }
            }
            return true
        }

        // Exactly one legal token → auto-execute path (prepare pending move).
        if (legal.size == 1) {
            val (player, tokenId) = legal.first()
            prepareMove(player, tokenId, value, newConsecutive)
            return true
        }

        // Multiple legal tokens → enter AWAITING_TOKEN_PICK.
        apply {
            it.copy(
                lastDiceValue = value,
                consecutiveSixes = newConsecutive,
                phase = TurnPhase.AWAITING_TOKEN_PICK,
                selectableTokenIds = legal,
                pendingMove = null,
                notification = null
            )
        }
        return true
    }

    /**
     * User selected a token to move (after multiple legal options existed).
     * Returns true if the selection was accepted.
     */
    fun selectToken(player: Player, tokenId: Int): Boolean {
        val s = _stateFlow.value
        if (s.isAnimating) return false
        if (s.phase != TurnPhase.AWAITING_TOKEN_PICK) return false
        val key = Pair(player, tokenId)
        if (key !in s.selectableTokenIds) return false
        prepareMove(player, tokenId, s.lastDiceValue, s.consecutiveSixes)
        return true
    }

    // ============================================================
    // Internal — prepare a pending move (no token mutation yet)
    // ============================================================

    private fun prepareMove(
        player: Player,
        tokenId: Int,
        diceValue: Int,
        currentConsecutive: Int
    ) {
        val s = _stateFlow.value
        val token = s.tokens[player]?.getOrNull(tokenId) ?: return

        val destination = LudoRules.computeDestination(token, diceValue) ?: run {
            // === RULE 4 — over-roll freezes the token ===
            apply {
                it.copy(
                    phase = TurnPhase.AWAITING_DICE,
                    consecutiveSixes = 0,
                    currentPlayerIndex = nextPlayerIndex(it),
                    pendingMove = null,
                    notification = GameNotification(
                        type = GameNotification.NotificationType.BLOCKED_MOVE,
                        message = "Move blocked — exact roll required to enter home."
                    )
                )
            }
            return
        }

        // === RULE 3 — captures only on non-safe cells ===
        val captured = LudoRules.computeCapture(s, player, tokenId, destination)

        val isUnlock = token.isAtBase && diceValue == 6
        val reachedHome = destination == Token.HOME

        // Pre-compute winner status by simulating the move
        val isWinningMove = s.tokens[player]!!.withIndex().all { (idx, t) ->
            if (idx == tokenId) destination == Token.HOME else t.isHome
        }

        val newConsecutive = if (diceValue == 6) currentConsecutive + 1 else 0
        val grantBonus = LudoRules.shouldGrantBonusRoll(
            diceValue = diceValue,
            capturedCount = captured.size,
            reachedHome = reachedHome,
            consecutiveSixes = currentConsecutive
        )

        val pending = PendingMove(
            player = player,
            tokenId = tokenId,
            diceValue = diceValue,
            fromStep = token.stepIndex,
            toStep = destination,
            captured = captured,
            isUnlock = isUnlock,
            reachedHome = reachedHome,
            isWinningMove = isWinningMove,
            grantBonus = grantBonus
        )

        apply {
            it.copy(
                phase = TurnPhase.PENDING_ANIMATION,
                pendingMove = pending,
                consecutiveSixes = newConsecutive,
                selectableTokenIds = emptySet(),
                notification = null,
                isAnimating = true
            )
        }
    }

    /**
     * Commit the pending move to the canonical state. Called by the ViewModel
     * ONLY after the hop animation completes (or is cancelled).
     *
     * If the pending move was cancelled (e.g. user hit Undo mid-animation),
     * the ViewModel calls [cancelPendingMove] instead — never this method.
     */
    fun commitPendingMove() {
        val s = _stateFlow.value
        val pending = s.pendingMove ?: run {
            // Defensive: no pending move to commit. Just clear the animating flag.
            if (s.isAnimating) apply { it.copy(isAnimating = false) }
            return
        }

        val newTokens = s.tokens.toMutableMap().mapValues { (_, list) -> list.toMutableList() }
        val movedToken = newTokens[pending.player]!![pending.tokenId]
        newTokens[pending.player]!![pending.tokenId] =
            movedToken.copy(stepIndex = pending.toStep)
        for ((capPlayer, capIdx) in pending.captured) {
            val capturedToken = newTokens[capPlayer]!![capIdx]
            newTokens[capPlayer]!![capIdx] = capturedToken.copy(stepIndex = Token.BASE)
        }

        val record = TurnRecord(
            player = pending.player,
            diceValue = pending.diceValue,
            tokenId = pending.tokenId,
            fromStep = pending.fromStep,
            toStep = pending.toStep,
            captured = pending.captured
        )

        val notification = when {
            pending.isWinningMove -> GameNotification(
                type = GameNotification.NotificationType.WINNER,
                message = "${pending.player.display} wins the match!"
            )
            pending.reachedHome -> GameNotification(
                type = GameNotification.NotificationType.HOME_REACHED,
                message = "${pending.player.display} reached home!"
            )
            pending.captured.isNotEmpty() -> GameNotification(
                type = GameNotification.NotificationType.CAPTURE,
                message = "${pending.player.display} captured ${pending.captured.size} token(s)! Bonus roll granted."
            )
            pending.isUnlock -> GameNotification(
                type = GameNotification.NotificationType.UNLOCK,
                message = "Rolled a 6! Pawn unlocked."
            )
            pending.grantBonus -> GameNotification(
                type = GameNotification.NotificationType.BONUS_ROLL,
                message = "Bonus roll!"
            )
            else -> null
        }

        val newHistory = s.turnHistory + record
        val newPointer = newHistory.lastIndex

        apply {
            if (pending.isWinningMove) {
                it.copy(
                    tokens = newTokens,
                    phase = TurnPhase.GAME_OVER,
                    winner = pending.player,
                    lastDiceValue = pending.diceValue,
                    consecutiveSixes = 0,
                    selectableTokenIds = emptySet(),
                    turnHistory = newHistory,
                    historyPointer = newPointer,
                    notification = notification,
                    pendingMove = null,
                    isAnimating = false
                )
            } else if (pending.grantBonus) {
                it.copy(
                    tokens = newTokens,
                    phase = TurnPhase.AWAITING_DICE,
                    lastDiceValue = pending.diceValue,
                    selectableTokenIds = emptySet(),
                    turnHistory = newHistory,
                    historyPointer = newPointer,
                    notification = notification,
                    pendingMove = null,
                    isAnimating = false
                )
            } else {
                it.copy(
                    tokens = newTokens,
                    phase = TurnPhase.AWAITING_DICE,
                    currentPlayerIndex = nextPlayerIndex(it.copy(tokens = newTokens)),
                    lastDiceValue = pending.diceValue,
                    consecutiveSixes = 0,
                    selectableTokenIds = emptySet(),
                    turnHistory = newHistory,
                    historyPointer = newPointer,
                    notification = notification,
                    pendingMove = null,
                    isAnimating = false
                )
            }
        }
    }

    /**
     * Cancel any pending move without committing it. Used when the user
     * triggers Undo/Redo (or any other navigation) mid-animation. The
     * engine returns to AWAITING_DICE without mutating tokens.
     */
    fun cancelPendingMove() {
        val s = _stateFlow.value
        if (s.pendingMove == null && !s.isAnimating) return
        apply {
            it.copy(
                pendingMove = null,
                isAnimating = false,
                phase = if (s.phase == TurnPhase.PENDING_ANIMATION)
                            TurnPhase.AWAITING_DICE
                        else s.phase,
                selectableTokenIds = emptySet()
            )
        }
    }

    // ============================================================
    // Undo / Redo
    // ============================================================

    fun undo(): Boolean {
        val s = _stateFlow.value
        if (s.isAnimating) return false   // caller must cancel animation first
        if (s.historyPointer < 0) return false
        val replay = s.turnHistory.subList(0, s.historyPointer)
        val rebuilt = rebuildFromHistory(s, replay)
        apply { rebuilt }
        return true
    }

    fun redo(): Boolean {
        val s = _stateFlow.value
        if (s.isAnimating) return false
        if (s.historyPointer >= s.turnHistory.lastIndex) return false
        val replay = s.turnHistory.subList(0, s.historyPointer + 2)
        val rebuilt = rebuildFromHistory(s, replay)
        apply { rebuilt }
        return true
    }

    private fun rebuildFromHistory(base: LudoGameState, history: List<TurnRecord>): LudoGameState {
        var rebuilt = base.copy(
            tokens = base.turnOrder.associateWith { p ->
                List(4) { idx -> Token(id = idx, ownerId = p, stepIndex = Token.BASE) }
            },
            currentPlayerIndex = 0,
            phase = TurnPhase.AWAITING_DICE,
            consecutiveSixes = 0,
            selectableTokenIds = emptySet(),
            winner = null,
            lastDiceValue = 0,
            notification = null,
            historyPointer = history.size - 1,
            pendingMove = null,
            isAnimating = false
        )
        for (record in history) {
            val token = rebuilt.tokens[record.player]!![record.tokenId]
            val newTokens = rebuilt.tokens.toMutableMap().mapValues { (_, l) -> l.toMutableList() }
            newTokens[record.player]!![record.tokenId] = token.copy(stepIndex = record.toStep)
            for ((capPlayer, capIdx) in record.captured) {
                val cap = newTokens[capPlayer]!![capIdx]
                newTokens[capPlayer]!![capIdx] = cap.copy(stepIndex = Token.BASE)
            }
            val bonus = record.diceValue == 6 ||
                        record.captured.isNotEmpty() ||
                        record.toStep == Token.HOME
            val nextIdx = if (bonus) rebuilt.currentPlayerIndex
                          else (rebuilt.currentPlayerIndex + 1) % rebuilt.turnOrder.size
            rebuilt = rebuilt.copy(
                tokens = newTokens,
                currentPlayerIndex = nextIdx,
                lastDiceValue = record.diceValue
            )
        }
        return rebuilt
    }

    // ============================================================
    // Utility
    // ============================================================

    fun clearNotification() {
        apply { it.copy(notification = null) }
    }

    /**
     * === SECTION 1 — MATCH HISTORY RECOVERY ===
     *
     * Replace the engine's entire state with [newState]. Used by the
     * ViewModel when resuming a saved match from Room. Bypasses the
     * normal UDF pipeline because the state was already validated at
     * save time.
     */
    fun restoreState(newState: LudoGameState) {
        apply { newState.copy(isAnimating = false, pendingMove = null) }
    }

    fun setManualDiceValue(value: Int) {
        (diceRoller as? ManualDiceRoller)?.setValue(value)
    }

    /**
     * Swap the dice roller implementation at runtime. Used by the ViewModel
     * when starting a new match in a different mode:
     *   • LOCAL_PASS_PLAY → [FairRandomDiceRoller]
     *   • PLAY_WITH_FRIENDS → [ManualDiceRoller]
     */
    fun setDiceRoller(newRoller: DiceRoller) {
        diceRoller = newRoller
    }

    private fun nextPlayerIndex(s: LudoGameState): Int =
        (s.currentPlayerIndex + 1) % s.turnOrder.size

    /**
     * === SECTION 4 — TEAM MODE TURN REDIRECTION ===
     *
     * If the current player has all 4 pawns home (hasWon == true), they
     * are NOT eliminated from the turn list. Instead, the engine
     * computes legal moves against their teammate. This prevents freezing
     * when one teammate finishes before the other.
     *
     * Returns the [Player] whose tokens should be considered for legal
     * move computation. In solo mode this is always [s.currentPlayer].
     * In team mode, if [s.currentPlayer] has won, it returns their
     * teammate.
     */
    private fun effectivePlayerForTurn(s: LudoGameState): Player {
        val current = s.currentPlayer
        if (s.gameMode != GameMode.TEAM_2V2) return current
        if (!s.hasWon(current)) return current
        // Find the teammate in the turn order.
        for (candidate in s.turnOrder) {
            if (candidate == current) continue
            if (Player.isTeammate(current, candidate)) return candidate
        }
        return current
    }

    /** Single atomic mutation point — every state change passes through here. */
    private inline fun apply(transform: (LudoGameState) -> LudoGameState) {
        _stateFlow.update(transform)
    }
}
