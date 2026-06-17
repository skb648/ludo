package com.ludolegends.game.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludolegends.game.engine.TurnPhase
import com.ludolegends.game.jui.ParticleBurstOverlay
import com.ludolegends.game.jui.rememberScreenShake
import com.ludolegends.game.jui.rememberSquashAnim
import com.ludolegends.game.ui.components.AnimatedDice3D
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
import androidx.compose.foundation.layout.offset as offsetModifier

/**
 * === MODE 1 — STANDARD "LOCAL PASS & PLAY" (100% Ludo King Clone) ===
 *
 *  • NO manual 1-6 dice input injector panel.
 *  • An [AnimatedDice3D] composable drives rolls via fair randomness.
 *  • All four player slots are human.
 *  • Pulsating glow on selectable tokens, squash-and-stretch landing,
 *    screen shake + particle burst on captures.
 */
@Composable
fun LocalPlayScreen(
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

    val passInstruction = if (state.winner == null && state.phase == TurnPhase.AWAITING_DICE) {
        "Pass the device to ${state.currentPlayer.display}"
    } else null

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
            // Apply screen shake offset to the board
            val shakeOffset = if (screenShake.active) {
                offsetModifier { IntOffset(screenShake.offsetX.toInt(), screenShake.offsetY.toInt()) }
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

                // === Particle burst overlay on capture ===
                if (captureTrigger != 0) {
                    ParticleBurstOverlay(
                        triggerKey = captureTrigger,
                        center = androidx.compose.ui.geometry.Offset(540f, 960f),
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (passInstruction != null) {
                    Text(
                        text = passInstruction,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W700,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Text(
                        text = "Tap the dice to roll",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.W500,
                        color = TextSecondary,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                } else if (state.phase == TurnPhase.AWAITING_TOKEN_PICK) {
                    Text(
                        text = "${state.currentPlayer.display}'s turn",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W700,
                        color = TextPrimary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Text(
                        text = "Tap a glowing pawn to move",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.W500,
                        color = TextSecondary,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }
            }
            AnimatedDice3D(
                enabled = diceEnabled,
                onRollStarted = { },
                onRollComplete = viewModel::onDigitalDiceRollComplete,
                size = 96.dp
            )
        }

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
