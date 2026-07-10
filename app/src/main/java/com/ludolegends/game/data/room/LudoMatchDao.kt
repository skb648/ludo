// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data-access object for [LudoMatchEntity]. Exposes asynchronous auto-save
 * upsert plus a live Flow of saved matches for the Lobby's resume list.
 */
@Dao
interface LudoMatchDao {

    /**
     * === SECTION 1 — ASYNCHRONOUS AUTO-SAVE UPSERT ===
     *
     * Insert or replace the match snapshot. Called from a background
     * coroutine after every valid piece movement.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMatch(entity: LudoMatchEntity): Long

    /** Live list of all saved matches, newest first. */
    @Query("SELECT * FROM ludo_matches ORDER BY saved_at DESC")
    fun observeAllMatches(): Flow<List<LudoMatchEntity>>

    /** Fetch a single match by id — used when the user taps a resume card. */
    @Query("SELECT * FROM ludo_matches WHERE id = :id LIMIT 1")
    suspend fun getMatchById(id: Long): LudoMatchEntity?

    /** Delete a saved match — called when the user finishes or discards one. */
    @Query("DELETE FROM ludo_matches WHERE id = :id")
    suspend fun deleteMatchById(id: Long)

    /** Delete all saved matches — used by the "Clear History" action. */
    @Query("DELETE FROM ludo_matches")
    suspend fun clearAllMatches()
}
