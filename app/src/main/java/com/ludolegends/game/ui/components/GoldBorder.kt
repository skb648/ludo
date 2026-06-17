package com.ludolegends.game.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ludolegends.game.ui.theme.GoldBright
import com.ludolegends.game.ui.theme.GoldDark
import com.ludolegends.game.ui.theme.GoldDeep
import com.ludolegends.game.ui.theme.GoldTrimGradient

/**
 * Premium gold trim border — razor-sharp double-layered outline used on
 * every primary layout card, selection tile, and board profile frame.
 *
 * The outer stroke is a linear gold gradient (#FFD700 → #D4AF37 → #FFD700)
 * at 2.5dp width; the inner stroke is a darker 0.75dp solid line offset
 * inward by 1.5dp, producing the beveled "double trim" effect seen in
 * the reference design.
 */
@Composable
fun GoldTrimBorder(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    outerWidth: Dp = 2.5.dp,
    innerWidth: Dp = 0.75.dp,
    innerInset: Dp = 1.5.dp,
    innerColor: Color = GoldDark,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .drawWithCache {
                val outerBrush = GoldTrimGradient
                val cornerPx = cornerRadius.toPx()
                val outerW = outerWidth.toPx()
                val innerW = innerWidth.toPx()
                val innerInsetPx = innerInset.toPx()
                onDrawWithContent {
                    drawContent()
                    // Outer trim
                    drawRoundRect(
                        brush = outerBrush,
                        topLeft = Offset(outerW / 2f, outerW / 2f),
                        size = Size(size.width - outerW, size.height - outerW),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerPx, cornerPx),
                        style = Stroke(width = outerW)
                    )
                    // Inner trim — darker, inset
                    drawRoundRect(
                        color = innerColor,
                        topLeft = Offset(outerW + innerInsetPx, outerW + innerInsetPx),
                        size = Size(
                            size.width - 2 * (outerW + innerInsetPx),
                            size.height - 2 * (outerW + innerInsetPx)
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                            (cornerPx - outerW - innerInsetPx).coerceAtLeast(0f),
                            (cornerPx - outerW - innerInsetPx).coerceAtLeast(0f)
                        ),
                        style = Stroke(width = innerW)
                    )
                }
            }
    ) { content() }
}

/**
 * Simpler Compose-only variant for components that don't need the
 * double-stroke canvas trick — uses two stacked borders.
 */
@Composable
fun GoldTrimSimple(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .border(
                BorderStroke(2.5.dp, Brush.linearGradient(listOf(GoldBright, GoldDeep, GoldBright))),
                RoundedCornerShape(cornerRadius)
            )
            .border(
                BorderStroke(0.75.dp, GoldDark),
                RoundedCornerShape(cornerRadius - 1.5.dp)
            )
    ) { content() }
}
