// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game

import android.app.Application
import com.ludolegends.game.engine.ManualDiceRoller

/**
 * Custom Application — currently a lightweight container; ready to host
 * DI containers, analytics, or background audio when needed.
 */
class LudoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
