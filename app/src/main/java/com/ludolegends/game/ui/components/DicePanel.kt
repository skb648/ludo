// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludolegends.game.ui.theme.ActionGreen
import com.ludolegends.game.ui.theme.ActionGreenBright
import com.ludolegends.game.ui.theme.DiceGlowGradient
import com.ludolegends.game.ui.theme.GoldBright
import com.ludolegends.game.ui.theme.GoldDeep
import com.ludolegends.game.ui.theme.SapphireBase
import com.ludolegends.game.ui.theme.SapphireDeep
import com.ludolegends.game.ui.theme.SapphireGlow
import com.ludolegends.game.ui.theme.SapphireMid
import com.ludolegends.game.ui.theme.SapphireRaised
import com.ludolegends.game.ui.theme.TextPrimary

/**
 * === SECTION 1.3 — HUD CONTROL INPUT PANEL ALIGNMENT ===
 *
 * Six manual dice buttons rendered as clean glowing dark rectangles with
 * bright white borders and crisp pip arrangements underneath the number.
 * The buttons are arranged horizontally and stretch to fill the panel width.
 *
 * Each button:
 *  • Dark sapphire gradient background with a soft blue radial glow when pressed
 *  • Razor-sharp gold trim border (matches [GoldTrimBorder])
 *  • Pip arrangement matching real dice (1..6) at the top
 *  • Bold white numeric label at the bottom
 */
@Composable
fun DicePanel(
    enabled: Boolean,
    onDiceTapped: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (value in 1..6) {
            DiceButton(
                value = value,
                enabled = enabled,
                onClick = { onDiceTapped(value) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun DiceButton(
    value: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()

    Box(
        modifier = modifier
            .height(64.dp)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .drawWithCache {
                val glowAlpha = if (isPressed && enabled) 0.85f else if (enabled) 0.45f else 0.15f
                onDrawBehind {
                    // Background fill — dark sapphire with subtle glow
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                SapphireGlow.copy(alpha = glowAlpha),
                                SapphireRaised,
                                SapphireMid,
                                SapphireBase
                            ),
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = size.maxDimension
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                    )
                    // Outer gold trim
                    drawRoundRect(
                        brush = Brush.linearGradient(listOf(GoldBright, GoldDeep, GoldBright)),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
                        style = Stroke(width = 2f)
                    )
                    // Inner white border (bright)
                    drawRoundRect(
                        color = Color.White.copy(alpha = if (enabled) 0.85f else 0.30f),
                        topLeft = Offset(3f, 3f),
                        size = Size(size.width - 6f, size.height - 6f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                        style = Stroke(width = 1f)
                    )
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Pip arrangement at the top
            DicePips(
                value = value,
                modifier = Modifier.size(28.dp),
                tint = if (enabled) GoldBright else GoldBright.copy(alpha = 0.35f)
            )
            // Number label at the bottom
            Text(
                text = value.toString(),
                fontSize = 13.sp,
                fontWeight = FontWeight.W900,
                letterSpacing = 0.5.sp,
                color = if (enabled) TextPrimary else TextPrimary.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DicePips(
    value: Int,
    modifier: Modifier = Modifier,
    tint: Color = GoldBright
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val pipR = minOf(w, h) * 0.10f
        val cx = w / 2f
        val cy = h / 2f
        val dx = w * 0.25f
        val dy = h * 0.25f

        val positions = when (value) {
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
            drawCircle(tint, radius = pipR, center = p)
        }
    }
}
