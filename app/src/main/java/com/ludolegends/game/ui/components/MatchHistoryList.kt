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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludolegends.game.data.room.LudoMatchEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * === SECTION 1 — MATCH HISTORY RECOVERY LIST ===
 *
 * Renders the live list of saved matches from the Room database.
 * Each card displays:
 *   • The match label (mode + player count).
 *   • The saved timestamp.
 *   • The active player turn at save time.
 *   • The coin balance at save time.
 *
 * Tap → [onResume] is invoked with the entity id (the caller deserializes
 * and resumes gameplay). Trash icon → [onDelete].
 */
@Composable
fun MatchHistoryList(
    matches: List<LudoMatchEntity>,
    onResume: (LudoMatchEntity) -> Unit,
    onDelete: (LudoMatchEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.History,
                contentDescription = null,
                tint = GoldBright,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "RESUME MATCH HISTORY",
                fontSize = 14.sp,
                fontWeight = FontWeight.W800,
                color = GoldBright,
                letterSpacing = 1.5.sp
            )
        }

        if (matches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No saved matches yet — play a game and your progress will auto-save.",
                    fontSize = 11.sp,
                    color = TextSecondary.copy(alpha = 0.7f)
                )
            }
        } else {
            for (match in matches) {
                SavedMatchCard(
                    match = match,
                    onResume = { onResume(match) },
                    onDelete = { onDelete(match) }
                )
            }
        }
    }
}

@Composable
private fun SavedMatchCard(
    match: LudoMatchEntity,
    onResume: () -> Unit,
    onDelete: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val dateFormat = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }
    val formattedDate = remember(match.savedAt) { dateFormat.format(Date(match.savedAt)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onResume
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
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                    )
                    drawRoundRect(
                        brush = Brush.linearGradient(listOf(GoldBright, GoldDeep, GoldBright)),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
                        style = Stroke(width = 1.5f)
                    )
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Resume icon disc
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(ActionGreenBright, ActionGreen),
                            center = Offset(10f, 10f),
                            radius = 30f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Resume",
                    tint = SapphireDeep,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            // Match info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = match.matchLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W800,
                    color = TextPrimary
                )
                Text(
                    text = "$formattedDate  •  ${match.coinBalance} coins  •  Turn ${match.activeTurnIndex + 1}",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
            // Delete icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDelete
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = PlayerRed.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
