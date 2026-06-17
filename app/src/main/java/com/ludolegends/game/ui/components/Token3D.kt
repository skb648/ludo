package com.ludolegends.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ludolegends.game.engine.Player
import com.ludolegends.game.ui.theme.GoldBright
import com.ludolegends.game.ui.theme.GoldDeep

/**
 * Standalone solid premium 3D pawn composable — used in the Lobby
 * preview row and anywhere else we want a single decorative pawn.
 *
 * Matches the on-board [drawSolidPawn3D] rendering style: solid base
 * disc, gold neck ring, solid domed body, sharp radial highlight on
 * the upper bulb, soft blurred drop-shadow. NO transparent bubble,
 * NO flat-globe wrapper.
 */
@Composable
fun Token3D(
    player: Player,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    highlight: Boolean = false,
    dim: Boolean = false
) {
    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val r = w * 0.32f
            val baseAlpha = if (dim) 0.45f else 1.0f

            // 1. Drop shadow
            val shadowY = h * 0.85f
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.55f * baseAlpha),
                        Color.Black.copy(alpha = 0.0f)
                    ),
                    center = Offset(cx, shadowY),
                    radius = r * 1.6f
                ),
                topLeft = Offset(cx - r * 1.3f, shadowY - r * 0.35f),
                size = Size(r * 2.6f, r * 1.1f)
            )

            // 2. Base disc
            val baseCenterY = h * 0.70f
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        player.light.copy(alpha = baseAlpha),
                        player.primary.copy(alpha = baseAlpha),
                        player.dark.copy(alpha = baseAlpha)
                    ),
                    center = Offset(cx - r * 0.3f, baseCenterY - r * 0.2f),
                    radius = r * 1.3f
                ),
                topLeft = Offset(cx - r, baseCenterY - r * 0.5f),
                size = Size(r * 2f, r * 1.1f)
            )
            drawOval(
                color = Color.Black.copy(alpha = 0.65f * baseAlpha),
                topLeft = Offset(cx - r, baseCenterY - r * 0.5f),
                size = Size(r * 2f, r * 1.1f),
                style = Stroke(width = 1.2f)
            )

            // 3. Gold neck ring
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(
                        GoldBright.copy(alpha = baseAlpha),
                        GoldDeep.copy(alpha = baseAlpha),
                        GoldBright.copy(alpha = baseAlpha)
                    )
                ),
                start = Offset(cx - r * 0.62f, baseCenterY - r * 0.30f),
                end   = Offset(cx + r * 0.62f, baseCenterY - r * 0.30f),
                strokeWidth = 2.2f
            )

            // 4. Solid domed body
            val domeTopY = h * 0.18f
            val domeBottomY = baseCenterY - r * 0.20f
            val domePath = Path().apply {
                moveTo(cx - r * 0.62f, domeBottomY)
                cubicTo(
                    cx - r * 0.62f, (domeBottomY + domeTopY) * 0.5f,
                    cx - r * 0.35f, domeTopY,
                    cx, domeTopY
                )
                cubicTo(
                    cx + r * 0.35f, domeTopY,
                    cx + r * 0.62f, (domeBottomY + domeTopY) * 0.5f,
                    cx + r * 0.62f, domeBottomY
                )
                lineTo(cx - r * 0.62f, domeBottomY)
                close()
            }
            drawPath(
                path = domePath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        player.light.copy(alpha = baseAlpha),
                        player.primary.copy(alpha = baseAlpha),
                        player.dark.copy(alpha = baseAlpha)
                    ),
                    center = Offset(cx - r * 0.25f, domeTopY + (domeBottomY - domeTopY) * 0.30f),
                    radius = r * 1.5f
                )
            )
            drawPath(
                path = domePath,
                color = Color.Black.copy(alpha = 0.55f * baseAlpha),
                style = Stroke(width = 1.0f)
            )

            // 5. Sharp radial highlight on the upper bulb
            val hx = cx - r * 0.22f
            val hy = domeTopY + (domeBottomY - domeTopY) * 0.30f
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f * baseAlpha),
                        Color.White.copy(alpha = 0.55f * baseAlpha),
                        Color.White.copy(alpha = 0.0f)
                    ),
                    center = Offset(hx, hy),
                    radius = r * 0.55f
                ),
                topLeft = Offset(hx - r * 0.35f, hy - r * 0.45f),
                size = Size(r * 0.7f, r * 0.9f)
            )

            // 6. Selection ring (optional)
            if (highlight) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            GoldBright,
                            GoldBright.copy(alpha = 0.10f),
                            GoldBright,
                            GoldBright.copy(alpha = 0.10f)
                        ),
                        center = Offset(cx, h * 0.5f)
                    ),
                    radius = w * 0.48f,
                    center = Offset(cx, h * 0.5f),
                    style = Stroke(width = 3.5f)
                )
            }
        }
    }
}
