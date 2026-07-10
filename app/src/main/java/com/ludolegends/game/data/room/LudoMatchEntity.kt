// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * === SECTION 1 — ROOM SQLITE MATCH STATE PERSISTENCE ===
 *
 * A serialized snapshot of an entire Ludo match — saved automatically
 * after every valid piece movement so the player can resume gameplay
 * from the Lobby's "Resume Match History" list.
 *
 * Snapshot fields (serialized as JSON strings for portability):
 *   • [tokenStatesJson] — all 16 token coordinates on the grid.
 *   • [coinBalance] — wallet balance at the time of the save.
 *   • [playerNamesJson] — comma-separated player display names.
 *   • [activeTurnIndex] — whose turn it was when saved.
 *
 * Other fields:
 *   • [id] — auto-generated primary key.
 *   • [savedAt] — epoch millis when the snapshot was written.
 *   • [matchLabel] — human-readable label for the resume list.
 *   • [gameModeType] — LOCAL_PASS_PLAY vs PLAY_WITH_FRIENDS.
 *   • [gameMode] — STANDARD vs TEAM_2V2.
 *   • [playerCount] — number of players (2/3/4).
 *   • [consecutiveSixes] — turn-state counter at save time.
 *   • [lastDiceValue] — last rolled dice value at save time.
 *   • [winnerJson] — null if match ongoing, else the winning player name.
 *   • [scoreMatrixJson] — per-player score breakdown.
 */
@Entity(tableName = "ludo_matches")
data class LudoMatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "saved_at")
    val savedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "match_label")
    val matchLabel: String,

    @ColumnInfo(name = "game_mode_type")
    val gameModeType: String,    // GameModeType.name

    @ColumnInfo(name = "game_mode")
    val gameMode: String,        // GameMode.name

    @ColumnInfo(name = "player_count")
    val playerCount: Int,

    @ColumnInfo(name = "token_states_json")
    val tokenStatesJson: String,

    @ColumnInfo(name = "coin_balance")
    val coinBalance: Int,

    @ColumnInfo(name = "player_names_json")
    val playerNamesJson: String,

    @ColumnInfo(name = "active_turn_index")
    val activeTurnIndex: Int,

    @ColumnInfo(name = "consecutive_sixes")
    val consecutiveSixes: Int,

    @ColumnInfo(name = "last_dice_value")
    val lastDiceValue: Int,

    @ColumnInfo(name = "winner_json")
    val winnerJson: String?,

    @ColumnInfo(name = "score_matrix_json")
    val scoreMatrixJson: String
)
