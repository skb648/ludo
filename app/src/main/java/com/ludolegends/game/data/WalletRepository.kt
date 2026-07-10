package com.ludolegends.game.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.ludoDataStore by preferencesDataStore("ludo_legends")

class WalletRepository(private val context: Context) {
    companion object { const val STARTING_BALANCE = 12_450; const val ENTRY_FEE = 500; const val WIN_PRIZE = 2_000 }
    private object Keys {
        val BALANCE = intPreferencesKey("balance")
        val SFX = floatPreferencesKey("sfx")
        val BGM = floatPreferencesKey("bgm")
        val HAPTICS = booleanPreferencesKey("haptics")
    }
    val balance: Flow<Int> = context.ludoDataStore.data.map { it[Keys.BALANCE] ?: STARTING_BALANCE }
    val sfxVolume: Flow<Float> = context.ludoDataStore.data.map { it[Keys.SFX] ?: .8f }
    val bgmVolume: Flow<Float> = context.ludoDataStore.data.map { it[Keys.BGM] ?: .35f }
    val hapticsEnabled: Flow<Boolean> = context.ludoDataStore.data.map { it[Keys.HAPTICS] ?: true }
    suspend fun tryDeductEntryFee(): Boolean { var ok=false; context.ludoDataStore.edit { val b=it[Keys.BALANCE]?:STARTING_BALANCE; ok=b>=ENTRY_FEE; if(ok) it[Keys.BALANCE]=b-ENTRY_FEE }; return ok }
    suspend fun refundEntryFee() = context.ludoDataStore.edit { it[Keys.BALANCE]=(it[Keys.BALANCE]?:STARTING_BALANCE)+ENTRY_FEE }
    suspend fun depositPrize(amount:Int=WIN_PRIZE) = context.ludoDataStore.edit { it[Keys.BALANCE]=(it[Keys.BALANCE]?:STARTING_BALANCE)+amount }
    suspend fun setSfxVolume(value:Float)=context.ludoDataStore.edit { it[Keys.SFX]=value.coerceIn(0f,1f) }
    suspend fun setBgmVolume(value:Float)=context.ludoDataStore.edit { it[Keys.BGM]=value.coerceIn(0f,1f) }
    suspend fun setHapticsEnabled(value:Boolean)=context.ludoDataStore.edit { it[Keys.HAPTICS]=value }
}
