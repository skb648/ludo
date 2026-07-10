// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ludolegends.game.audio.LudoAudioEngine
import com.ludolegends.game.data.WalletRepository
import com.ludolegends.game.data.room.LudoMatchEntity
import com.ludolegends.game.data.room.LudoMatchRepository
import com.ludolegends.game.data.room.LudoMatchSerializer
import com.ludolegends.game.engine.DiceRoller
import com.ludolegends.game.engine.FairRandomDiceRoller
import com.ludolegends.game.engine.GameMode
import com.ludolegends.game.engine.GameModeType
import com.ludolegends.game.engine.LudoEngine
import com.ludolegends.game.engine.LudoGameState
import com.ludolegends.game.engine.ManualDiceRoller
import com.ludolegends.game.engine.Player
import com.ludolegends.game.engine.ScoreCalculator
import com.ludolegends.game.engine.TurnPhase
import com.ludolegends.game.haptics.HapticFeedbackEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Hardened MVI ViewModel — v4.0 production grade.
 *
 * Wires together:
 *   • [LudoEngine] — UDF game state machine (mode-agnostic)
 *   • [WalletRepository] — DataStore-backed coin wallet + match economy
 *   • [LudoMatchRepository] — Room SQLite auto-save + resume-match list
 *   • [LudoAudioEngine] — SoundPool SFX + ExoPlayer BGM with volume control
 *   • [HapticFeedbackEngine] — system vibrator for crisp tactile feedback
 *   • Game-juice animation triggers (squash, shake, particles, confetti)
 *   • Live score matrix via [ScoreCalculator]
 *
 * === SECTION 4 — FREE OFFLINE BYPASS ===
 *   Local Pass & Play and Play With Friends modes are 100% free to access.
 *   The coin entry-fee deduction is SKIPPED when [SetupConfig.modeType]
 *   is one of those two modes. Only a hypothetical future Online mode
 *   would deduct coins.
 *
 * === SECTION 1 — ASYNCHRONOUS AUTO-SAVE ===
 *   After every committed move (captured or otherwise), the engine state
 *   is serialized and upserted into Room via [LudoMatchRepository.autoSave].
 *   The user can resume any saved match from the Lobby's "RESUME MATCH
 *   HISTORY" list.
 */
