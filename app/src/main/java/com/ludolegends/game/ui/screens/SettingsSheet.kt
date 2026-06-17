package com.ludolegends.game.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludolegends.game.ui.theme.ActionGreen
import com.ludolegends.game.ui.theme.ActionGreenBright
import com.ludolegends.game.ui.theme.GoldBright
import com.ludolegends.game.ui.theme.GoldDeep
import com.ludolegends.game.ui.theme.SapphireDeep
import com.ludolegends.game.ui.theme.SapphireMid
import com.ludolegends.game.ui.theme.SapphireRaised
import com.ludolegends.game.ui.theme.TextPrimary
import com.ludolegends.game.ui.theme.TextSecondary
import com.ludolegends.game.viewmodel.LudoViewModel
import kotlin.math.roundToInt

/**
 * === SECTION 2 — REVISED AUDIO ARCHITECTURE & MASTER SETTINGS ===
 *
 * 100% functional settings panel — NO DUMMIES.
 *
 * Sliders:
 *   • Slider 1 — Master BGM Volume (controls ExoPlayer BGM stream smoothly).
 *   • Slider 2 — SFX Volume (controls SoundPool playback levels).
 *
 * Toggle:
 *   • Haptic Feedback Engine on/off (system vibrator loop).
 *
 * All values are persisted via [WalletRepository] (DataStore) so they
 * survive app restarts.
 */
@Composable
fun SettingsSheet(
    visible: Boolean,
    viewModel: LudoViewModel,
    onDismiss: () -> Unit,
    onReturnToMenu: () -> Unit,
    onRestartMatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .drawWithCache {
                        onDrawBehind {
                            drawRoundRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(SapphireRaised, SapphireMid, SapphireDeep),
                                    center = Offset(size.width / 2f, 0f),
                                    radius = size.maxDimension
                                ),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
                            )
                            drawRoundRect(
                                brush = Brush.linearGradient(listOf(GoldBright, GoldDeep, GoldBright)),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f),
                                style = Stroke(width = 2.5f)
                            )
                            drawRoundRect(
                                color = GoldDeep,
                                topLeft = Offset(3f, 3f),
                                size = Size(size.width - 6f, size.height - 6f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f),
                                style = Stroke(width = 0.75f)
                            )
                        }
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(48.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(GoldBright.copy(alpha = 0.7f))
                    )
                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "SETTINGS",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.W900,
                        color = GoldBright,
                        letterSpacing = 3.sp
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .width(80.dp)
                            .height(2.dp)
                            .background(GoldDeep)
                    )

                    Spacer(Modifier.height(20.dp))

                    // === Slider 1 — Master BGM Volume ===
                    SettingsSliderRow(
                        icon = Icons.Filled.MusicNote,
                        label = "BGM Volume",
                        value = settings.bgmVolume,
                        onValueChange = viewModel::setBgmVolume
                    )

                    Spacer(Modifier.height(16.dp))

                    // === Slider 2 — SFX Volume ===
                    SettingsSliderRow(
                        icon = Icons.Filled.VolumeUp,
                        label = "SFX Volume",
                        value = settings.sfxVolume,
                        onValueChange = viewModel::setSfxVolume
                    )

                    Spacer(Modifier.height(16.dp))

                    // === Haptic toggle ===
                    SettingsToggleRow(
                        icon = Icons.Filled.Vibration,
                        label = "Haptic Feedback",
                        subtitle = "Vibrate on dice tap and token landing",
                        enabled = settings.hapticEnabled,
                        onToggle = viewModel::setHapticEnabled
                    )

                    Spacer(Modifier.height(24.dp))

                    SettingsActionRow(
                        icon = Icons.Filled.Refresh,
                        label = "Restart Match",
                        subtitle = "Discard current progress and start over",
                        onClick = onRestartMatch
                    )
                    Spacer(Modifier.height(12.dp))
                    SettingsActionRow(
                        icon = Icons.Filled.Home,
                        label = "Return to Main Menu",
                        subtitle = "Exit to the lobby screen",
                        onClick = onReturnToMenu
                    )

                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "Tap outside to dismiss",
                        fontSize = 11.sp,
                        color = TextSecondary.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSliderRow(
    icon: ImageVector,
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawWithCache {
                onDrawBehind {
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(SapphireRaised, SapphireMid, SapphireDeep),
                            center = Offset(0f, 0f),
                            radius = size.maxDimension
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f)
                    )
                    drawRoundRect(
                        brush = Brush.linearGradient(listOf(GoldBright, GoldDeep, GoldBright)),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f),
                        style = Stroke(width = 1.5f)
                    )
                }
            }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.radialGradient(
                                colors = listOf(ActionGreenBright, ActionGreen),
                                center = Offset(12f, 12f),
                                radius = 40f
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(8.dp)
                ) {
                    Icon(imageVector = icon, contentDescription = label, tint = SapphireDeep)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W700,
                    color = TextPrimary
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${(value * 100).roundToInt()}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W800,
                    color = GoldBright
                )
            }
            Spacer(Modifier.height(10.dp))
            CustomSlider(
                value = value,
                onValueChange = onValueChange
            )
        }
    }
}

