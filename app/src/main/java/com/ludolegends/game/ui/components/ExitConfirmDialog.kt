package com.ludolegends.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.ludolegends.game.ui.theme.GoldBright
import com.ludolegends.game.ui.theme.GoldDeep
import com.ludolegends.game.ui.theme.PlayerRed
import com.ludolegends.game.ui.theme.SapphireDeep
import com.ludolegends.game.ui.theme.SapphireMid
import com.ludolegends.game.ui.theme.SapphireRaised
import com.ludolegends.game.ui.theme.TextPrimary
import com.ludolegends.game.ui.theme.TextSecondary

/**
 * === SECTION 1 — ACCIDENTAL EXIT PREVENTION DIALOG ===
 *
 * A beautifully integrated Jetpack Compose overlay modal that intercepts
 * the [Menu] button (and system back) tap. Displays:
 *   "Exit Match? Your ongoing match progress will be fully saved automatically."
 *
 * Buttons:
 *   • [YES, EXIT]  — confirms the exit; the caller performs the navigation.
 *   • [NO, CONTINUE] — dismisses the dialog; gameplay continues.
 *
 * The dialog auto-saves before showing — the caller has already triggered
 * the auto-save via the ViewModel, so by the time this dialog appears the
 * match state is persisted and the user can safely exit.
 */
@Composable
fun ExitConfirmDialog(
    visible: Boolean,
    onConfirmExit: () -> Unit,
    onContinue: () -> Unit
) {
    if (!visible) return
    androidx.compose.ui.window.Dialog(onDismissRequest = onContinue) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(18.dp))
                .drawWithCache {
                    onDrawBehind {
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(SapphireRaised, SapphireMid, SapphireDeep),
                                center = Offset(size.width / 2f, size.height / 2f),
                                radius = size.maxDimension
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
                        )
                        drawRoundRect(
                            brush = Brush.linearGradient(listOf(GoldBright, GoldDeep, GoldBright)),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f),
                            style = Stroke(width = 2.5f)
                        )
                        drawRoundRect(
                            color = GoldDeep,
                            topLeft = Offset(3f, 3f),
                            size = Size(size.width - 6f, size.height - 6f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
                            style = Stroke(width = 0.75f)
                        )
                    }
                }
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "EXIT MATCH?",
                fontSize = 22.sp,
                fontWeight = FontWeight.W900,
                color = GoldBright,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(2.dp)
                    .background(GoldDeep)
            )
            Spacer(Modifier.height(16.dp))

            // Body
            Text(
                text = "Your ongoing match progress will be fully saved automatically.",
                fontSize = 13.sp,
                fontWeight = FontWeight.W500,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // [NO, CONTINUE]
                DialogActionButton(
                    label = "NO, CONTINUE",
                    accent = ActionGreen,
                    accentBright = ActionGreenBright,
                    modifier = Modifier.weight(1f),
                    onClick = onContinue
                )
                // [YES, EXIT]
                DialogActionButton(
                    label = "YES, EXIT",
                    accent = PlayerRed,
                    accentBright = PlayerRed.copy(alpha = 0.85f),
                    modifier = Modifier.weight(1f),
                    onClick = onConfirmExit
                )
            }
        }
    }
}

@Composable
private fun DialogActionButton(
    label: String,
    accent: Color,
    accentBright: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    Row(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .drawWithCache {
                onDrawBehind {
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = if (isPressed) listOf(accent, accentBright)
                                     else listOf(accentBright, accent),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                    )
                    drawRoundRect(
                        brush = Brush.linearGradient(listOf(GoldBright, GoldDeep, GoldBright)),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
                        style = Stroke(width = 1.5f)
                    )
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.W900,
            color = SapphireDeep,
            letterSpacing = 1.sp
        )
    }
}
