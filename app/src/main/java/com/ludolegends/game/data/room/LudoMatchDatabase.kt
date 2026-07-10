// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room SQLite database for Ludo Legends match-state persistence.
 *
 * Holds a single table of [LudoMatchEntity] rows — one per auto-saved
 * match snapshot. The Lobby's "RESUME MATCH HISTORY" list observes
 * [matchDao] live so newly-saved matches appear without a refresh.
 */
@Database(
    entities = [LudoMatchEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LudoMatchDatabase : RoomDatabase() {
    abstract fun matchDao(): LudoMatchDao

    companion object {
        @Volatile private var INSTANCE: LudoMatchDatabase? = null

        fun get(context: Context): LudoMatchDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    LudoMatchDatabase::class.java,
                    "ludo_legends_matches.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = db
                db
            }
        }
    }
}
