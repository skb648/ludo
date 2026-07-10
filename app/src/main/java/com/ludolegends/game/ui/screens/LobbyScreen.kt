// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludolegends.game.engine.Player
import com.ludolegends.game.ui.components.GoldTrimBorder
import com.ludolegends.game.ui.components.Token3D
import com.ludolegends.game.ui.theme.ActionGreen
import com.ludolegends.game.ui.theme.ActionGreenBright
import com.ludolegends.game.ui.theme.GoldBright
import com.ludolegends.game.ui.theme.GoldDeep
import com.ludolegends.game.ui.theme.PlayerBlue
import com.ludolegends.game.ui.theme.PlayerGreen
import com.ludolegends.game.ui.theme.PlayerRed
import com.ludolegends.game.ui.theme.PlayerYellow
import com.ludolegends.game.ui.theme.SapphireBase
import com.ludolegends.game.ui.theme.SapphireDeep
import com.ludolegends.game.ui.theme.SapphireMid
import com.ludolegends.game.ui.theme.SapphireRaised
import com.ludolegends.game.ui.theme.TextPrimary
import com.ludolegends.game.ui.theme.TextSecondary

/**
 * Lobby screen — the main menu shown on app launch.
 *
 * Layout (matches reference):
 *   ┌──────────────────────────────────┐
 *   │     ◆ LUDO LEGENDS ◆            │  ← Title block
 *   │     Roll. Strategize. Conquer.  │
 *   │                                  │
 *   │  ┌──┐  ┌──┐  ┌──┐  ┌──┐         │  ← 4 floating tokens preview
 *   │  │🔴│  │🟢│  │🔵│  │🟡│         │
 *   │  └──┘  └──┘  └──┘  └──┘         │
 *   │                                  │
 *   │  ┌──────────────────────────┐   │
 *   │  │ 🎲  LOCAL PASS & PLAY    │   │  ← Local Play card (opens SetupSheet)
 *   │  │     Play with friends... │   │
 *   │  └──────────────────────────┘   │
 *   │  ┌──────────────────────────┐   │
 *   │  │ 👥  PLAY WITH FRIENDS    │   │  ← Friends card
 *   │  └──────────────────────────┘   │
 *   │  ┌──────────────────────────┐   │
 *   │  │ 🌐  ONLINE MULTIPLAYER   │   │  ← Online card
 *   │  └──────────────────────────┘   │
 *   └──────────────────────────────────┘
 */
@Composable
fun LobbyScreen(
    onLocalPlay: () -> Unit,
    onFriends: () -> Unit,
    onOnline: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        // === Title block ===
        Text(
            text = "LUDO LEGENDS",
            fontSize = 36.sp,
            fontWeight = FontWeight.W900,
            color = GoldBright,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Roll. Strategize. Conquer.",
            fontSize = 13.sp,
            fontWeight = FontWeight.W500,
            color = TextSecondary,
            letterSpacing = 4.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(Modifier.height(28.dp))

        // === Floating token preview ===
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            for (player in Player.values()) {
                Token3D(player = player, size = 44.dp)
            }
        }

        Spacer(Modifier.height(28.dp))

        // === Menu cards ===
        LobbyCard(
            icon = Icons.Filled.SportsEsports,
            title = "Local Pass & Play",
            subtitle = "Animated digital 3D dice • Pass the device between turns",
            accent = PlayerRed,
            onClick = onLocalPlay
        )
        Spacer(Modifier.height(12.dp))
        LobbyCard(
            icon = Icons.Filled.Group,
            title = "Play With Friends",
            subtitle = "Roll a real wooden dice • Tap the matching number",
            accent = PlayerGreen,
            onClick = onFriends
        )
        Spacer(Modifier.height(12.dp))
        LobbyCard(
            icon = Icons.Filled.Language,
            title = "Online Multiplayer",
            subtitle = "Compete with players worldwide",
            accent = PlayerBlue,
            onClick = onOnline
        )

        Spacer(Modifier.height(40.dp))
        Text(
            text = "v1.0.0  •  Premium Edition",
            fontSize = 10.sp,
            color = TextSecondary.copy(alpha = 0.6f),
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun LobbyCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
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
                    // Double gold trim
                    drawRoundRect(
                        brush = Brush.linearGradient(listOf(GoldBright, GoldDeep, GoldBright)),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f),
                        style = Stroke(width = 2.5f)
                    )
                    drawRoundRect(
                        color = GoldDeep,
                        topLeft = Offset(3f, 3f),
                        size = Size(size.width - 6f, size.height - 6f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
                        style = Stroke(width = 0.75f)
                    )
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(accent, accent.copy(alpha = 0.6f)),
                            center = Offset(12f, 12f),
                            radius = 40f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = GoldBright,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W800,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W400,
                    color = TextSecondary
                )
            }
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Open",
                tint = GoldBright,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
