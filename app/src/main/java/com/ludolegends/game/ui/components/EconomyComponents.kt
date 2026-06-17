package com.ludolegends.game.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.ludolegends.game.ui.theme.SapphireDeep
import com.ludolegends.game.ui.theme.SapphireMid
import com.ludolegends.game.ui.theme.SapphireRaised
import com.ludolegends.game.ui.theme.TextPrimary
import com.ludolegends.game.viewmodel.LudoViewModel
import java.text.NumberFormat
import java.util.Locale

/**
 * Persistent coin-balance badge — shown at the top-right of every screen.
 * Backed by DataStore so the balance survives app restarts.
 */
@Composable
fun CoinBalanceBadge(
    balance: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .drawWithCache {
                onDrawBehind {
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(SapphireRaised, SapphireMid, SapphireDeep),
                            center = Offset(0f, 0f),
                            radius = size.maxDimension
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
                    )
                    drawRoundRect(
                        brush = Brush.linearGradient(listOf(GoldBright, GoldDeep, GoldBright)),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f),
                        style = Stroke(width = 1.5f)
                    )
                }
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(GoldBright, GoldDeep),
                            center = Offset(6f, 6f),
                            radius = 14f
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MonetizationOn,
                    contentDescription = "Coins",
                    tint = SapphireDeep,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = NumberFormat.getNumberInstance(Locale.US).format(balance),
                fontSize = 14.sp,
                fontWeight = FontWeight.W800,
                color = GoldBright
            )
        }
    }
}

/**
 * === SECTION 3 — WINNER LOOT DEPOSIT POP-UP ===
 *
 * Magnificent pop-up banner with animated sequential coin-count ticker
 * that adds the loot back to the wallet. Shows:
 *   • "VICTORY!" headline
 *   • Coin count ticker ramping from 0 → totalPrize
 *   • New wallet balance once the ticker completes
 *   • "CLAIM" dismiss button
 */
@Composable
fun WinnerPayoutBanner(
    payout: LudoViewModel.PayoutAnimation,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}  // swallow backdrop taps — must press CLAIM
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .drawWithCache {
                    onDrawBehind {
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(SapphireRaised, SapphireMid, SapphireDeep),
                                center = Offset(size.width / 2f, size.height / 2f),
                                radius = size.maxDimension
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f)
                        )
                        drawRoundRect(
                            brush = Brush.linearGradient(listOf(GoldBright, GoldDeep, GoldBright)),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f, 20f),
                            style = Stroke(width = 3f)
                        )
                        drawRoundRect(
                            color = GoldDeep,
                            topLeft = Offset(4f, 4f),
                            size = Size(size.width - 8f, size.height - 8f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f),
                            style = Stroke(width = 1f)
                        )
                    }
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "VICTORY!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.W900,
                    color = GoldBright,
                    letterSpacing = 4.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Match won — loot secured",
                    fontSize = 12.sp,
                    color = TextPrimary.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(20.dp))
                // Animated coin ticker
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.MonetizationOn,
                        contentDescription = null,
                        tint = GoldBright,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "+${NumberFormat.getNumberInstance(Locale.US).format(payout.currentCount)}",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.W900,
                        color = GoldBright
                    )
                }
                Spacer(Modifier.height(20.dp))
                if (payout.finalBalance != null) {
                    Text(
                        text = "Wallet Balance: ${NumberFormat.getNumberInstance(Locale.US).format(payout.finalBalance)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W700,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(20.dp))
                    ClaimButton(onClick = onDismiss)
                } else {
                    Text(
                        text = "Depositing loot...",
                        fontSize = 12.sp,
                        color = TextPrimary.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ClaimButton(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .drawWithCache {
                onDrawBehind {
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(ActionGreenBright, ActionGreen),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        ),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                    )
                    drawRoundRect(
                        brush = Brush.linearGradient(listOf(GoldBright, GoldDeep, GoldBright)),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
                        style = Stroke(width = 2f)
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "CLAIM",
            fontSize = 16.sp,
            fontWeight = FontWeight.W900,
            color = SapphireDeep,
            letterSpacing = 3.sp
        )
    }
}