/**
 * Custom slider — drawn from scratch on Canvas with gold trim.
 * No Material Slider dependency, fully owned look-and-feel.
 */
@Composable
private fun CustomSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        val w = size.width.toFloat()
                        if (w > 0f) onValueChange((offset.x / w).coerceIn(0f, 1f))
                    },
                    onHorizontalDrag = { change, _: Float ->
                        val w = size.width.toFloat()
                        if (w > 0f) {
                            val newX = change.position.x
                            onValueChange((newX / w).coerceIn(0f, 1f))
                        }
                        change.consume()
                    }
                )
            }
            .drawWithCache {
                val trackHeight = 8.dp.toPx()
                val centerY = (size.height - trackHeight) / 2f
                val thumbR = 10.dp.toPx()
                onDrawBehind {
                    // Track background
                    drawRoundRect(
                        color = SapphireDeep,
                        topLeft = Offset(0f, centerY),
                        size = Size(size.width, trackHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2f, trackHeight / 2f)
                    )
                    // Filled portion
                    val fillWidth = size.width * value.coerceIn(0f, 1f)
                    drawRoundRect(
                        brush = Brush.horizontalGradient(listOf(GoldBright, GoldDeep)),
                        topLeft = Offset(0f, centerY),
                        size = Size(fillWidth, trackHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2f, trackHeight / 2f)
                    )
                    // Thumb
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(GoldBright, GoldDeep),
                            center = Offset(fillWidth, centerY + trackHeight / 2f),
                            radius = thumbR * 1.4f
                        ),
                        radius = thumbR,
                        center = Offset(fillWidth, centerY + trackHeight / 2f)
                    )
                    drawCircle(
                        color = SapphireDeep,
                        radius = thumbR,
                        center = Offset(fillWidth, centerY + trackHeight / 2f),
                        style = Stroke(width = 1.5f)
                    )
                }
            }
    )
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = { onToggle(!enabled) }
            )
            .drawWithCache {
                onDrawBehind {
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(SapphireRaised, SapphireMid, SapphireDeep),
                            center = Offset(0f, 0f),
                            radius = size.maxDimension
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f)
                    )
                    drawRoundRect(
                        brush = Brush.linearGradient(listOf(GoldBright, GoldDeep, GoldBright)),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f),
                        style = Stroke(width = 1.5f)
                    )
                }
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.radialGradient(
                            colors = listOf(ActionGreenBright, ActionGreen),
                            center = Offset(12f, 12f),
                            radius = 40f
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(8.dp)
            ) {
                Icon(imageVector = icon, contentDescription = label, tint = SapphireDeep)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W700,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            // Toggle indicator
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (enabled) ActionGreen else SapphireDeep
                    ),
                contentAlignment = if (enabled) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .width(20.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoldBright)
                )
            }
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
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
                                if (isPressed) SapphireRaised else SapphireMid,
                                SapphireMid,
                                SapphireDeep
                            ),
                            center = Offset(0f, 0f),
                            radius = size.maxDimension
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f)
                    )
                    drawRoundRect(
                        brush = Brush.linearGradient(listOf(GoldBright, GoldDeep, GoldBright)),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f),
                        style = Stroke(width = 2f)
                    )
                }
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.radialGradient(
                            colors = listOf(ActionGreenBright, ActionGreen),
                            center = Offset(12f, 12f),
                            radius = 40f
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = SapphireDeep,
                    modifier = Modifier.padding(2.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W800,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
