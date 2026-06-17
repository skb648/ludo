package com.ludolegends.game.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * === SECTION 2 — HAPTIC FEEDBACK ENGINE ===
 *
 * Wraps the system [Vibrator] for crisp low-amplitude pulses on:
 *   • Dice button click (short tick)
 *   • Token landing on a box (slightly stronger tap)
 *   • Token capture (sharp double-pulse)
 *   • Victory (long rising vibration pattern)
 *
 * On Android API 26+ uses [VibrationEffect.createOneShot] / [createWaveform]
 * with explicit amplitude control. Below 26 falls back to the deprecated
 * [Vibrator.vibrate] (no amplitude control).
 *
 * The engine respects the user's haptic-enabled preference — callers
 * should check [HapticFeedbackEngine.enabled] before invoking.
 */
class HapticFeedbackEngine(context: Context) {

    private val vibrator: Vibrator? = run {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val mgr = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            mgr?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    @Volatile var enabled: Boolean = true

    /** Crisp short tick — used on dice button click. */
    fun tick() {
        if (!enabled) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(TICK_MS, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(TICK_MS)
        }
    }

    /** Slightly stronger tap — used on token landing. */
    fun tap() {
        if (!enabled) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(TAP_MS, TAP_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(TAP_MS)
        }
    }

    /** Sharp double-pulse — used on token capture. */
    fun capture() {
        if (!enabled) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        val timings = longArrayOf(0, 30, 50, 60)
        val amplitudes = intArrayOf(0, 200, 0, 255)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(timings, -1)
        }
    }

    /** Long rising pattern — used on victory. */
    fun victory() {
        if (!enabled) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        val timings = longArrayOf(0, 80, 60, 120, 80, 200)
        val amplitudes = intArrayOf(0, 120, 0, 180, 0, 255)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(timings, -1)
        }
    }

    companion object {
        private const val TICK_MS = 15L
        private const val TAP_MS = 25L
        private const val TAP_AMPLITUDE = 180
    }
}

/**
 * CompositionLocal that exposes the [HapticFeedbackEngine] to the
 * Compose tree. Injected at the activity level.
 */
val LocalHapticEngine = staticCompositionLocalOf<HapticFeedbackEngine?> { null }

/**
 * Convenience helper for composables that just need a single tick —
 * uses Compose's built-in [LocalHapticFeedback] which routes through
 * the system's preferred haptic API.
 */
@Composable
fun rememberHapticTick(): () -> Unit {
    val haptic = LocalHapticFeedback.current
    return { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) }
}
