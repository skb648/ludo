package com.ludolegends.game.data.room

import com.ludolegends.game.engine.GameMode
import com.ludolegends.game.engine.GameModeType
import com.ludolegends.game.engine.LudoGameState
import com.ludolegends.game.engine.Player
import com.ludolegends.game.engine.Token
import com.ludolegends.game.engine.TurnPhase

/**
 * Serializer that converts a live [LudoGameState] into a persistable
 * [LudoMatchEntity] and back. Uses a hand-rolled compact JSON format
 * (no external dependency) to keep the binary small and parsing fast.
 *
 * Snapshot fields serialized:
 *   • Per-player token step indices (4 tokens × 4 players = 16 coordinates).
 *   • Coin balance at save time.
 *   • Player display names.
 *   • Active turn index.
 *   • Consecutive sixes counter.
 *   • Last dice value.
 *   • Winner (null if ongoing).
 *   • Per-player score breakdown.
 */
object LudoMatchSerializer {

    /**
     * Build a [LudoMatchEntity] from the current game state. The caller
     * supplies the [coinBalance] from the wallet repository and the
     * [scoreMatrix] from the score calculator.
     */
    fun serialize(
        state: LudoGameState,
        coinBalance: Int,
        scoreMatrix: Map<Player, Int>,
        matchLabel: String = defaultLabel(state)
    ): LudoMatchEntity {
        val tokenStates = buildString {
            append("{")
            append("\"players\":[")
            val players = state.turnOrder
            for ((i, player) in players.withIndex()) {
                append("{")
                append("\"name\":\"${player.name}\",")
                append("\"tokens\":[")
                val tokens = state.tokens[player].orEmpty()
                for ((j, token) in tokens.withIndex()) {
                    append(token.stepIndex)
                    if (j < tokens.lastIndex) append(",")
                }
                append("]}")
                if (i < players.lastIndex) append(",")
            }
            append("]}")
        }

        val playerNames = buildString {
            append("[")
            for ((i, p) in state.turnOrder.withIndex()) {
                append("\"${p.display}\"")
                if (i < state.turnOrder.lastIndex) append(",")
            }
            append("]")
        }

        val scoreMatrixJson = buildString {
            append("{")
            for ((i, player) in state.turnOrder.withIndex()) {
                append("\"${player.name}\":${scoreMatrix[player] ?: 0}")
                if (i < state.turnOrder.lastIndex) append(",")
            }
            append("}")
        }

        return LudoMatchEntity(
            matchLabel = matchLabel,
            gameModeType = state.gameModeType.name,
            gameMode = state.gameMode.name,
            playerCount = state.turnOrder.size,
            tokenStatesJson = tokenStates,
            coinBalance = coinBalance,
            playerNamesJson = playerNames,
            activeTurnIndex = state.currentPlayerIndex,
            consecutiveSixes = state.consecutiveSixes,
            lastDiceValue = state.lastDiceValue,
            winnerJson = state.winner?.name,
            scoreMatrixJson = scoreMatrixJson
        )
    }

    /**
     * Reconstruct a [LudoGameState] from a saved [entity]. The caller
     * (typically the ViewModel) will then `engine.startNewGame(...)` to
     * initialize a fresh state and immediately apply this snapshot to
     * resume gameplay exactly where the player left off.
     *
     * Returns a [ResumedState] bundle containing the rebuilt game state
     * plus metadata needed by the caller (mode type, player count, etc.).
     */
    fun deserialize(entity: LudoMatchEntity): ResumedState {
        val modeType = GameModeType.valueOf(entity.gameModeType)
        val gameMode = GameMode.valueOf(entity.gameMode)
        val playerCount = entity.playerCount

        // Rebuild turn order from the player count.
        val turnOrder = Player.orderFor(playerCount)

        // Parse token states: {"players":[{"name":"RED","tokens":[s0,s1,s2,s3]},...]}
        val tokensByPlayer = mutableMapOf<Player, List<Int>>()
        val playersRegex = Regex("\"name\":\"(\\w+)\",\"tokens\":\\[([^\\]]*)\\]")
        for (match in playersRegex.findAll(entity.tokenStatesJson)) {
            val playerName = match.groupValues[1]
            val stepsStr = match.groupValues[2]
            val player = runCatching { Player.valueOf(playerName) }.getOrNull() ?: continue
            val steps = if (stepsStr.isBlank()) emptyList()
                        else stepsStr.split(",").map { it.trim().toInt() }
            tokensByPlayer[player] = steps
        }

        // Build the token map.
        val tokens = turnOrder.associateWith { player ->
            val steps = tokensByPlayer[player] ?: List(4) { Token.BASE }
            List(4) { idx ->
                val step = steps.getOrNull(idx) ?: Token.BASE
                Token(id = idx, ownerId = player, stepIndex = step)
            }
        }

        // Parse score matrix.
        val scoreMatrix = mutableMapOf<Player, Int>()
        val scoreRegex = Regex("\"(\\w+)\":(\\d+)")
        for (match in scoreRegex.findAll(entity.scoreMatrixJson)) {
            val playerName = match.groupValues[1]
            val score = match.groupValues[2].toInt()
            val player = runCatching { Player.valueOf(playerName) }.getOrNull() ?: continue
            scoreMatrix[player] = score
        }

        val state = LudoGameState(
            turnOrder = turnOrder,
            currentPlayerIndex = entity.activeTurnIndex.coerceIn(0, turnOrder.lastIndex),
            tokens = tokens,
            lastDiceValue = entity.lastDiceValue,
            consecutiveSixes = entity.consecutiveSixes,
            phase = TurnPhase.AWAITING_DICE,
            winner = entity.winnerJson?.let { runCatching { Player.valueOf(it) }.getOrNull() },
            gameMode = gameMode,
            gameModeType = modeType
        )

        return ResumedState(
            state = state,
            coinBalance = entity.coinBalance,
            scoreMatrix = scoreMatrix,
            matchId = entity.id,
            matchLabel = entity.matchLabel
        )
    }

    private fun defaultLabel(state: LudoGameState): String {
        val modeShort = when (state.gameModeType) {
            GameModeType.LOCAL_PASS_PLAY -> "Local"
            GameModeType.PLAY_WITH_FRIENDS -> "Friends"
        }
        val mode2Short = if (state.gameMode == GameMode.TEAM_2V2) "2v2" else "Std"
        return "$modeShort • $mode2Short • ${state.turnOrder.size}P"
    }

    data class ResumedState(
        val state: LudoGameState,
        val coinBalance: Int,
        val scoreMatrix: Map<Player, Int>,
        val matchId: Long,
        val matchLabel: String
    )
}
