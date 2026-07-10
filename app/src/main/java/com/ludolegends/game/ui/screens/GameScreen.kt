package com.ludolegends.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ludolegends.game.audio.LudoAudioEngine
import com.ludolegends.game.engine.*
import com.ludolegends.game.ui.board.*
import com.ludolegends.game.ui.components.*
import com.ludolegends.game.ui.theme.LudoColors
import com.ludolegends.game.vm.*
import kotlinx.coroutines.delay

@Composable fun GameScreen(vm:GameViewModel,onExit:()->Unit){val ui by vm.ui.collectAsState();val balance by vm.balance.collectAsState();val sfx by vm.sfxVolume.collectAsState();val bgm by vm.bgmVolume.collectAsState();val haptics by vm.hapticsEnabled.collectAsState();val context=LocalContext.current;val audio=remember{LudoAudioEngine(context)};var settings by remember{mutableStateOf(false)};var exitConfirm by remember{mutableStateOf(false)};DisposableEffect(Unit){audio.startMusic(bgm);onDispose{audio.close()}};LaunchedEffect(bgm){audio.setMusicVolume(bgm)};LaunchedEffect(ui.sound?.id){ui.sound?.let{audio.play(it.cue,sfx,haptics)}};BackHandler{exitConfirm=true};val game=ui.game?:return
    Box(Modifier.fillMaxSize().background(LudoColors.Background)){Column(Modifier.fillMaxSize().systemBarsPadding()){Row(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically){IconButton(onClick={exitConfirm=true}){Icon(Icons.Default.ArrowBack,null,tint=LudoColors.Gold)};Column{Text(if(game.mode==GameMode.TEAM_2V2)"TEAM BATTLE" else "CLASSIC MATCH",color=Color.White,fontWeight=FontWeight.Black);Text("${game.currentSeat.name} • ${game.currentSeat.color.displayName}",color=game.currentSeat.color.composeColor(),fontSize=12.sp)};Spacer(Modifier.weight(1f));CoinPill(balance)}
        PlayerStrip(game)
        Box(Modifier.fillMaxWidth().padding(horizontal=8.dp)){LudoBoardCanvas(game,ui.hop,ui.burst,ui.landing,Modifier.fillMaxWidth(),vm::moveToken)}
        Text(game.statusMessage,Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=7.dp),color=Color.White,textAlign=androidx.compose.ui.text.style.TextAlign.Center,fontSize=13.sp,maxLines=2)
        if(ui.diceInputMode==DiceInputMode.ROLLER)RollerDicePanel(game.phase,ui.lastDiceShown,ui.diceAnim?.face,vm::rollDice)else ManualDicePanel(game.phase,vm::enterDice)
        Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal=8.dp,vertical=5.dp),horizontalArrangement=Arrangement.SpaceEvenly){ControlButton(Icons.Default.Menu,"MENU"){exitConfirm=true};ControlButton(Icons.Default.Undo,"UNDO",ui.canUndo){vm.undo()};ControlButton(Icons.Default.Redo,"REDO",ui.canRedo){vm.redo()};ControlButton(Icons.Default.Settings,"SETTINGS"){settings=true}}
    }
    ConfettiOverlay(ui.homeConfetti?.id?:ui.prize?.id,ui.homeConfetti?.color?:ui.celebrating,Modifier.fillMaxSize())
    ui.celebrating?.let{color->Box(Modifier.fillMaxSize().background(Color.Black.copy(.72f)),contentAlignment=Alignment.Center){GoldCard(Modifier.padding(28.dp)){Text("🏆",fontSize=64.sp,modifier=Modifier.align(Alignment.CenterHorizontally));GoldTitle("VICTORY!",Modifier.align(Alignment.CenterHorizontally));Text("${color.displayName} conquers the board",color=Color.White,modifier=Modifier.align(Alignment.CenterHorizontally));ui.prize?.let{Text("+${it.amount} COINS",color=LudoColors.Gold,fontWeight=FontWeight.Black,fontSize=22.sp,modifier=Modifier.align(Alignment.CenterHorizontally))};Spacer(Modifier.height(15.dp));GlowGreenButton("BACK TO LOBBY",{vm.quitMatch();onExit()},Modifier.fillMaxWidth())}}}
    if(settings)SettingsDialog(sfx,bgm,haptics,{vm.setSfxVolume(it)},{vm.setBgmVolume(it)},{vm.setHapticsEnabled(it)}){settings=false}
    if(exitConfirm)AlertDialog(onDismissRequest={exitConfirm=false},title={Text("Leave match?")},text={Text("Your current match will be closed.")},confirmButton={TextButton(onClick={vm.quitMatch();onExit()}){Text("LEAVE")}},dismissButton={TextButton(onClick={exitConfirm=false}){Text("STAY")}})
    }
}
@Composable private fun PlayerStrip(game:GameState){Row(Modifier.fillMaxWidth().padding(horizontal=10.dp,vertical=4.dp),horizontalArrangement=Arrangement.spacedBy(5.dp)){game.seats.forEachIndexed{i,seat->val active=i==game.currentSeatIndex;Column(Modifier.weight(1f).background(if(active)seat.color.composeColor().copy(.28f)else Color.White.copy(.05f),RoundedCornerShape(9.dp)).padding(6.dp),horizontalAlignment=Alignment.CenterHorizontally){PawnIcon(seat.color,Modifier.size(24.dp),active);Text(seat.name,color=Color.White,fontSize=9.sp,maxLines=1);Text("${game.scores[seat.color]?:0} pts",color=LudoColors.Gold,fontSize=9.sp)}}}}
@Composable private fun RollerDicePanel(phase:TurnPhase,last:Int?,anim:Int?,roll:()->Unit){val enabled=phase==TurnPhase.AWAIT_DICE;Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally){Box(Modifier.size(72.dp).background(Color.White,RoundedCornerShape(14.dp)).clickable(enabled=enabled,onClick=roll),contentAlignment=Alignment.Center){DiceFace(anim?:last?:6)};Text(if(enabled)"TAP TO ROLL" else "WAIT FOR MOVE",color=if(enabled)LudoColors.Gold else Color.Gray,fontWeight=FontWeight.Bold,fontSize=11.sp)}}
@Composable private fun DiceFace(value:Int){Canvas(Modifier.fillMaxSize().padding(10.dp)){val pts=when(value){1->listOf(.5f to .5f);2->listOf(.28f to .28f,.72f to .72f);3->listOf(.25f to .25f,.5f to .5f,.75f to .75f);4->listOf(.25f to .25f,.75f to .25f,.25f to .75f,.75f to .75f);5->listOf(.25f to .25f,.75f to .25f,.5f to .5f,.25f to .75f,.75f to .75f);else->listOf(.25f to .22f,.75f to .22f,.25f to .5f,.75f to .5f,.25f to .78f,.75f to .78f)};pts.forEach{drawCircle(Color(0xFF071B3E),size.width*.085f,androidx.compose.ui.geometry.Offset(size.width*it.first,size.height*it.second))}}}
@Composable private fun ManualDicePanel(phase:TurnPhase,enter:(Int)->Unit){Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally){Text("PHYSICAL DICE INPUT",color=LudoColors.Gold,fontSize=11.sp,fontWeight=FontWeight.Bold);Row(Modifier.fillMaxWidth().padding(horizontal=10.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)){(1..6).forEach{n->Box(Modifier.weight(1f).aspectRatio(1f).background(if(phase==TurnPhase.AWAIT_DICE)Color.White else Color.Gray,RoundedCornerShape(9.dp)).clickable(enabled=phase==TurnPhase.AWAIT_DICE){enter(n)},contentAlignment=Alignment.Center){Text("$n",color=LudoColors.Navy,fontWeight=FontWeight.Black,fontSize=18.sp)}}}}}
@Composable private fun ControlButton(icon:androidx.compose.ui.graphics.vector.ImageVector,label:String,enabled:Boolean=true,onClick:()->Unit){Column(Modifier.clickable(enabled=enabled,onClick=onClick).padding(4.dp),horizontalAlignment=Alignment.CenterHorizontally){Icon(icon,null,tint=if(enabled)LudoColors.Gold else Color.Gray,modifier=Modifier.size(20.dp));Text(label,color=if(enabled)Color.White else Color.Gray,fontSize=8.sp)}}
@Composable private fun SettingsDialog(sfx:Float,bgm:Float,haptics:Boolean,onSfx:(Float)->Unit,onBgm:(Float)->Unit,onHaptics:(Boolean)->Unit,onDismiss:()->Unit){Dialog(onDismissRequest=onDismiss){GoldCard(Modifier.fillMaxWidth()){GoldTitle("SETTINGS");Text("Sound effects",color=Color.White);Slider(sfx,onSfx);Text("Ambient music",color=Color.White);Slider(bgm,onBgm);Row(verticalAlignment=Alignment.CenterVertically){Text("Haptics",color=Color.White,modifier=Modifier.weight(1f));Switch(haptics,onHaptics)};TextButton(onClick=onDismiss,modifier=Modifier.align(Alignment.End)){Text("DONE")}}}}
