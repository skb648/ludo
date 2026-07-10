package com.ludolegends.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ludolegends.game.engine.GameMode
import com.ludolegends.game.ui.components.GlowGreenButton
import com.ludolegends.game.ui.components.GoldTitle
import com.ludolegends.game.ui.theme.LudoColors
import com.ludolegends.game.vm.*

@Composable fun SetupSheet(inputMode:DiceInputMode,onDismiss:()->Unit,onStart:(MatchConfig)->Unit){var mode by remember{mutableStateOf(GameMode.STANDARD)};var players by remember{mutableIntStateOf(4)};Dialog(onDismissRequest=onDismiss){Column(Modifier.fillMaxWidth().background(Color(0xFF071B3E),RoundedCornerShape(24.dp)).padding(22.dp)){GoldTitle("NEW GAME");Text(if(inputMode==DiceInputMode.ROLLER)"LOCAL PASS & PLAY" else "PLAY WITH FRIENDS",color=Color.White.copy(.65f));Spacer(Modifier.height(22.dp));Text("GAME MODE",color=LudoColors.Gold,fontWeight=FontWeight.Bold);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){ModeChoice("CLASSIC",mode==GameMode.STANDARD){mode=GameMode.STANDARD};ModeChoice("2 VS 2",mode==GameMode.TEAM_2V2){mode=GameMode.TEAM_2V2;players=4}};Spacer(Modifier.height(18.dp));Text("PLAYERS",color=LudoColors.Gold,fontWeight=FontWeight.Bold);Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){(2..4).forEach{n->FilterChip(selected=players==n,onClick={if(mode==GameMode.STANDARD)players=n},label={Text("$n")})}};Spacer(Modifier.height(24.dp));GlowGreenButton("START MATCH",{onStart(MatchConfig(mode,if(mode==GameMode.TEAM_2V2)4 else players,inputMode))},Modifier.fillMaxWidth())}}
}
@Composable private fun RowScope.ModeChoice(text:String,selected:Boolean,onClick:()->Unit){Box(Modifier.weight(1f).background(if(selected)LudoColors.Royal else Color.White.copy(.07f),RoundedCornerShape(12.dp)).clickable(onClick=onClick).padding(14.dp),contentAlignment=Alignment.Center){Text(text,color=if(selected)LudoColors.Gold else Color.White,fontWeight=FontWeight.Bold)}}
