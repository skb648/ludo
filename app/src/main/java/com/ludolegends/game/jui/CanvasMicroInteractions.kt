// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game.jui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import com.ludolegends.game.engine.Player
import com.ludolegends.game.ui.theme.GoldBright
import com.ludolegends.game.ui.theme.PlayerBlue
import com.ludolegends.game.ui.theme.PlayerGreen
import com.ludolegends.game.ui.theme.PlayerRed
import com.ludolegends.game.ui.theme.PlayerYellow

/**
 * === SECTION 2.1 — DOTTED SPINNING SELECTION RING ===
 *
 * When a player's dice value makes a specific token eligible to move,
 * draw a dynamic rotating neon-dotted circle directly underneath
 * that token's base. The rotation angle (0° → 360°) drives the
 * `PathEffect.dashPathEffect(floatArrayOf(10f, 10f), phase)` so the
 * dashes appear to spin around the token.
 *
 * Renders as an overlay on top of the board — does NOT modify the
 * existing token rendering or layout.
 *
 * @param selectableTokens set of (player, tokenId) pairs that should glow.
 * @param cellCenters map from (player, tokenId) → canvas pixel center.
 * @param tokenRadiusPx base token radius in pixels.
 */
@Composable
fun DottedSpinningSelectionRing(
    selectableTokens: Set<Pair<Player, Int>>,
    cellCenters: Map<Pair<Player, Int>, Offset>,
    tokenRadiusPx: Float,
    modifier: Modifier = Modifier
) {
    if (selectableTokens.isEmpty()) return
    val infinite = rememberInfiniteTransition(label = "spin-ring")
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin-angle"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val ringRadius = tokenRadiusPx * 1.45f
        val dashPhase = rotation  // drives the dashPathEffect phase
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), dashPhase)

        for (key in selectableTokens) {
            val center = cellCenters[key] ?: continue
            val player = key.first
            val ringColor = when (player) {
                Player.RED    -> PlayerRed
                Player.GREEN  -> PlayerGreen
                Player.YELLOW -> PlayerYellow
                Player.BLUE   -> PlayerBlue
            }
            // Outer neon dotted ring (rotating dashes)
            drawCircle(
                color = ringColor.copy(alpha = 0.95f),
                radius = ringRadius,
                center = center,
                style = Stroke(width = 4f, pathEffect = dashEffect)
            )
            // Inner gold solid pulse ring (static, gives the glow depth)
            drawCircle(
                color = GoldBright.copy(alpha = 0.45f),
                radius = ringRadius * 0.92f,
                center = center,
                style = Stroke(width = 1.5f)
            )
            // Soft glow halo behind the dashes
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ringColor.copy(alpha = 0.35f),
                        ringColor.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = ringRadius * 1.6f
                ),
                radius = ringRadius * 1.6f,
                center = center
            )
        }
    }
}

/**
 * === SECTION 2.2 — ACTIVE TURN PROFILE PULSE GLOW ===
 *
 * The avatar frame and nameplate container of the active player color
 * turn must display a smooth, repeating breathing glow animation.
 * Cycle the shadow/aura opacity between 0.4f and 1.0f via an
 * infinite transition. Used by [PlayerStatusBar] to wrap the active
 * player's chip.
 *
 * Returns the current glow alpha (0.4f..1.0f) so the caller can apply
 * it as a draw modifier on any composable.
 */
@Composable
fun activeTurnPulseAlpha(): Float {
    val infinite = rememberInfiniteTransition(label = "turn-pulse")
    val alpha by infinite.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse-alpha"
    )
    return alpha
}
