// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game.data.room

import android.content.Context
import com.ludolegends.game.engine.LudoGameState
import com.ludolegends.game.engine.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Wraps [LudoMatchDao] with a high-level API for auto-saving and
 * resuming Ludo matches. Auto-save runs on the IO dispatcher so it
 * never blocks the UI thread.
 */
class LudoMatchRepository(context: Context) {

    private val dao = LudoMatchDatabase.get(context).matchDao()
    private val ioScope = CoroutineScope(Dispatchers.IO)

    /** Live list of saved matches (newest first), observed by the Lobby. */
    val savedMatches: Flow<List<LudoMatchEntity>> = dao.observeAllMatches()

    /**
     * === SECTION 1 — ASYNCHRONOUS AUTO-SAVE ===
     *
     * Serialize the current game state and upsert it on the IO dispatcher.
     * Called from the ViewModel after every valid piece movement.
     *
     * If [existingId] is non-null, the existing match row is updated
     * (preserving its primary key) instead of inserting a new row.
     * Returns the persisted row id via the [onSaved] callback.
     */
    fun autoSave(
        state: LudoGameState,
        coinBalance: Int,
        scoreMatrix: Map<Player, Int>,
        existingId: Long? = null,
        matchLabel: String? = null,
        onSaved: (Long) -> Unit = {}
    ) {
        ioScope.launch {
            val label = matchLabel ?: defaultLabel(state)
            val entity = LudoMatchSerializer.serialize(state, coinBalance, scoreMatrix, label)
            val withId = if (existingId != null) entity.copy(id = existingId) else entity
            val newId = dao.upsertMatch(withId)
            withContext(Dispatchers.Main) { onSaved(newId) }
        }
    }

    /** Fetch a single match by id — used when the user taps a resume card. */
    suspend fun getMatch(id: Long): LudoMatchEntity? = dao.getMatchById(id)

    /** Delete a saved match — called when the user finishes or discards one. */
    fun deleteMatch(id: Long) {
        ioScope.launch { dao.deleteMatchById(id) }
    }

    /** Delete all saved matches — used by the "Clear History" action. */
    fun clearAllMatches() {
        ioScope.launch { dao.clearAllMatches() }
    }

    private fun defaultLabel(state: LudoGameState): String {
        val modeShort = when (state.gameModeType) {
            com.ludolegends.game.engine.GameModeType.LOCAL_PASS_PLAY -> "Local"
            com.ludolegends.game.engine.GameModeType.PLAY_WITH_FRIENDS -> "Friends"
        }
        val mode2Short = if (state.gameMode == com.ludolegends.game.engine.GameMode.TEAM_2V2) "2v2" else "Std"
        return "$modeShort • $mode2Short • ${state.turnOrder.size}P"
    }
}