class LudoViewModel(
    private val audioEngine: LudoAudioEngine? = null,
    private val hapticEngine: HapticFeedbackEngine? = null,
    private val wallet: WalletRepository? = null,
    private val matchRepository: LudoMatchRepository? = null,
    private val diceRoller: DiceRoller = FairRandomDiceRoller()
) : ViewModel() {

    private val engine = LudoEngine(diceRoller)

    val state: StateFlow<LudoGameState> = engine.state

    private val _hopState = MutableStateFlow<HopState>(HopState.Idle)
    val hopState: StateFlow<HopState> = _hopState.asStateFlow()

    private val _screen = MutableStateFlow(Screen.LOBBY)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    private val _setupConfig = MutableStateFlow(SetupConfig())
    val setupConfig: StateFlow<SetupConfig> = _setupConfig.asStateFlow()

    val coinBalance: StateFlow<Int> = wallet?.coins
        ?.stateIn(viewModelScope, SharingStarted.Eagerly, WalletRepository.INITIAL_COINS)
        ?: MutableStateFlow(WalletRepository.INITIAL_COINS)

    val settings: StateFlow<SettingsState> = combine(
        wallet?.bgmVolume ?: MutableStateFlow(0.5f),
        wallet?.sfxVolume ?: MutableStateFlow(0.8f),
        wallet?.hapticEnabled ?: MutableStateFlow(true)
    ) { bgm, sfx, haptic ->
        SettingsState(bgmVolume = bgm, sfxVolume = sfx, hapticEnabled = haptic)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsState())

    /**
     * === SECTION 4 — REAL SCORE MATRIX ===
     * Live per-player score, computed from the engine state via
     * [ScoreCalculator]. The UI observes this and animates the numerical
     * updates via ticker transitions inside the horizontal profile banners.
     */
    val scoreMatrix: StateFlow<Map<Player, Int>> = engine.state
        .map { ScoreCalculator.compute(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    /** Live list of saved matches for the Lobby's Resume History list. */
    val savedMatches: StateFlow<List<LudoMatchEntity>> = matchRepository?.savedMatches
        ?.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        ?: MutableStateFlow(emptyList())

    private val _juiceEvents = MutableStateFlow<JuiceEvent>(JuiceEvent.Idle)
    val juiceEvents: StateFlow<JuiceEvent> = _juiceEvents.asStateFlow()

    private val _payoutAnimation = MutableStateFlow<PayoutAnimation?>(null)
    val payoutAnimation: StateFlow<PayoutAnimation?> = _payoutAnimation.asStateFlow()

    /** Exit-confirm dialog visibility — observed by the GameScreen. */
    private val _showExitDialog = MutableStateFlow(false)
    val showExitDialog: StateFlow<Boolean> = _showExitDialog.asStateFlow()

    private val inputMutex = Mutex()

    @Volatile private var animationJob: Job? = null

    /** The Room row id of the currently-active match (null until first auto-save). */
    @Volatile private var currentMatchId: Long? = null

    init {
        audioEngine?.setBgmVolume(settings.value.bgmVolume)
        audioEngine?.setSfxVolume(settings.value.sfxVolume)
        hapticEngine?.enabled = settings.value.hapticEnabled
    }

    // ============================================================
    // Navigation
    // ============================================================

    fun navigate(screen: Screen) {
        cancelAnimationAndRun {
            if (screen == Screen.LOBBY) audioEngine?.pauseBgm()
            _screen.value = screen
        }
    }

    /**
     * === SECTION 1 — EXIT CONFIRMATION DIALOG ===
     * Triggered by the [Menu] button. Shows the dialog instead of
     * immediately navigating. The match state is auto-saved in the
     * background before the dialog appears.
     */
    fun onMenuPressed() {
        autoSaveCurrentMatch()
        _showExitDialog.value = true
    }

    fun confirmExit() {
        _showExitDialog.value = false
        navigate(Screen.LOBBY)
    }

    fun continueMatch() {
        _showExitDialog.value = false
    }

    // ============================================================
    // Setup sheet configuration
    // ============================================================

    fun setPlayerCount(count: Int) {
        _setupConfig.update { it.copy(playerCount = count.coerceIn(2, 4)) }
    }

    fun setGameMode(mode: GameMode) {
        _setupConfig.update { it.copy(mode = mode) }
    }

    fun setGameModeType(modeType: GameModeType) {
        _setupConfig.update { it.copy(modeType = modeType) }
    }

    /**
     * === SECTION 4 — FREE OFFLINE BYPASS ===
     *
     * If the selected mode is LOCAL_PASS_PLAY or PLAY_WITH_FRIENDS,
     * the coin entry-fee deduction is SKIPPED — these modes are 100%
     * free to access regardless of the wallet balance. Only Online mode
     * (future) would deduct coins.
     */
    fun startMatchFromSetup() {
        cancelAnimationAndRun {
            val cfg = _setupConfig.value
            val isFreeMode = cfg.modeType == GameModeType.LOCAL_PASS_PLAY ||
                             cfg.modeType == GameModeType.PLAY_WITH_FRIENDS
            if (!isFreeMode) {
                val fundsOk = wallet?.deductEntryFee() ?: true
                if (!fundsOk) {
                    _juiceEvents.value = JuiceEvent.InsufficientFunds(
                        required = WalletRepository.ENTRY_FEE
                    )
                    return@cancelAnimationAndRun
                }
            }
            engine.setDiceRoller(
                if (cfg.modeType == GameModeType.PLAY_WITH_FRIENDS)
                    ManualDiceRoller()
                else
                    FairRandomDiceRoller()
            )
            engine.startNewGame(
                playerCount = cfg.playerCount,
                mode = cfg.mode,
                modeType = cfg.modeType
            )
            _hopState.value = HopState.Idle
            currentMatchId = null
            _screen.value = Screen.GAME
            audioEngine?.startBgm()
            autoSaveCurrentMatch()
        }
    }

    /**
     * === SECTION 1 — MATCH HISTORY RECOVERY ===
     *
     * Deserialize a saved match and resume gameplay exactly where the
     * player left off.
     */
    fun resumeMatch(entity: LudoMatchEntity) {
        cancelAnimationAndRun {
            val resumed = LudoMatchSerializer.deserialize(entity)
            engine.restoreState(resumed.state)
            currentMatchId = resumed.matchId
            _hopState.value = HopState.Idle
            _screen.value = Screen.GAME
            audioEngine?.startBgm()
        }
    }

    fun deleteSavedMatch(entity: LudoMatchEntity) {
        matchRepository?.deleteMatch(entity.id)
        if (currentMatchId == entity.id) currentMatchId = null
    }

    fun clearAllSavedMatches() {
        matchRepository?.clearAllMatches()
        currentMatchId = null
    }

    /**
     * === SECTION 1 — ASYNCHRONOUS AUTO-SAVE ===
     *
     * Serialize the current engine state and upsert it into Room on the
     * IO dispatcher. Called after every committed move (and on
     * Menu-button press). Does NOT block the UI thread.
     */
    private fun autoSaveCurrentMatch() {
        val s = state.value
        val coins = coinBalance.value
        val scores = scoreMatrix.value
        matchRepository?.autoSave(
            state = s,
            coinBalance = coins,
            scoreMatrix = scores,
            existingId = currentMatchId,
            onSaved = { newId -> currentMatchId = newId }
        )
    }

    // ============================================================
    // MODE 1 — Digital dice input (LOCAL_PASS_PLAY)
    // ============================================================

    fun onDigitalDiceTapped() {
        hapticEngine?.tick()
        launchInput {
            val s = state.value
            if (s.isAnimating) return@launchInput
            if (s.phase != TurnPhase.AWAITING_DICE) return@launchInput
            audioEngine?.playDiceRoll()
            val value = (diceRoller as? FairRandomDiceRoller)?.roll()
                ?: kotlin.random.Random.nextInt(1, 7)
            engine.submitDiceValue(value)
            val pending = state.value.pendingMove
            if (pending != null && state.value.phase == TurnPhase.PENDING_ANIMATION) {
                startHopAnimation()
            }
        }
    }

    fun onDigitalDiceRollComplete(value: Int) {
        launchInput {
            val s = state.value
            if (s.isAnimating) return@launchInput
            if (s.phase == TurnPhase.AWAITING_DICE) {
                engine.submitDiceValue(value)
                val pending = state.value.pendingMove
                if (pending != null && state.value.phase == TurnPhase.PENDING_ANIMATION) {
                    startHopAnimation()
                }
            }
        }
    }

    // ============================================================
    // MODE 2 — Manual dice input (PLAY_WITH_FRIENDS)
    // ============================================================

    fun submitManualDice(value: Int) {
        hapticEngine?.tick()
        launchInput {
            val s = state.value
            if (s.isAnimating) return@launchInput
            if (s.phase != TurnPhase.AWAITING_DICE) return@launchInput
            audioEngine?.playDiceRoll()
            engine.submitDiceValue(value)
            val pending = state.value.pendingMove
            if (pending != null && state.value.phase == TurnPhase.PENDING_ANIMATION) {
                startHopAnimation()
            }
        }
    }

    // ============================================================
    // Shared — token selection (both modes)
    // ============================================================

    fun onTokenTapped(player: Player, tokenId: Int) {
        launchInput {
            val s = state.value
            if (s.isAnimating) return@launchInput
            if (s.phase != TurnPhase.AWAITING_TOKEN_PICK) return@launchInput
            engine.selectToken(player, tokenId)
            val pending = state.value.pendingMove
            if (pending != null && state.value.phase == TurnPhase.PENDING_ANIMATION) {
                startHopAnimation()
            }
        }
    }

    fun undo() {
        cancelAnimationAndRun {
            engine.cancelPendingMove()
            engine.undo()
            autoSaveCurrentMatch()
        }
    }

    fun redo() {
        cancelAnimationAndRun {
            engine.cancelPendingMove()
            engine.redo()
            autoSaveCurrentMatch()
        }
    }

    fun clearNotification() { engine.clearNotification() }
    fun clearJuiceEvent() { _juiceEvents.value = JuiceEvent.Idle }
    fun clearPayoutAnimation() { _payoutAnimation.value = null }

    // ============================================================
    // Settings (live-updates the audio + haptic engines)
    // ============================================================

    fun setBgmVolume(volume: Float) {
        audioEngine?.setBgmVolume(volume)
        viewModelScope.launch(Dispatchers.IO) { wallet?.setBgmVolume(volume) }
    }

    fun setSfxVolume(volume: Float) {
        audioEngine?.setSfxVolume(volume)
        viewModelScope.launch(Dispatchers.IO) { wallet?.setSfxVolume(volume) }
    }

    fun setHapticEnabled(enabled: Boolean) {
        hapticEngine?.enabled = enabled
        viewModelScope.launch(Dispatchers.IO) { wallet?.setHapticEnabled(enabled) }
    }

    // ============================================================
    // Hop animation — independent sequential coroutine per move
    // ============================================================

    private fun startHopAnimation() {
        animationJob?.cancel()
        animationJob = viewModelScope.launch(Dispatchers.Main) {
            try {
                animatePendingMove()
            } catch (_: kotlinx.coroutines.CancellationException) {
                _hopState.value = HopState.Idle
            }
        }
    }

    private suspend fun animatePendingMove() {
        val pending = state.value.pendingMove ?: return

        if (pending.isUnlock) {
            _hopState.value = HopState.Hopping(
                playerId = pending.player,
                tokenId = pending.tokenId,
                fromStep = 0, toStep = 0, progress = 1f
            )
            audioEngine?.playTokenHop()
            hapticEngine?.tap()
            kotlinx.coroutines.delay(180)
            _hopState.value = HopState.Idle
            if (pending.captured.isNotEmpty()) {
                audioEngine?.playTokenKill()
                hapticEngine?.capture()
                _juiceEvents.value = JuiceEvent.Capture(System.currentTimeMillis())
            }
            engine.commitPendingMove()
            autoSaveCurrentMatch()
            handleWinAndPayout(pending)
            return
        }

        val totalHops = pending.toStep - pending.fromStep
        val perHopMs = 180L

        for (i in 0 until totalHops) {
            val stepFrom = pending.fromStep + i
            val stepTo = pending.fromStep + i + 1
            val start = System.currentTimeMillis()
            val duration = perHopMs.toFloat()
            var hopSoundFired = false
            while (true) {
                val elapsed = System.currentTimeMillis() - start
                val t = (elapsed / duration).coerceIn(0f, 1f)
                val eased = fastOutSlowIn(t)
                _hopState.value = HopState.Hopping(
                    playerId = pending.player,
                    tokenId = pending.tokenId,
                    fromStep = stepFrom, toStep = stepTo, progress = eased
                )
                if (!hopSoundFired && t >= 0.5f) {
                    audioEngine?.playTokenHop()
                    hapticEngine?.tap()
                    hopSoundFired = true
                }
                if (t >= 1f) break
                kotlinx.coroutines.delay(16)
            }
            if (i == totalHops - 1) {
                _juiceEvents.value = JuiceEvent.SquashLanding(System.currentTimeMillis())
            }
        }

        _hopState.value = HopState.Idle

        if (pending.captured.isNotEmpty()) {
            audioEngine?.playTokenKill()
            hapticEngine?.capture()
            _juiceEvents.value = JuiceEvent.Capture(System.currentTimeMillis())
        }
        engine.commitPendingMove()
        autoSaveCurrentMatch()
        handleWinAndPayout(pending)
    }

    private suspend fun handleWinAndPayout(pending: com.ludolegends.game.engine.PendingMove) {
        if (!pending.isWinningMove) return
        audioEngine?.playVictory()
        hapticEngine?.victory()
        _juiceEvents.value = JuiceEvent.Victory(System.currentTimeMillis())
        val cfg = _setupConfig.value
        val isFreeMode = cfg.modeType == GameModeType.LOCAL_PASS_PLAY ||
                         cfg.modeType == GameModeType.PLAY_WITH_FRIENDS
        if (isFreeMode) return
        val soloMode = cfg.mode != GameMode.TEAM_2V2
        val prize = wallet?.prizePoolFor(soloMode) ?: 0
        if (prize > 0 && wallet != null) {
            val tickerDurationMs = 1500L
            val tickerStart = System.currentTimeMillis()
            while (true) {
                val elapsed = System.currentTimeMillis() - tickerStart
                if (elapsed >= tickerDurationMs) break
                val t = (elapsed.toFloat() / tickerDurationMs).coerceIn(0f, 1f)
                val eased = 1f - (1f - t) * (1f - t) * (1f - t)
                val displayed = (prize * eased).toInt()
                _payoutAnimation.value = PayoutAnimation(
                    totalPrize = prize,
                    currentCount = displayed,
                    finalBalance = null
                )
                kotlinx.coroutines.delay(33)
            }
            val newBalance = wallet.depositWinnings(prize)
            _payoutAnimation.value = PayoutAnimation(
                totalPrize = prize,
                currentCount = prize,
                finalBalance = newBalance
            )
        }
    }

    private fun cancelAnimationAndRun(block: suspend CoroutineScope.() -> Unit) {
        animationJob?.cancel()
        animationJob = null
        _hopState.value = HopState.Idle
        launchInput(block)
    }

    private fun launchInput(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(Dispatchers.Main) {
            inputMutex.withLock {
                try {
                    block()
                } catch (_: kotlinx.coroutines.CancellationException) {
                    // Intentional cancellation — release mutex and exit.
                }
            }
        }
    }

    private fun fastOutSlowIn(t: Float): Float {
        val x = t.coerceIn(0f, 1f)
        return 1f - (1f - x) * (1f - x) * (1f - x) * (1f - x)
    }

    override fun onCleared() {
        super.onCleared()
        animationJob?.cancel()
        animationJob = null
    }

    // ============================================================
    // Public types
    // ============================================================

    enum class Screen { LOBBY, GAME }

    data class SetupConfig(
        val playerCount: Int = 4,
        val mode: GameMode = GameMode.STANDARD,
        val modeType: GameModeType = GameModeType.LOCAL_PASS_PLAY
    )

    data class SettingsState(
        val bgmVolume: Float = 0.5f,
        val sfxVolume: Float = 0.8f,
        val hapticEnabled: Boolean = true
    )

    sealed class HopState {
        data object Idle : HopState()
        data class Hopping(
            val playerId: Player,
            val tokenId: Int,
            val fromStep: Int,
            val toStep: Int,
            val progress: Float
        ) : HopState()
    }

    sealed class JuiceEvent {
        data object Idle : JuiceEvent()
        data class Capture(val timestamp: Long) : JuiceEvent()
        data class SquashLanding(val timestamp: Long) : JuiceEvent()
        data class Victory(val timestamp: Long) : JuiceEvent()
        data class InsufficientFunds(val required: Int) : JuiceEvent()
    }

    data class PayoutAnimation(
        val totalPrize: Int,
        val currentCount: Int,
        val finalBalance: Int?
    )

    companion object {
        private const val TAG = "LudoViewModel"
    }
}
