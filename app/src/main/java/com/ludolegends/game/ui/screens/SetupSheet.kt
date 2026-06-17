package com.ludolegends.game.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
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
import com.ludolegends.game.engine.GameMode
import com.ludolegends.game.engine.GameModeType
import com.ludolegends.game.ui.theme.ActionGreen
import com.ludolegends.game.ui.theme.ActionGreenBright
import com.ludolegends.game.ui.theme.GoldBright
import com.ludolegends.game.ui.theme.GoldDeep
import com.ludolegends.game.ui.theme.SapphireBase
import com.ludolegends.game.ui.theme.SapphireDeep
import com.ludolegends.game.ui.theme.SapphireMid
import com.ludolegends.game.ui.theme.SapphireRaised
import com.ludolegends.game.ui.theme.TextPrimary
import com.ludolegends.game.ui.theme.TextSecondary

/**
 * === SECTION 3.2 — DISMISSABLE SETUP SHEET NAVIGATION ===
 *
 * A bottom-sheet style modal that slides up from the bottom of the screen
 * when the user taps "Local Pass & Play" or "Play With Friends" on the
 * Lobby. Clicking the glowing green "START MATCH" button dismisses the
 * sheet and starts the game loop.
 *
 * The sheet exposes:
 *   • Mode Type selector — LOCAL_PASS_PLAY vs PLAY_WITH_FRIENDS
 *     (pre-selected based on which Lobby card the user tapped)
 *   • Game Mode selector — Standard Ludo vs 2v2 Team Mode
 *   • Player Count selector — 2 / 3 / 4 players
 *   • START MATCH button
 */
@Composable
fun SetupSheet(
    visible: Boolean,
    playerCount: Int,
    gameMode: GameMode,
    modeType: GameModeType,
    onPlayerCountChange: (Int) -> Unit,
    onGameModeChange: (GameMode) -> Unit,
    onGameModeTypeChange: (GameModeType) -> Unit,
    onStartMatch: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                        text = "NEW GAME SETUP",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.W900,
                        color = GoldBright,
                        letterSpacing = 2.sp
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .width(80.dp)
                            .height(2.dp)
                            .background(GoldDeep)
                    )

                    Spacer(Modifier.height(20.dp))

                    // === Mode Type selector (LOCAL_PASS_PLAY vs PLAY_WITH_FRIENDS) ===
                    Text(
                        text = "Game Mode",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.W600,
                        color = TextSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ModeTypeChip(
                            label = GameModeType.LOCAL_PASS_PLAY.display,
                            selected = modeType == GameModeType.LOCAL_PASS_PLAY,
                            onClick = { onGameModeTypeChange(GameModeType.LOCAL_PASS_PLAY) },
                            modifier = Modifier.weight(1f)
                        )
                        ModeTypeChip(
                            label = GameModeType.PLAY_WITH_FRIENDS.display,
                            selected = modeType == GameModeType.PLAY_WITH_FRIENDS,
                            onClick = { onGameModeTypeChange(GameModeType.PLAY_WITH_FRIENDS) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // === Game Mode (Standard vs Team) ===
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ModeChip(
                            label = "STANDARD LUDO",
                            selected = gameMode == GameMode.STANDARD,
                            onClick = { onGameModeChange(GameMode.STANDARD) },
                            modifier = Modifier.weight(1f)
                        )
                        ModeChip(
                            label = "2v2 TEAM MODE",
                            selected = gameMode == GameMode.TEAM_2V2,
                            onClick = { onGameModeChange(GameMode.TEAM_2V2) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "Number of Players",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.W600,
                        color = TextSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (count in 2..4) {
                            PlayerCountChip(
                                count = count,
                                selected = playerCount == count,
                                onClick = { onPlayerCountChange(count) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    StartMatchButton(onClick = onStartMatch)

                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = when (modeType) {
                            GameModeType.LOCAL_PASS_PLAY ->
                                "Animated digital 3D dice • Pass the device between turns"
                            GameModeType.PLAY_WITH_FRIENDS ->
                                "Roll a real wooden dice • Tap the matching number"
                        },
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
private fun ModeTypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    Box(
        modifier = modifier
            .height(54.dp)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .drawWithCache {
                onDrawBehind {
                    drawRoundRect(
                        color = if (selected) GoldBright else SapphireRaised,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                    )
                    drawRoundRect(
                        brush = Brush.linearGradient(listOf(GoldBright, GoldDeep, GoldBright)),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
                        style = Stroke(width = if (selected) 2f else 1f)
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.W800,
            color = if (selected) SapphireDeep else TextPrimary,
            letterSpacing = 0.4.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
    }
}

@Composable
private fun ModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    Box(
        modifier = modifier
            .height(44.dp)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .drawWithCache {
                onDrawBehind {
                    drawRoundRect(
                        color = if (selected) GoldBright else SapphireRaised,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                    )
                    drawRoundRect(
                        brush = Brush.linearGradient(listOf(GoldBright, GoldDeep, GoldBright)),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
                        style = Stroke(width = if (selected) 2f else 1f)
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.W800,
            color = if (selected) SapphireDeep else TextPrimary,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun PlayerCountChip(
    count: Int,
    selected: Boolean,
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
                onClick = onClick
            )
            .drawWithCache {
                onDrawBehind {
                    drawRoundRect(
                        color = if (selected) GoldBright else SapphireRaised,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(36f, 36f)
                    )
                    drawRoundRect(
                        brush = Brush.linearGradient(listOf(GoldBright, GoldDeep, GoldBright)),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(36f, 36f),
                        style = Stroke(width = if (selected) 2.5f else 1.2f)
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = count.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.W900,
                color = if (selected) SapphireDeep else TextPrimary
            )
            Text(
                text = "Players",
                fontSize = 9.sp,
                fontWeight = FontWeight.W500,
                color = if (selected) SapphireDeep else TextSecondary
            )
        }
    }
}

@Composable
private fun StartMatchButton(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .drawWithCache {
                onDrawBehind {
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = if (isPressed)
                                listOf(ActionGreen, ActionGreenBright)
                            else
                                listOf(ActionGreenBright, ActionGreen),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f)
                    )
                    drawRoundRect(
                        brush = Brush.linearGradient(listOf(GoldBright, GoldDeep, GoldBright)),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f),
                        style = Stroke(width = 2f)
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "START MATCH",
                fontSize = 18.sp,
                fontWeight = FontWeight.W900,
                color = SapphireDeep,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.width(10.dp))
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = "Start",
                tint = SapphireDeep,
                modifier = Modifier.padding(0.dp)
            )
        }
    }
}
