package com.ludolegends.game.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludolegends.game.engine.LudoGameState
import com.ludolegends.game.engine.Player
import com.ludolegends.game.jui.activeTurnPulseAlpha
import com.ludolegends.game.ui.theme.GoldBright
import com.ludolegends.game.ui.theme.GoldDeep
import com.ludolegends.game.ui.theme.SapphireDeep
import com.ludolegends.game.ui.theme.SapphireMid
import com.ludolegends.game.ui.theme.SapphireRaised
import com.ludolegends.game.ui.theme.TextPrimary
import com.ludolegends.game.ui.theme.TextSecondary

/**
 * Top status bar showing all players with their token counts and live scores.
 *
 * === SECTION 2.2 — ACTIVE TURN PROFILE PULSE GLOW ===
 * The active player's chip wraps in a breathing-glow aura whose opacity
 * cycles between 0.4f and 1.0f via [activeTurnPulseAlpha].
 *
 * === SECTION 4 — REAL SCORE MATRIX ===
 * Each chip displays the live score (computed by [ScoreCalculator]) with
 * an animated ticker that slides vertically when the number changes.
 *
 * Layout (matches reference):
 *  ┌──────────────────────────────────────────────────┐
 *  │  [🔴 Red] 3🏠 250pts   [🟢 Green] 1🏠 80pts  ...   │
 *  └──────────────────────────────────────────────────┘
 */
@Composable
fun PlayerStatusBar(
    state: LudoGameState,
    scoreMatrix: Map<Player, Int> = emptyMap(),
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (player in state.turnOrder) {
            val isActive = player == state.currentPlayer && state.winner == null
            PlayerStatusChip(
                player = player,
                homeCount = state.homeCount(player),
                onBaseCount = state.tokens[player]?.count { it.isAtBase } ?: 0,
                isActive = isActive,
                isWinner = state.winner == player,
                score = scoreMatrix[player] ?: 0,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PlayerStatusChip(
    player: Player,
    homeCount: Int,
    onBaseCount: Int,
    isActive: Boolean,
    isWinner: Boolean,
    score: Int,
    modifier: Modifier = Modifier
) {
    // === SECTION 2.2 — Breathing glow alpha (0.4f ↔ 1.0f) ===
    val pulseAlpha = if (isActive) activeTurnPulseAlpha() else 0f

    Box(
        modifier = modifier
            .height(56.dp)
            .drawWithCache {
                onDrawBehind {
                    // Background
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                if (isActive) SapphireRaised else SapphireMid,
                                SapphireMid,
                                SapphireDeep
                            ),
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = size.maxDimension
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                    )
                    // === Breathing aura — only on the active player's chip ===
                    if (isActive && pulseAlpha > 0f) {
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    GoldBright.copy(alpha = pulseAlpha * 0.35f),
                                    GoldBright.copy(alpha = pulseAlpha * 0.15f),
                                    Color.Transparent
                                ),
                                center = Offset(size.width / 2f, size.height / 2f),
                                radius = size.maxDimension
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                        )
                    }
                    // Gold trim — brighter when active
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            listOf(
                                if (isActive) GoldBright else GoldDeep,
                                GoldBright,
                                if (isActive) GoldBright else GoldDeep
                            )
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
                        style = Stroke(width = if (isActive) 2.5f else 1.2f)
                    )
                }
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Player token disc
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(player.light, player.primary, player.dark),
                            center = Offset(8f, 8f),
                            radius = 30f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(GoldBright.copy(alpha = 0.85f))
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = player.display,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W800,
                    color = TextPrimary
                )
                // === SECTION 4 — Animated score ticker ===
                AnimatedContent(
                    targetState = score,
                    transitionSpec = {
                        (slideInVertically { it } + fadeIn()) togetherWith
                        (slideOutVertically { -it } + fadeOut())
                    },
                    label = "score-ticker"
                ) { targetScore ->
                    Text(
                        text = if (isWinner) "WINNER" else "$targetScore pts",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.W700,
                        color = if (isWinner) GoldBright else TextSecondary
                    )
                }
            }
        }
    }
}
