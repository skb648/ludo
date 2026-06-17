package com.ludolegends.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ludolegends.game.audio.LudoAudioEngine
import com.ludolegends.game.audio.ProvideLudoAudio
import com.ludolegends.game.data.WalletRepository
import com.ludolegends.game.data.room.LudoMatchRepository
import com.ludolegends.game.engine.GameModeType
import com.ludolegends.game.haptics.HapticFeedbackEngine
import com.ludolegends.game.haptics.LocalHapticEngine
import com.ludolegends.game.jui.VictoryConfettiOverlay
import com.ludolegends.game.ui.components.CoinBalanceBadge
import com.ludolegends.game.ui.components.ExitConfirmDialog
import com.ludolegends.game.ui.components.MatchHistoryList
import com.ludolegends.game.ui.components.WinnerPayoutBanner
import com.ludolegends.game.ui.screens.LobbyScreen
import com.ludolegends.game.ui.screens.LocalPlayScreen
import com.ludolegends.game.ui.screens.PlayWithFriendsScreen
import com.ludolegends.game.ui.screens.SettingsSheet
import com.ludolegends.game.ui.screens.SetupSheet
import com.ludolegends.game.ui.theme.LudoLegendsTheme
import com.ludolegends.game.viewmodel.LudoViewModel

/**
 * Single-activity host for the entire Ludo Legends v4.0 application.
 *
 * Routing:
 *   LOBBY  →  SetupSheet overlay  →  GAME (mode-aware)
 *
 * Activity wires together the audio + haptic + wallet + Room-match
 * dependencies and injects them into the [LudoViewModel] via its
 * factory lambda.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LudoLegendsTheme {
                ProvideLudoAudio {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        LudoLegendsApp()
                    }
                }
            }
        }
    }
}

@Composable
fun LudoLegendsApp() {
    val context = LocalContext.current
    val audioEngine = remember { LudoAudioEngine.create(context) }
    val hapticEngine = remember { HapticFeedbackEngine(context) }
    val wallet = remember { WalletRepository(context) }
    val matchRepo = remember { LudoMatchRepository(context) }

    val viewModel: LudoViewModel = viewModel {
        LudoViewModel(
            audioEngine = audioEngine,
            hapticEngine = hapticEngine,
            wallet = wallet,
            matchRepository = matchRepo
        )
    }

    val screen by viewModel.screen.collectAsState()
    val state by viewModel.state.collectAsState()
    val setupConfig by viewModel.setupConfig.collectAsState()
    val coinBalance by viewModel.coinBalance.collectAsState()
    val juiceEvent by viewModel.juiceEvents.collectAsState()
    val payoutAnimation by viewModel.payoutAnimation.collectAsState()
    val showExitDialog by viewModel.showExitDialog.collectAsState()
    val savedMatches by viewModel.savedMatches.collectAsState()
    val scoreMatrix by viewModel.scoreMatrix.collectAsState()

    var showSetup by remember { mutableStateOf(false) }
    var setupModeType by remember { mutableStateOf(GameModeType.LOCAL_PASS_PLAY) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalHapticEngine provides hapticEngine) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = screen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen-transition",
                modifier = Modifier.fillMaxSize()
            ) { current ->
                when (current) {
                    LudoViewModel.Screen.LOBBY -> {
                        LobbyScreen(
                            onLocalPlay = {
                                setupModeType = GameModeType.LOCAL_PASS_PLAY
                                viewModel.setGameModeType(setupModeType)
                                showSetup = true
                            },
                            onFriends = {
                                setupModeType = GameModeType.PLAY_WITH_FRIENDS
                                viewModel.setGameModeType(setupModeType)
                                showSetup = true
                            },
                            onOnline = {
                                setupModeType = GameModeType.LOCAL_PASS_PLAY
                                viewModel.setGameModeType(setupModeType)
                                showSetup = true
                            }
                        )
                        // === SECTION 1 — MATCH HISTORY RECOVERY LIST ===
                        // Injected BELOW the lobby cards. The list observes
                        // savedMatches live from Room, so newly-saved matches
                        // appear without a refresh.
                        MatchHistoryList(
                            matches = savedMatches,
                            onResume = { entity -> viewModel.resumeMatch(entity) },
                            onDelete = { entity -> viewModel.deleteSavedMatch(entity) },
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    LudoViewModel.Screen.GAME -> {
                        when (state.gameModeType) {
                            GameModeType.LOCAL_PASS_PLAY -> {
                                LocalPlayScreen(
                                    viewModel = viewModel,
                                    scoreMatrix = scoreMatrix,
                                    onMenu = { viewModel.onMenuPressed() },
                                    onSettings = { showSettingsSheet = true }
                                )
                            }
                            GameModeType.PLAY_WITH_FRIENDS -> {
                                PlayWithFriendsScreen(
                                    viewModel = viewModel,
                                    scoreMatrix = scoreMatrix,
                                    onMenu = { viewModel.onMenuPressed() },
                                    onSettings = { showSettingsSheet = true }
                                )
                            }
                        }
                    }
                }
            }

            // === Persistent coin balance badge (top-right of every screen) ===
            CoinBalanceBadge(
                balance = coinBalance,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 12.dp)
            )

            // === Dismissable Setup Sheet overlay ===
            SetupSheet(
                visible = showSetup,
                playerCount = setupConfig.playerCount,
                gameMode = setupConfig.mode,
                modeType = setupConfig.modeType,
                onPlayerCountChange = viewModel::setPlayerCount,
                onGameModeChange = viewModel::setGameMode,
                onGameModeTypeChange = viewModel::setGameModeType,
                onStartMatch = {
                    showSetup = false
                    viewModel.startMatchFromSetup()
                },
                onDismiss = { showSetup = false }
            )

            // === Dismissable Settings Sheet overlay ===
            SettingsSheet(
                visible = showSettingsSheet,
                viewModel = viewModel,
                onDismiss = { showSettingsSheet = false },
                onReturnToMenu = {
                    showSettingsSheet = false
                    viewModel.navigate(LudoViewModel.Screen.LOBBY)
                },
                onRestartMatch = {
                    showSettingsSheet = false
                    viewModel.startMatchFromSetup()
                }
            )

            // === SECTION 1 — EXIT CONFIRMATION DIALOG ===
            ExitConfirmDialog(
                visible = showExitDialog,
                onConfirmExit = { viewModel.confirmExit() },
                onContinue = { viewModel.continueMatch() }
            )

            // === Insufficient-funds prompt (only triggers in non-free modes) ===
            if (juiceEvent is LudoViewModel.JuiceEvent.InsufficientFunds) {
                val required = (juiceEvent as LudoViewModel.JuiceEvent.InsufficientFunds).required
                AlertDialog(
                    onDismissRequest = { viewModel.clearJuiceEvent() },
                    title = { Text("Insufficient Coins") },
                    text = {
                        Text(
                            "You need at least $required coins to start a match. " +
                            "Win matches to earn more!"
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearJuiceEvent() }) { Text("OK") }
                    }
                )
            }

            // === Winner payout pop-up banner ===
            if (payoutAnimation != null) {
                WinnerPayoutBanner(
                    payout = payoutAnimation!!,
                    onDismiss = { viewModel.clearPayoutAnimation() }
                )
            }

            // === Victory confetti overlay (full-screen, looping) ===
            VictoryConfettiOverlay(
                active = juiceEvent is LudoViewModel.JuiceEvent.Victory
            )
        }
    }
}
