package com.ludolegends.game.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.ludolegends.game.engine.BoardMap
import com.ludolegends.game.engine.LudoGameState
import com.ludolegends.game.engine.Player
import com.ludolegends.game.engine.TurnPhase

/**
 * Transparent overlay that captures taps anywhere on the board and
 * forwards them as token-pick events when the tap lands inside a
 * cell containing a selectable token of the current player.
 */
@Composable
fun TokenTouchOverlay(
    state: LudoGameState,
    cellSizeFraction: Float,
    onTokenTapped: (Player, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectableCells = remember(state) {
        if (state.phase != TurnPhase.AWAITING_TOKEN_PICK) emptyMap()
        else {
            // Map (row, col) -> selectable (player, tokenId) pairs
            val map = mutableMapOf<Pair<Int, Int>, MutableList<Pair<Player, Int>>>()
            for ((player, tokenId) in state.selectableTokenIds) {
                val token = state.tokens[player]?.getOrNull(tokenId) ?: continue
                val cell = BoardMap.tokenToCell(token) ?: continue
                map.getOrPut(cell) { mutableListOf() }.add(Pair(player, tokenId))
            }
            map
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(state) {
                detectTapGestures(
                    onTap = { tapOffset ->
                        val totalSize = size.width.toFloat()  // board is square
                        if (totalSize <= 0f) return@detectTapGestures
                        val cellPx = totalSize / BoardMap.GRID_SIZE
                        val col = ((tapOffset.x) / cellPx).toInt().coerceIn(0, BoardMap.GRID_SIZE - 1)
                        val row = ((tapOffset.y) / cellPx).toInt().coerceIn(0, BoardMap.GRID_SIZE - 1)
                        val candidates = selectableCells[Pair(row, col)] ?: return@detectTapGestures
                        // Pick the first selectable token on the tapped cell
                        val first = candidates.first()
                        onTokenTapped(first.first, first.second)
                    }
                )
            }
    )
}
