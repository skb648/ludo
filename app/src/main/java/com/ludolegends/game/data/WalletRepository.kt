// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

/**
 * Jetpack DataStore-backed persistent wallet + match-economy repository.
 *
 * === SECTION 3 — REAL ECONOMY SYSTEM ===
 *
 * Stored values:
 *   • COINS — current wallet balance (starts at 12,450).
 *   • MATCHES_PLAYED — lifetime counter.
 *   • MATCHES_WON — lifetime counter.
 *   • BGM_VOLUME, SFX_VOLUME, HAPTIC_ENABLED — master settings.
 *
 * Entry fee deduction is atomic and verified via [deductEntryFee] —
 * returns false if the wallet has insufficient funds. Winner loot
 * deposit is performed via [depositWinnings] which adds the prize pool
 * back securely.
 */
private val Context.ludoDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ludo_legends_store"
)

class WalletRepository(private val context: Context) {

    companion object {
        const val INITIAL_COINS = 12450
        const val ENTRY_FEE = 500
        // 2v2 team winner pool: 4× entry fees minus house cut.
        const val TEAM_WINNER_POOL = 1800
        // Solo winner pool: 4× entry fees minus house cut.
        const val SOLO_WINNER_POOL = 1500

        private val COINS_KEY = intPreferencesKey("coins")
        private val MATCHES_PLAYED_KEY = intPreferencesKey("matches_played")
        private val MATCHES_WON_KEY = intPreferencesKey("matches_won")
        private val BGM_VOLUME_KEY = floatPreferencesKey("bgm_volume")
        private val SFX_VOLUME_KEY = floatPreferencesKey("sfx_volume")
        private val HAPTIC_KEY = booleanPreferencesKey("haptic_enabled")
    }

    /** Live wallet balance as a Flow — collect for UI updates. */
    val coins: Flow<Int> = context.ludoDataStore.data.map { it[COINS_KEY] ?: INITIAL_COINS }

    val matchesPlayed: Flow<Int> = context.ludoDataStore.data.map { it[MATCHES_PLAYED_KEY] ?: 0 }
    val matchesWon: Flow<Int> = context.ludoDataStore.data.map { it[MATCHES_WON_KEY] ?: 0 }

    /** Live BGM volume in 0..1f. */
    val bgmVolume: Flow<Float> = context.ludoDataStore.data.map { it[BGM_VOLUME_KEY] ?: 0.5f }
    /** Live SFX volume in 0..1f. */
    val sfxVolume: Flow<Float> = context.ludoDataStore.data.map { it[SFX_VOLUME_KEY] ?: 0.8f }
    /** Live haptic-enabled flag. */
    val hapticEnabled: Flow<Boolean> = context.ludoDataStore.data.map { it[HAPTIC_KEY] ?: true }

    /** Current wallet balance — snapshot via a single Flow emission. */
    suspend fun currentCoins(): Int = coins.first()

    /**
     * === SECTION 3 — ENTRY FEE DEDUCTION ===
     *
     * Verifies sufficient funds, deducts [ENTRY_FEE], increments the
     * matches-played counter, and updates persistent storage atomically.
     * Returns true on success, false if the wallet lacks funds.
     */
    suspend fun deductEntryFee(): Boolean {
        var success = false
        context.ludoDataStore.edit { prefs ->
            val current = prefs[COINS_KEY] ?: INITIAL_COINS
            if (current >= ENTRY_FEE) {
                prefs[COINS_KEY] = current - ENTRY_FEE
                prefs[MATCHES_PLAYED_KEY] = (prefs[MATCHES_PLAYED_KEY] ?: 0) + 1
                success = true
            } else {
                success = false
            }
        }
        return success
    }

    /**
     * === SECTION 3 — WINNER LOOT DEPOSIT ===
     *
     * Deposits the [prizeAmount] into the wallet, increments the
     * matches-won counter, and returns the new balance.
     */
    suspend fun depositWinnings(prizeAmount: Int): Int {
        var newBalance = 0
        context.ludoDataStore.edit { prefs ->
            val current = prefs[COINS_KEY] ?: INITIAL_COINS
            newBalance = current + prizeAmount
            prefs[COINS_KEY] = newBalance
            prefs[MATCHES_WON_KEY] = (prefs[MATCHES_WON_KEY] ?: 0) + 1
        }
        return newBalance
    }

    /**
     * Compute the prize pool for the given mode. 2v2 team mode awards a
     * larger pool because both teammates split it (but we deposit the
     * full pool to the human winner's wallet — the teammate is AI-controlled
     * in this build, so the human collects the full pot).
     */
    fun prizePoolFor(soloMode: Boolean): Int =
        if (soloMode) SOLO_WINNER_POOL else TEAM_WINNER_POOL

    // === Settings ===

    suspend fun setBgmVolume(volume: Float) {
        context.ludoDataStore.edit { it[BGM_VOLUME_KEY] = volume.coerceIn(0f, 1f) }
    }

    suspend fun setSfxVolume(volume: Float) {
        context.ludoDataStore.edit { it[SFX_VOLUME_KEY] = volume.coerceIn(0f, 1f) }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.ludoDataStore.edit { it[HAPTIC_KEY] = enabled }
    }
}
