package com.ludolegends.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.ludolegends.game.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * === SECTION 2 — ROBUST HARDWARE-ACCELERATED AUDIO ENGINE ===
 *
 * Two-layer audio architecture:
 *   • SoundPool (low-latency SFX) — for dice roll, token hop, capture, victory.
 *     Loaded from `res/raw/{dice_roll,token_hop,token_kill,victory}.wav`.
 *   • ExoPlayer (BGM) — for the ambient theme loop.
 *     Loaded from `res/raw/bgm_ambient.wav`.
 *
 * Volume control:
 *   • [setBgmVolume] — sets the BGM player's volume in 0..1f, smoothly
 *     via ExoPlayer's audio component (no stream re-creation).
 *   • [setSfxVolume] — sets the SFX master gain applied to every
 *     SoundPool.play call.
 *
 * The engine is best-effort: if a clip fails to load or ExoPlayer is
 * unavailable, calls become no-ops so the game keeps running.
 */
class LudoAudioEngine private constructor(
    private val context: Context,
    private val soundPool: SoundPool,
    private val clipIds: Map<Clip, Int>,
    private val bgmPlayer: ExoPlayer?
) {
    enum class Clip { DICE_ROLL, TOKEN_HOP, TOKEN_KILL, VICTORY }

    private val ready = AtomicBoolean(false)
    private val scope = CoroutineScope(Dispatchers.IO)

    @Volatile private var sfxVolume: Float = 0.8f
    @Volatile private var bgmVolume: Float = 0.5f

    init {
        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) ready.set(true)
        }
        scope.launch {
            kotlinx.coroutines.delay(1000)
            ready.set(true)
        }
    }

    // === SFX ===

    fun playDiceRoll() = play(Clip.DICE_ROLL)
    fun playTokenHop() = play(Clip.TOKEN_HOP)
    fun playTokenKill() = play(Clip.TOKEN_KILL)
    fun playVictory() = play(Clip.VICTORY)

    private fun play(clip: Clip) {
        if (!ready.get()) return
        val id = clipIds[clip] ?: return
        if (id <= 0) return
        val v = sfxVolume.coerceIn(0f, 1f)
        soundPool.play(id, v, v, PRIORITY_NORMAL, NO_LOOP, NORMAL_RATE)
    }

    /** Set the SFX master volume (0..1). Applies to all future play() calls. */
    fun setSfxVolume(volume: Float) {
        sfxVolume = volume.coerceIn(0f, 1f)
    }

    // === BGM ===

    /** Start the ambient BGM loop. Idempotent — calling twice does nothing. */
    fun startBgm() {
        val player = bgmPlayer ?: return
        if (player.isPlaying) return
        try {
            val uri = "android.resource://${context.packageName}/${R.raw.bgm_ambient}"
            player.setMediaItem(MediaItem.fromUri(uri))
            player.volume = bgmVolume.coerceIn(0f, 1f)
            player.repeatMode = Player.REPEAT_MODE_ONE
            player.prepare()
            player.playWhenReady = true
        } catch (_: Exception) {
            // Best-effort — BGM is non-critical.
        }
    }

    /** Set the BGM volume (0..1). Smoothly applies to the ExoPlayer instance. */
    fun setBgmVolume(volume: Float) {
        bgmVolume = volume.coerceIn(0f, 1f)
        bgmPlayer?.volume = bgmVolume
    }

    fun pauseBgm() { bgmPlayer?.pause() }
    fun resumeBgm() { bgmPlayer?.playWhenReady = true }

    /** Release all audio resources — call from Activity.onDestroy. */
    fun release() {
        soundPool.release()
        bgmPlayer?.release()
    }

    companion object {
        private const val PRIORITY_NORMAL = 1
        private const val NO_LOOP = 0
        private const val NORMAL_RATE = 1.0f
        private const val MAX_STREAMS = 6

        fun create(context: Context): LudoAudioEngine {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val pool = SoundPool.Builder()
                .setMaxStreams(MAX_STREAMS)
                .setAudioAttributes(attrs)
                .build()
            val clips = mapOf(
                Clip.DICE_ROLL to safeLoad(context, pool, R.raw.dice_roll),
                Clip.TOKEN_HOP to safeLoad(context, pool, R.raw.token_hop),
                Clip.TOKEN_KILL to safeLoad(context, pool, R.raw.token_kill),
                Clip.VICTORY   to safeLoad(context, pool, R.raw.victory)
            )
            val bgmPlayer = try {
                ExoPlayer.Builder(context).build().apply { volume = 0.5f }
            } catch (_: Exception) { null }
            return LudoAudioEngine(context, pool, clips, bgmPlayer)
        }

        private fun safeLoad(context: Context, pool: SoundPool, @RawRes resId: Int): Int {
            return try {
                if (resId == 0) 0 else pool.load(context, resId, PRIORITY_NORMAL)
            } catch (_: Exception) { 0 }
        }
    }
}

val LocalLudoAudio = staticCompositionLocalOf<LudoAudioEngine?> { null }

@Composable
fun ProvideLudoAudio(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val engine = remember { LudoAudioEngine.create(context) }
    DisposableEffect(engine) {
        onDispose { engine.release() }
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalLudoAudio provides engine) {
        content()
    }
}
