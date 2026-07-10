package com.ludolegends.game.engine

enum class PlayerColor(val startOffset: Int, val displayName: String) {
    RED(0, "Red"), GREEN(13, "Green"), YELLOW(26, "Yellow"), BLUE(39, "Blue")
}
enum class SeatKind { HUMAN, BOT }
enum class GameMode { STANDARD, TEAM_2V2 }
enum class TurnPhase { AWAIT_DICE, AWAIT_MOVE, ANIMATING, FINISHED }

data class PlayerSeat(
    val color: PlayerColor,
    val kind: SeatKind = SeatKind.HUMAN,
    val name: String,
    val teamId: Int
)

data class Token(val id: Int, val color: PlayerColor, val pos: Int = POS_BASE) {
    val isInBase get() = pos == POS_BASE
    val isHome get() = pos == POS_HOME
    val isOnMainTrack get() = pos in 1..MAIN_TRACK_END
    val isInHomeColumn get() = pos in HOME_COLUMN_START until POS_HOME
    companion object {
        const val POS_BASE = -1
        const val MAIN_TRACK_END = 51
        const val HOME_COLUMN_START = 52
        const val POS_HOME = 57
    }
}

sealed interface DiceOutcome {
    data class ChooseMove(val movableTokenIds: List<Int>) : DiceOutcome
    data object NoMoves : DiceOutcome
    data object TripleSixPenalty : DiceOutcome
}

data class MoveResolution(
    val state: GameState,
    val pathPositions: List<Int>,
    val capturedTokenIds: List<Int>,
    val reachedHome: Boolean,
    val grantsExtraTurn: Boolean,
    val winnerDeclared: PlayerColor?
)

data class GameState(
    val mode: GameMode,
    val seats: List<PlayerSeat>,
    val tokens: List<Token>,
    val currentSeatIndex: Int = 0,
    val phase: TurnPhase = TurnPhase.AWAIT_DICE,
    val diceValue: Int? = null,
    val consecutiveSixes: Int = 0,
    val movableTokenIds: List<Int> = emptyList(),
    val finishedColors: List<PlayerColor> = emptyList(),
    val statusMessage: String = "",
    val scores: Map<PlayerColor, Int> = seats.associate { it.color to 0 }
) {
    val currentSeat: PlayerSeat get() = seats[currentSeatIndex]
    fun tokensOf(color: PlayerColor) = tokens.filter { it.color == color }
    fun token(id: Int) = tokens.first { it.id == id }

    companion object {
        fun newMatch(mode: GameMode, seats: List<PlayerSeat>): GameState {
            require(seats.size in 2..4)
            val tokens = seats.flatMapIndexed { seatIndex, seat ->
                List(4) { Token(seatIndex * 4 + it, seat.color) }
            }
            return GameState(
                mode = mode,
                seats = seats,
                tokens = tokens,
                statusMessage = "${seats.first().name}'s turn — roll the dice"
            )
        }
    }
}
