package com.ludolegends.game.jui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.ludolegends.game.engine.Player
import com.ludolegends.game.ui.theme.GoldBright
import com.ludolegends.game.ui.theme.PlayerBlue
import com.ludolegends.game.ui.theme.PlayerGreen
import com.ludolegends.game.ui.theme.PlayerRed
import com.ludolegends.game.ui.theme.PlayerYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// ============================================================
// SECTION 1.1 — PULSATING TURN GLOW
// ============================================================

/**
 * When a player's turn is active, all eligible tokens display a smooth
 * repeating neon glowing aura/halo via [infiniteRepeatable]. Unmovable
 * pieces remain dim.
 *
 * Renders the glow ring as an overlay on the board. Driven by an
 * [InfiniteTransition] whose phase advances continuously.
 *
 * @param selectableTokens set of (player, tokenId) pairs that should glow.
 * @param cellCenters map from (player, tokenId) to canvas pixel center.
 * @param tokenRadiusPx base radius of a token in pixels.
 */
@Composable
fun PulsatingGlowOverlay(
    selectableTokens: Set<Pair<Player, Int>>,
    cellCenters: Map<Pair<Player, Int>, Offset>,
    tokenRadiusPx: Float,
    modifier: Modifier = Modifier
) {
    if (selectableTokens.isEmpty()) return
    val infinite = rememberInfiniteTransition(label = "glow-pulse")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow-phase"
    )
    Canvas(modifier = modifier.fillMaxSize()) {
        val baseAlpha = 0.55f + 0.45f * phase
        val radiusBoost = 1.4f + 0.20f * phase
        for (key in selectableTokens) {
            val center = cellCenters[key] ?: continue
            val player = key.first
            val glowColor = when (player) {
                Player.RED    -> PlayerRed
                Player.GREEN  -> PlayerGreen
                Player.YELLOW -> PlayerYellow
                Player.BLUE   -> PlayerBlue
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = baseAlpha * 0.55f),
                        glowColor.copy(alpha = baseAlpha * 0.25f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = tokenRadiusPx * radiusBoost * 1.8f
                ),
                radius = tokenRadiusPx * radiusBoost * 1.8f,
                center = center
            )
            drawCircle(
                color = GoldBright.copy(alpha = baseAlpha * 0.85f),
                radius = tokenRadiusPx * radiusBoost,
                center = center,
                style = Stroke(width = 3f)
            )
        }
    }
}

// ============================================================
// SECTION 1.2 — SQUASH & STRETCH LANDING
// ============================================================

/**
 * Holds the live (scaleX, scaleY) applied to the landing token and
 * exposes a [trigger] function that fires the squash-and-stretch
 * animation: compress height → 0.7f, expand width → 1.3f, then snap
 * back to 1.0f via elastic overshoot spring.
 */
@Composable
fun rememberSquashAnim(): SquashAnim {
    val scope = rememberCoroutineScope()
    val scaleX = remember { Animatable(1f) }
    val scaleY = remember { Animatable(1f) }
    return remember(scaleX, scaleY) {
        SquashAnim(
            scope = scope,
            scaleXAnim = scaleX,
            scaleYAnim = scaleY
        )
    }
}

