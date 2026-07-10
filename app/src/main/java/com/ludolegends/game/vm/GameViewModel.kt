package com.ludolegends.game.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ludolegends.game.data.WalletRepository
import com.ludolegends.game.engine.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

enum class DiceInputMode { ROLLER, MANUAL }
enum class SoundCue { DICE, HOP, CAPTURE, VICTORY }
data class MatchConfig(val mode: GameMode=GameMode.STANDARD, val playerCount:Int=4, val diceInputMode:DiceInputMode=DiceInputMode.ROLLER)
data class HopFrame(val tokenId:Int, val from:BoardGeometry.Cell, val to:BoardGeometry.Cell, val progress:Float, val lift:Float)
data class DiceRollAnim(val id:Long, val face:Int)
data class CaptureBurst(val id:Long, val cell:BoardGeometry.Cell, val color:PlayerColor)
data class LandingPulse(val id:Long, val color:PlayerColor)
data class HomeConfetti(val id:Long, val color:PlayerColor)
data class PrizeEvent(val id:Long, val amount:Int)
data class SoundRequest(val id:Long, val cue:SoundCue)
data class GameEvent(val id:Long, val message:String)
data class GameUiState(
    val game:GameState?=null, val diceInputMode:DiceInputMode=DiceInputMode.ROLLER,
    val hop:HopFrame?=null, val diceAnim:DiceRollAnim?=null, val lastDiceShown:Int?=null,
    val canUndo:Boolean=false, val canRedo:Boolean=false, val event:GameEvent?=null,
    val sound:SoundRequest?=null, val burst:CaptureBurst?=null, val landing:LandingPulse?=null,
    val homeConfetti:HomeConfetti?=null, val prize:PrizeEvent?=null, val celebrating:PlayerColor?=null
)

class GameViewModel(app:Application):AndroidViewModel(app) {
    private val wallet=WalletRepository(app)
    val balance=wallet.balance.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WalletRepository.STARTING_BALANCE)
    val sfxVolume=wallet.sfxVolume.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),.8f)
    val bgmVolume=wallet.bgmVolume.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),.35f)
    val hapticsEnabled=wallet.hapticsEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),true)
    private val _ui=MutableStateFlow(GameUiState()); val ui:StateFlow<GameUiState> = _ui.asStateFlow()
    private val undo=ArrayDeque<GameState>(); private val redo=ArrayDeque<GameState>()
    private var paid=false; private var settled=false

    fun startMatch(config:MatchConfig) {
        val colors=PlayerColor.entries.take(config.playerCount.coerceIn(2,4))
        val seats=colors.mapIndexed { i,c -> PlayerSeat(c, SeatKind.HUMAN, "Player ${i+1}", if(config.mode==GameMode.TEAM_2V2) i%2 else i) }
        undo.clear(); redo.clear(); settled=false
        _ui.value=GameUiState(GameState.newMatch(config.mode,seats),config.diceInputMode)
    }
    fun rollDice() { val g=_ui.value.game?:return; if(_ui.value.diceInputMode!=DiceInputMode.ROLLER||g.phase!=TurnPhase.AWAIT_DICE)return; viewModelScope.launch { val id=System.nanoTime(); repeat(8){ frame -> _ui.update{state->state.copy(diceAnim=DiceRollAnim(id,Random.nextInt(1,7)),sound=if(frame==0) SoundRequest(id,SoundCue.DICE) else state.sound)}; delay(55) }; processDice(Random.nextInt(1,7)) } }
    fun enterDice(value:Int){ if(_ui.value.diceInputMode==DiceInputMode.MANUAL) processDice(value) }
    private fun processDice(value:Int) { val old=_ui.value.game?:return; if(old.phase!=TurnPhase.AWAIT_DICE)return; saveUndo(old); val applied=LudoRules.applyDice(old,value); _ui.update{it.copy(game=applied.state,lastDiceShown=value,diceAnim=null,canUndo=true,canRedo=false,sound=SoundRequest(System.nanoTime(),SoundCue.DICE))}; val ids=(applied.outcome as? DiceOutcome.ChooseMove)?.movableTokenIds; if(ids?.size==1) moveToken(ids.first()) }
    fun moveToken(id:Int){ val g=_ui.value.game?:return; if(g.phase!=TurnPhase.AWAIT_MOVE||id !in g.movableTokenIds)return; viewModelScope.launch { val result=LudoRules.applyMove(g,id); val token=g.token(id); var fromPos=token.pos; _ui.update{it.copy(game=g.copy(phase=TurnPhase.ANIMATING))}; for(pos in result.pathPositions){ val from=BoardGeometry.cellFor(token.color,fromPos,id%4); val to=BoardGeometry.cellFor(token.color,pos,id%4); for(frame in 1..7){ val p=frame/7f; _ui.update{it.copy(hop=HopFrame(id,from,to,p,sin(Math.PI*p).toFloat()))}; delay(32) }; fromPos=pos; _ui.update{it.copy(sound=SoundRequest(System.nanoTime(),SoundCue.HOP))} }; val now=System.nanoTime(); _ui.update{it.copy(game=result.state,hop=null,landing=LandingPulse(now,token.color),burst=if(result.capturedTokenIds.isNotEmpty()) CaptureBurst(now,BoardGeometry.cellFor(token.color,result.pathPositions.last(),id%4),token.color) else null,homeConfetti=if(result.reachedHome) HomeConfetti(now,token.color) else null,celebrating=result.winnerDeclared,sound=SoundRequest(now,if(result.winnerDeclared!=null) SoundCue.VICTORY else if(result.capturedTokenIds.isNotEmpty()) SoundCue.CAPTURE else SoundCue.HOP),canUndo=true)}; if(result.winnerDeclared!=null&&!settled){settled=true;wallet.depositPrize();_ui.update{it.copy(prize=PrizeEvent(now,WalletRepository.WIN_PRIZE))}} } }
    fun undo(){ val current=_ui.value.game?:return; val prev=undo.removeLastOrNull()?:return; redo.addLast(current); _ui.update{it.copy(game=prev,canUndo=undo.isNotEmpty(),canRedo=true,celebrating=null)} }
    fun redo(){ val current=_ui.value.game?:return; val next=redo.removeLastOrNull()?:return; undo.addLast(current); _ui.update{it.copy(game=next,canUndo=true,canRedo=redo.isNotEmpty())} }
    private fun saveUndo(state:GameState){undo.addLast(state);if(undo.size>30)undo.removeFirst();redo.clear()}
    fun setSfxVolume(v:Float)=viewModelScope.launch{wallet.setSfxVolume(v)}
    fun setBgmVolume(v:Float)=viewModelScope.launch{wallet.setBgmVolume(v)}
    fun setHapticsEnabled(v:Boolean)=viewModelScope.launch{wallet.setHapticsEnabled(v)}
    fun quitMatch(){ if(paid&&!settled)viewModelScope.launch{wallet.refundEntryFee()}; _ui.value=GameUiState() }
}
