package com.ludolegends.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.ludolegends.game.ui.theme.GoldBright

/**
 * The star-marked safe-zone indicator drawn on the 4 star cells of the
 * Ludo ring. A glossy 5-pointed star with a gold fill and dark outline.
 */
@Composable
fun SafeStar(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 22.dp,
    fillColor: Color = GoldBright
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val outerR = minOf(w, h) / 2f * 0.92f
        val innerR = outerR * 0.42f

        val star = Path().apply {
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) outerR else innerR
                val angle = Math.PI / 2 + i * Math.PI / 5
                val x = cx + (r * Math.cos(angle)).toFloat()
                val y = cy - (r * Math.sin(angle)).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        drawPath(star, fillColor)
        drawPath(star, Color.Black.copy(alpha = 0.6f), style = Stroke(width = 1.2f))
    }
}