class SquashAnim internal constructor(
    private val scope: kotlinx.coroutines.CoroutineScope,
    private val scaleXAnim: Animatable<Float, androidx.compose.animation.core.AnimationVector1D>,
    private val scaleYAnim: Animatable<Float, androidx.compose.animation.core.AnimationVector1D>
) {
    val scaleX: Float get() = scaleXAnim.value
    val scaleY: Float get() = scaleYAnim.value
    val active: Boolean get() = scaleX != 1f || scaleY != 1f

    fun trigger() {
        scope.launch {
            // Phase A: compress height, expand width (60ms)
            scaleXAnim.snapTo(1f)
            scaleYAnim.snapTo(1f)
            scaleXAnim.animateTo(
                targetValue = 1.3f,
                animationSpec = tween(60, easing = FastOutSlowInEasing)
            )
            scaleYAnim.animateTo(
                targetValue = 0.7f,
                animationSpec = tween(60, easing = FastOutSlowInEasing)
            )
            // Phase B: elastic overshoot spring back to 1.0f
            scaleXAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            scaleYAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }
}

// ============================================================
// SECTION 1.3 — SCREEN SHAKE & PARTICLE BLAST
// ============================================================

/**
 * Holds the live shake offset for the board canvas. Call [trigger] on
 * token capture to fire a 380ms decaying shake.
 */
@Composable
fun rememberScreenShake(): ScreenShake {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    return remember(offsetX, offsetY) {
        ScreenShake(scope = scope, offsetXAnim = offsetX, offsetYAnim = offsetY)
    }
}

class ScreenShake internal constructor(
    private val scope: kotlinx.coroutines.CoroutineScope,
    private val offsetXAnim: Animatable<Float, androidx.compose.animation.core.AnimationVector1D>,
    private val offsetYAnim: Animatable<Float, androidx.compose.animation.core.AnimationVector1D>
) {
    val offsetX: Float get() = offsetXAnim.value
    val offsetY: Float get() = offsetYAnim.value
    val active: Boolean get() = offsetX != 0f || offsetY != 0f

    fun trigger() {
        scope.launch {
            val rng = Random.Default
            val durationMs = 380
            val start = System.currentTimeMillis()
            while (true) {
                val elapsed = System.currentTimeMillis() - start
                if (elapsed >= durationMs) break
                val decay = 1f - (elapsed.toFloat() / durationMs)
                val dx = (rng.nextFloat() - 0.5f) * 18f * decay
                val dy = (rng.nextFloat() - 0.5f) * 18f * decay
                offsetXAnim.snapTo(dx)
                offsetYAnim.snapTo(dy)
                delay(16)
            }
            offsetXAnim.snapTo(0f)
            offsetYAnim.snapTo(0f)
        }
    }
}

/**
 * Particle burst overlay — emits N alpha-fading colored particles from
 * a center point and animates them outward in random directions with
 * gravity. Re-fires whenever [triggerKey] changes to a non-zero value.
 */
@Composable
fun ParticleBurstOverlay(
    triggerKey: Int,
    center: Offset,
    particleColors: List<Color>,
    modifier: Modifier = Modifier
) {
    var particles by remember { mutableStateOf<List<Particle>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(triggerKey) {
        if (triggerKey == 0) return@LaunchedEffect
        val newParticles = mutableListOf<Particle>()
        val rng = Random(triggerKey.toLong())
        for (i in 0 until 24) {
            val angle = (rng.nextFloat() * Math.PI * 2).toFloat()
            val speed = 80f + rng.nextFloat() * 160f
            val color = particleColors[i % particleColors.size]
            newParticles += Particle(
                x = center.x,
                y = center.y,
                vx = (Math.cos(angle.toDouble()).toFloat() * speed),
                vy = (Math.sin(angle.toDouble()).toFloat() * speed),
                color = color,
                size = 4f + rng.nextFloat() * 6f,
                alpha = 1f
            )
        }
        particles = newParticles
        scope.launch {
            val start = System.currentTimeMillis()
            while (true) {
                val elapsed = System.currentTimeMillis() - start
                if (elapsed > 800) break
                delay(16)
                val dt = 0.016f
                val gravity = 380f
                particles = particles.map { p ->
                    p.copy(
                        x = p.x + p.vx * dt,
                        y = p.y + p.vy * dt,
                        vy = p.vy + gravity * dt,
                        alpha = (1f - elapsed / 800f).coerceAtLeast(0f)
                    )
                }
            }
            particles = emptyList()
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        for (p in particles) {
            drawCircle(
                color = p.color.copy(alpha = p.alpha),
                radius = p.size,
                center = Offset(p.x, p.y)
            )
        }
    }
}

internal data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val alpha: Float
)

// ============================================================
// SECTION 1.4 — VICTORY CONFETTI
// ============================================================

/**
 * Full-screen looping confetti overlay with varying gravity and wind
 * speed. Renders rectangular confetti pieces falling from the top with
 * horizontal drift and rotation.
 */
@Composable
fun VictoryConfettiOverlay(
    active: Boolean,
    modifier: Modifier = Modifier
) {
    if (!active) return
    val infinite = rememberInfiniteTransition(label = "confetti")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        ),
        label = "confetti-phase"
    )
    val pieces = remember {
        val rng = Random(42)
        List(80) { i ->
            ConfettiPiece(
                x = rng.nextFloat(),
                startY = -rng.nextFloat() * 0.5f,
                speed = 0.15f + rng.nextFloat() * 0.25f,
                drift = (rng.nextFloat() - 0.5f) * 0.1f,
                size = 6f + rng.nextFloat() * 10f,
                colorIdx = i % 6
            )
        }
    }
    val colors = listOf(PlayerRed, PlayerGreen, PlayerBlue, PlayerYellow, GoldBright, Color.White)
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        for (piece in pieces) {
            val t = (phase * piece.speed + piece.startY) % 1f
            val x = (piece.x + piece.drift * phase) % 1f
            val px = x * w
            val py = t * h
            drawRect(
                color = colors[piece.colorIdx].copy(alpha = 0.9f),
                topLeft = Offset(px, py),
                size = Size(piece.size, piece.size * 0.5f)
            )
        }
    }
}

internal data class ConfettiPiece(
    val x: Float,
    val startY: Float,
    val speed: Float,
    val drift: Float,
    val size: Float,
    val colorIdx: Int
)
