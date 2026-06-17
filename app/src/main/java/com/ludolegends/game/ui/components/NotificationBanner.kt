package com.ludolegends.game.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludolegends.game.engine.GameNotification
import com.ludolegends.game.ui.theme.ActionGreen
import com.ludolegends.game.ui.theme.ActionRed
import com.ludolegends.game.ui.theme.GoldBright
import com.ludolegends.game.ui.theme.GoldDeep
import com.ludolegends.game.ui.theme.PlayerGreen
import com.ludolegends.game.ui.theme.SapphireDeep
import com.ludolegends.game.ui.theme.SapphireMid
import kotlinx.coroutines.delay

@Composable
fun NotificationBanner(
    notification: GameNotification?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(notification?.timestamp) {
        if (notification != null) {
            delay(2500)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = notification != null,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier
    ) {
        if (notification == null) return@AnimatedVisibility
        val accent = when (notification.type) {
            GameNotification.NotificationType.THREE_SIXES_BURN -> ActionRed
            GameNotification.NotificationType.CAPTURE          -> ActionRed
            GameNotification.NotificationType.UNLOCK           -> GoldBright
            GameNotification.NotificationType.BONUS_ROLL       -> ActionGreen
            GameNotification.NotificationType.HOME_REACHED     -> GoldBright
            GameNotification.NotificationType.WINNER           -> GoldBright
            GameNotification.NotificationType.BLOCKED_MOVE     -> ActionRed
            GameNotification.NotificationType.INFO             -> SapphireMid
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .drawWithCache {
                    onDrawBehind {
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(accent.copy(alpha = 0.35f), SapphireMid, SapphireDeep),
                                center = Offset(size.width / 2f, size.height / 2f),
                                radius = size.maxDimension
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                        )
                        drawRoundRect(
                            brush = Brush.horizontalGradient(listOf(accent, GoldBright, accent)),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
                            style = Stroke(width = 1.5f)
                        )
                    }
                }
        ) {
            Text(
                text = notification.message,
                fontSize = 12.sp,
                fontWeight = FontWeight.W700,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}
