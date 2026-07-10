// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludolegends.game.engine.TurnPhase
import com.ludolegends.game.jui.ParticleBurstOverlay
import com.ludolegends.game.jui.rememberScreenShake
import com.ludolegends.game.jui.rememberSquashAnim
import com.ludolegends.game.ui.components.DicePanel
import com.ludolegends.game.ui.components.FooterNav
import com.ludolegends.game.ui.components.GoldTrimBorder
import com.ludolegends.game.ui.components.LudoBoard
import com.ludolegends.game.ui.components.NotificationBanner
import com.ludolegends.game.ui.components.PlayerStatusBar
import com.ludolegends.game.ui.theme.ActionGreen
import com.ludolegends.game.ui.theme.GoldBright
import com.ludolegends.game.ui.theme.PlayerBlue
import com.ludolegends.game.ui.theme.PlayerGreen
import com.ludolegends.game.ui.theme.PlayerRed
import com.ludolegends.game.ui.theme.PlayerYellow
import com.ludolegends.game.ui.theme.SapphireDeep
import com.ludolegends.game.ui.theme.TextPrimary
import com.ludolegends.game.ui.theme.TextSecondary
import com.ludolegends.game.viewmodel.LudoViewModel

/**
 * === MODE 2 — "PLAY WITH FRIENDS" (Hybrid Physical Dice Engine) ===
 *
 *  • Retains the [DicePanel] (1–6 manual buttons) at the bottom.
 *  • NO bots — all 4 slots are real human players.
 *  • Players roll a real wooden dice in physical space, then tap the
 *    matching number on screen.
 *  • Same game-juice overlays as MODE 1: squash, shake, particles.
 */
@Composable
fun PlayWithFriendsScreen(
    viewModel: LudoViewModel,
    scoreMatrix: Map<com.ludolegends.game.engine.Player, Int>,
    onMenu: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val hopState by viewModel.hopState.collectAsState()
    val juiceEvent by viewModel.juiceEvents.collectAsState()

    val diceEnabled = state.phase == TurnPhase.AWAITING_DICE &&
                      !state.isAnimating &&
                      state.winner == null

    val instruction = when {
        state.winner != null -> null
        state.phase == TurnPhase.AWAITING_DICE ->
            "${state.currentPlayer.display}: roll your wooden dice, tap the matching number"
        state.phase == TurnPhase.AWAITING_TOKEN_PICK ->
            "${state.currentPlayer.display}: tap a glowing pawn to move"
        else -> null
    }

    // === Game juice states ===
    val squashAnim = rememberSquashAnim()
    val screenShake = rememberScreenShake()
    val captureTrigger = (juiceEvent as? LudoViewModel.JuiceEvent.Capture)?.timestamp?.hashCode() ?: 0
    val squashTrigger = (juiceEvent as? LudoViewModel.JuiceEvent.SquashLanding)?.timestamp ?: 0L

    LaunchedEffect(squashTrigger) {
        if (squashTrigger != 0L) squashAnim.trigger()
    }
    LaunchedEffect(captureTrigger) {
        if (captureTrigger != 0) screenShake.trigger()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        NotificationBanner(
            notification = state.notification,
            onDismiss = { viewModel.clearNotification() }
        )

        Spacer(Modifier.height(4.dp))

        PlayerStatusBar(state = state, scoreMatrix = scoreMatrix)

        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            val shakeOffset = if (screenShake.active) {
                Modifier.offset(
                    x = screenShake.offsetX.toInt().dp,
                    y = screenShake.offsetY.toInt().dp
                )
            } else Modifier
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(shakeOffset)
            ) {
                LudoBoard(
                    state = state,
                    hopState = hopState,
                    onTokenTapped = viewModel::onTokenTapped,
                    modifier = Modifier.fillMaxWidth()
                )

                if (captureTrigger != 0) {
                    ParticleBurstOverlay(
                        triggerKey = captureTrigger,
                        center = Offset(540f, 960f),
                        particleColors = listOf(PlayerRed, PlayerGreen, PlayerBlue, PlayerYellow, GoldBright)
                    )
                }
            }

            if (state.winner != null) {
                WinnerOverlay(
                    winnerName = state.winner!!.display,
                    onRestart = { viewModel.startMatchFromSetup() },
                    onMenu = onMenu
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (instruction != null) {
            Text(
                text = instruction,
                fontSize = 13.sp,
                fontWeight = FontWeight.W700,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
            )
            Spacer(Modifier.height(4.dp))
        }

        DicePanel(
            enabled = diceEnabled,
            onDiceTapped = viewModel::submitManualDice
        )

        Spacer(Modifier.height(8.dp))

        FooterNav(
            onMenu = onMenu,
            onUndo = viewModel::undo,
            onRedo = viewModel::redo,
            onSettings = onSettings
        )
    }
}

@Composable
private fun WinnerOverlay(
    winnerName: String,
    onRestart: () -> Unit,
    onMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        GoldTrimBorder(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "WINNER",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W800,
                    color = TextSecondary,
                    letterSpacing = 4.sp
                )
                Text(
                    text = winnerName,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.W900,
                    color = GoldBright
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onRestart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ActionGreen
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("PLAY AGAIN", color = SapphireDeep, fontWeight = FontWeight.W900)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onMenu,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("MAIN MENU", color = GoldBright, fontWeight = FontWeight.W800)
                }
            }
        }
    }
}
