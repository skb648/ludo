// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game.ui.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludolegends.game.ui.theme.GoldBright
import com.ludolegends.game.ui.theme.GoldDeep
import com.ludolegends.game.ui.theme.SapphireBase
import com.ludolegends.game.ui.theme.SapphireGlow
import com.ludolegends.game.ui.theme.SapphireMid
import com.ludolegends.game.ui.theme.SapphireRaised
import com.ludolegends.game.ui.theme.TextPrimary

/**
 * Compact navigation footer — [Menu] [Undo] [Redo] [Settings].
 *
 * === GLITCH FIXES (Veteran pass) ===
 *   • Button height raised from 54dp → 64dp to give text & icon room to breathe.
 *   • Icon size raised from 20dp → 26dp for crisp visibility.
 *   • Label font size raised from 10sp → 12sp, weight raised from W700 → W900 (Black)
 *     so the labels read as Bold per spec.
 *   • Label letter-spacing increased from 0 → 0.8sp for legibility.
 *   • Caller (GameScreen) MUST apply [Modifier.navigationBarsPadding] to the
 *     FooterNav host (or to the entire Column) so the strip sits comfortably
 *     above the device's native navigation-bar pill. This prevents text
 *     clipping and accidental OS home-swipe gestures.
 *   • Added vertical padding inside each button so icons and labels don't
 *     collide with the gold trim border.
 */
@Composable
fun FooterNav(
    onMenu: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FooterButton(label = "Menu",    icon = Icons.Filled.Menu,     onClick = onMenu,     modifier = Modifier.weight(1f))
        FooterButton(label = "Undo",    icon = Icons.Filled.Undo,     onClick = onUndo,     modifier = Modifier.weight(1f))
        FooterButton(label = "Redo",    icon = Icons.Filled.Refresh,  onClick = onRedo,     modifier = Modifier.weight(1f))
        FooterButton(label = "Settings",icon = Icons.Filled.Settings, onClick = onSettings, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun FooterButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()

    Box(
        modifier = modifier
            .height(64.dp)   // was 54dp — raised for breathing room
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .drawWithCache {
                onDrawBehind {
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                SapphireGlow.copy(alpha = if (isPressed) 0.85f else 0.30f),
                                SapphireRaised,
                                SapphireMid,
                                SapphireBase
                            ),
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = size.maxDimension
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                    )
                    drawRoundRect(
                        brush = Brush.linearGradient(listOf(GoldBright, GoldDeep, GoldBright)),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
                        style = Stroke(width = 1.5f)
                    )
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),  // internal padding so content doesn't touch the trim
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = GoldBright,
                modifier = Modifier.size(26.dp)   // was 20dp — raised for crisp visibility
            )
            Text(
                text = label,
                fontSize = 12.sp,                 // was 10sp — raised for legibility
                fontWeight = FontWeight.W900,     // was W700 — raised to Black per spec
                letterSpacing = 0.8.sp,           // new — improves readability
                color = TextPrimary,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
}
