// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ludolegends.game.ui.theme.GoldBright
import com.ludolegends.game.ui.theme.GoldDeep
import com.ludolegends.game.ui.theme.SapphireBase
import com.ludolegends.game.ui.theme.SapphireDeep
import com.ludolegends.game.ui.theme.SapphireGlow
import com.ludolegends.game.ui.theme.SapphireMid
import com.ludolegends.game.ui.theme.SapphireRaised
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * === MODE 1 — Animated Digital 3D Rolling Dice ===
 *
 * A premium square dice element that triggers a crisp multi-frame
 * tumbling/rolling animation upon tap. Driven by secure mathematical
 * randomness (`Random.nextInt(1, 7)`) with absolute equal probability
 * distribution — zero bias, zero rigging.
 *
 * Animation pipeline:
 *   1. User taps the dice.
 *   2. `onRollStarted()` fires (lets the ViewModel play the dice-roll sound).
 *   3. Tumble animation: 8 random face flips at 60ms each.
 *   4. Final face revealed via `onRollComplete(value)`.
 *   5. Dice disabled until the engine returns to AWAITING_DICE phase.
 *
 * The dice draws a real 3D-looking square on Canvas with:
 *   • A radial gradient face (sapphire glow)
 *   • A double-layered gold trim border (matches the rest of the UI)
 *   • Crisp white pips arranged per face value
 *   • A subtle 3D rotation skew during the tumble
 */
@Composable
fun AnimatedDice3D(
    enabled: Boolean,
    onRollStarted: () -> Unit,
    onRollComplete: (Int) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp
) {
    val scope = rememberCoroutineScope()
    val interaction = remember { MutableInteractionSource() }

    // The currently-displayed face value (1..6). Animates during the tumble.
    var displayedFace by remember { mutableStateOf(1) }
    // Tumble progress (0..1) — drives a rotation/scale skew for visual flair.
    val tumbleAnim = remember { Animatable(0f) }
    var isRolling by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled && !isRolling,
                onClick = {
                    if (isRolling) return@clickable
                    isRolling = true
                    onRollStarted()
                    scope.launch {
                        // === Tumble animation: 8 random face flips at 60ms each ===
                        val flips = 8
                        for (i in 0 until flips) {
                            displayedFace = Random.nextInt(1, 7)
                            tumbleAnim.snapTo(i.toFloat() / flips)
                            delay(60)
                        }
                        // === Final value via secure random ===
                        val finalValue = Random.nextInt(1, 7)
                        displayedFace = finalValue
                        tumbleAnim.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(180, easing = FastOutSlowInEasing)
                        )
                        // Brief settle delay so the player sees the final face
                        delay(120)
                        isRolling = false
                        onRollComplete(finalValue)
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height
            val inset = w * 0.06f
            val diceSize = Size(w - inset * 2, h - inset * 2)
            val topLeft = Offset(inset, inset)

            // Tumble skew — a small rotation-like scale oscillation
            val skew = if (isRolling) {
                val t = tumbleAnim.value
                val wobble = (kotlin.math.sin(t.toDouble() * kotlin.math.PI * 4.0) * 0.08).toFloat()
                1f - wobble
            } else 1f

            // Background glow halo (pulsing while rolling)
            if (isRolling) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(SapphireGlow.copy(alpha = 0.55f), Color.Transparent),
                        center = Offset(w / 2f, h / 2f),
                        radius = w * 0.6f
                    ),
                    radius = w * 0.6f,
                    center = Offset(w / 2f, h / 2f)
                )
            }

            // Dice body — dark sapphire with gold trim
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        SapphireRaised,
                        SapphireMid,
                        SapphireBase,
                        SapphireDeep
                    ),
                    center = Offset(w / 2f - inset, h / 2f - inset),
                    radius = diceSize.maxDimension
                ),
                topLeft = topLeft,
                size = diceSize,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
            )
            // Outer gold trim
            drawRoundRect(
                brush = Brush.linearGradient(listOf(GoldBright, GoldDeep, GoldBright)),
                topLeft = topLeft,
                size = diceSize,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
                style = Stroke(width = 2.5f)
            )
            // Inner thin trim
            drawRoundRect(
                color = GoldDeep.copy(alpha = 0.7f),
                topLeft = Offset(topLeft.x + 3f, topLeft.y + 3f),
                size = Size(diceSize.width - 6f, diceSize.height - 6f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
                style = Stroke(width = 0.75f)
            )

            // Pips for the current displayed face
            drawDicePips(
                face = displayedFace,
                center = Offset(w / 2f, h / 2f),
                size = diceSize,
                scale = skew
            )

            // Disabled overlay
            if (!enabled && !isRolling) {
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.40f),
                    topLeft = topLeft,
                    size = diceSize,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                )
            }
        }
    }
}

private fun DrawScope.drawDicePips(
    face: Int,
    center: Offset,
    size: Size,
    scale: Float
) {
    val pipR = size.minDimension * 0.075f * scale
    val dx = size.width * 0.22f
    val dy = size.height * 0.22f
    val cx = center.x
    val cy = center.y

    val positions = when (face) {
        1    -> listOf(Offset(cx, cy))
        2    -> listOf(Offset(cx - dx, cy - dy), Offset(cx + dx, cy + dy))
        3    -> listOf(Offset(cx - dx, cy - dy), Offset(cx, cy), Offset(cx + dx, cy + dy))
        4    -> listOf(Offset(cx - dx, cy - dy), Offset(cx + dx, cy - dy),
                       Offset(cx - dx, cy + dy), Offset(cx + dx, cy + dy))
        5    -> listOf(Offset(cx - dx, cy - dy), Offset(cx + dx, cy - dy),
                       Offset(cx, cy),
                       Offset(cx - dx, cy + dy), Offset(cx + dx, cy + dy))
        6    -> listOf(Offset(cx - dx, cy - dy), Offset(cx + dx, cy - dy),
                       Offset(cx - dx, cy),     Offset(cx + dx, cy),
                       Offset(cx - dx, cy + dy), Offset(cx + dx, cy + dy))
        else -> emptyList()
    }
    for (p in positions) {
        // Pip shadow
        drawCircle(
            color = Color.Black.copy(alpha = 0.5f),
            radius = pipR * 1.1f,
            center = Offset(p.x + 1.5f, p.y + 1.5f)
        )
        // Pip body — bright white with a subtle gradient
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, Color.White.copy(alpha = 0.85f)),
                center = Offset(p.x - pipR * 0.3f, p.y - pipR * 0.3f),
                radius = pipR * 1.3f
            ),
            radius = pipR,
            center = p
        )
    }
}
