package com.ludolegends.game.engine

object LudoRules {
    val SAFE_ABSOLUTE_CELLS = setOf(0, 8, 13, 21, 26, 34, 39, 47)

    fun toAbsolute(color: PlayerColor, relativePos: Int): Int =
        (color.startOffset + relativePos - 1).mod(52)

    fun isSafeCell(color: PlayerColor, relativePos: Int): Boolean =
        relativePos in 1..51 && toAbsolute(color, relativePos) in SAFE_ABSOLUTE_CELLS

    fun destinationFor(token: Token, dice: Int): Int? {
        require(dice in 1..6) { "Dice must be 1..6, got $dice" }
        if (token.isInBase) return if (dice == 6) 1 else null
        if (token.isHome) return null
        return (token.pos + dice).takeIf { it <= Token.POS_HOME }
    }

    fun effectiveColorFor(state: GameState, seatIndex: Int): PlayerColor {
        val seat = state.seats[seatIndex]
        if (state.mode != GameMode.TEAM_2V2) return seat.color
        if (state.tokensOf(seat.color).any { !it.isHome }) return seat.color
        val mate = state.seats.firstOrNull { it.teamId == seat.teamId && it.color != seat.color }
            ?: return seat.color
        return if (state.tokensOf(mate.color).any { !it.isHome }) mate.color else seat.color
    }

    fun movableTokens(state: GameState, dice: Int): List<Token> =
        state.tokensOf(effectiveColorFor(state, state.currentSeatIndex))
            .filter { destinationFor(it, dice) != null }

    data class DiceApplication(val state: GameState, val outcome: DiceOutcome)

    fun applyDice(state: GameState, dice: Int): DiceApplication {
        require(state.phase == TurnPhase.AWAIT_DICE) { "Dice not expected in ${state.phase}" }
        require(dice in 1..6) { "Dice must be 1..6" }
        val seat = state.currentSeat
        val sixes = if (dice == 6) state.consecutiveSixes + 1 else 0
        if (sixes >= 3) {
            val next = advanceSeat(state)
            return DiceApplication(
                state.copy(currentSeatIndex = next, phase = TurnPhase.AWAIT_DICE, diceValue = null,
                    consecutiveSixes = 0, movableTokenIds = emptyList(),
                    statusMessage = "Three sixes! ${seat.name}'s roll is void — ${state.seats[next].name}'s turn."),
                DiceOutcome.TripleSixPenalty
            )
        }
        val movable = movableTokens(state, dice)
        if (movable.isEmpty()) {
            val next = advanceSeat(state)
            return DiceApplication(
                state.copy(currentSeatIndex = next, diceValue = null, consecutiveSixes = 0,
                    movableTokenIds = emptyList(), statusMessage = "${seat.name} rolled $dice — no legal move. ${state.seats[next].name}'s turn."),
                DiceOutcome.NoMoves
            )
        }
        val ids = movable.map { it.id }
        return DiceApplication(
            state.copy(phase = TurnPhase.AWAIT_MOVE, diceValue = dice, consecutiveSixes = sixes,
                movableTokenIds = ids, statusMessage = if (ids.size == 1) "${seat.name} rolled $dice — moving pawn." else "${seat.name} rolled $dice — tap a glowing pawn."),
            DiceOutcome.ChooseMove(ids)
        )
    }

    fun applyMove(state: GameState, tokenId: Int): MoveResolution {
        require(state.phase == TurnPhase.AWAIT_MOVE)
        val dice = requireNotNull(state.diceValue)
        require(tokenId in state.movableTokenIds)
        val token = state.token(tokenId)
        val dest = requireNotNull(destinationFor(token, dice))
        val path = if (token.isInBase) listOf(1) else (token.pos + 1..dest).toList()
        val captured = if (dest in 1..51 && !isSafeCell(token.color, dest)) {
            val absolute = toAbsolute(token.color, dest)
            state.tokens.filter { other ->
                other.color != token.color && other.isOnMainTrack &&
                    !isTeammate(state, token.color, other.color) &&
                    toAbsolute(other.color, other.pos) == absolute
            }.map { it.id }
        } else emptyList()
        val moved = state.tokens.map {
            when {
                it.id == tokenId -> it.copy(pos = dest)
                it.id in captured -> it.copy(pos = Token.POS_BASE)
                else -> it
            }
        }
        val home = dest == Token.POS_HOME
        val scores = state.scores.toMutableMap().apply {
            this[token.color] = (this[token.color] ?: 0) + captured.size * 5 + if (home) 25 else 0
        }
        val colorDone = moved.count { it.color == token.color && it.isHome } == 4
        val finished = if (colorDone && token.color !in state.finishedColors) state.finishedColors + token.color else state.finishedColors
        val winner = when {
            !colorDone -> null
            state.mode == GameMode.STANDARD -> token.color
            else -> {
                val team = state.seats.filter { it.teamId == state.currentSeat.teamId }
                if (team.all { s -> moved.filter { it.color == s.color }.all(Token::isHome) }) token.color else null
            }
        }
        val extra = winner == null && (dice == 6 || captured.isNotEmpty() || home)
        val next = if (extra || winner != null) state.currentSeatIndex else advanceSeat(state.copy(tokens = moved, finishedColors = finished))
        val message = when {
            winner != null && state.mode == GameMode.TEAM_2V2 -> "${state.currentSeat.name}'s team wins!"
            winner != null -> "${state.currentSeat.name} wins the match!"
            colorDone && state.mode == GameMode.TEAM_2V2 -> "${token.color.displayName} is home! Now drive your teammate's pawns."
            home -> "Pawn reached HOME! Bonus roll."
            captured.isNotEmpty() -> "Opponent captured! Bonus roll."
            extra -> "Six rolled — roll again!"
            else -> "${state.seats[next].name}'s turn — roll the dice"
        }
        val resolved = state.copy(tokens = moved, currentSeatIndex = next,
            phase = if (winner != null) TurnPhase.FINISHED else TurnPhase.AWAIT_DICE,
            diceValue = null, consecutiveSixes = if (extra) state.consecutiveSixes else 0,
            movableTokenIds = emptyList(), finishedColors = finished, scores = scores, statusMessage = message)
        return MoveResolution(resolved, path, captured, home, extra, winner)
    }

    fun isTeammate(state: GameState, a: PlayerColor, b: PlayerColor): Boolean {
        if (state.mode != GameMode.TEAM_2V2) return false
        val sa = state.seats.firstOrNull { it.color == a } ?: return false
        val sb = state.seats.firstOrNull { it.color == b } ?: return false
        return sa.teamId == sb.teamId
    }

    private fun advanceSeat(state: GameState): Int {
        var idx = state.currentSeatIndex
        repeat(state.seats.size) {
            idx = (idx + 1) % state.seats.size
            val seat = state.seats[idx]
            val skip = if (state.mode == GameMode.TEAM_2V2) {
                state.seats.filter { it.teamId == seat.teamId }.all { s -> state.tokensOf(s.color).all(Token::isHome) }
            } else seat.color in state.finishedColors
            if (!skip) return idx
        }
        return (state.currentSeatIndex + 1) % state.seats.size
    }
}
