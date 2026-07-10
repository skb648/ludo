package com.ludolegends.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludolegends.game.ui.components.*
import com.ludolegends.game.ui.theme.LudoColors
import com.ludolegends.game.vm.*

@Composable fun LobbyScreen(balance:Int,onStart:(MatchConfig)->Unit){var selected by remember{mutableStateOf<DiceInputMode?>(null)};Box(Modifier.fillMaxSize().background(LudoColors.Background)){Column(Modifier.fillMaxSize().systemBarsPadding()){Row(Modifier.fillMaxWidth().padding(18.dp),verticalAlignment=Alignment.CenterVertically){GoldTitle("LUDO LEGENDS");Spacer(Modifier.weight(1f));CoinPill(balance)};Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal=18.dp)){Text("CHOOSE YOUR ARENA",color=Color.White.copy(.65f),letterSpacing=2.sp,fontSize=12.sp);Spacer(Modifier.height(16.dp));ModeCard("LOCAL PASS & PLAY","Animated fair 3D dice • all human seats",Icons.Default.Casino){selected=DiceInputMode.ROLLER};Spacer(Modifier.height(14.dp));ModeCard("PLAY WITH FRIENDS","Manual 1–6 physical dice injector",Icons.Default.Groups){selected=DiceInputMode.MANUAL};Spacer(Modifier.height(14.dp));GoldCard(Modifier.fillMaxWidth()){Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Default.Public,null,tint=LudoColors.Gold,modifier=Modifier.size(45.dp));Spacer(Modifier.width(15.dp));Column{Text("ONLINE ARENA",color=Color.White,fontWeight=FontWeight.Bold,fontSize=18.sp);Text("Coming soon",color=Color.White.copy(.55f))}}}};Row(Modifier.fillMaxWidth().navigationBarsPadding().height(62.dp).background(Color(0xE9031028)),horizontalArrangement=Arrangement.SpaceEvenly,verticalAlignment=Alignment.CenterVertically){listOf(Icons.Default.Home to "HOME",Icons.Default.EmojiEvents to "LEAGUE",Icons.Default.Person to "PROFILE").forEach{(icon,label)->Column(horizontalAlignment=Alignment.CenterHorizontally){Icon(icon,null,tint=if(label=="HOME")LudoColors.Gold else Color.Gray);Text(label,color=Color.White.copy(.7f),fontSize=10.sp)}}}};selected?.let{SetupSheet(it,onDismiss={selected=null},onStart={onStart(it);selected=null})}}
}
@Composable private fun ModeCard(title:String,subtitle:String,icon:androidx.compose.ui.graphics.vector.ImageVector,onClick:()->Unit){GoldCard(Modifier.fillMaxWidth().clickable(onClick=onClick)){Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(74.dp).background(Color(0x3318A6FF),androidx.compose.foundation.shape.CircleShape),contentAlignment=Alignment.Center){Icon(icon,null,tint=LudoColors.Gold,modifier=Modifier.size(42.dp))};Spacer(Modifier.width(15.dp));Column(Modifier.weight(1f)){Text(title,color=Color.White,fontSize=19.sp,fontWeight=FontWeight.Black);Spacer(Modifier.height(4.dp));Text(subtitle,color=Color.White.copy(.65f),fontSize=12.sp)};Icon(Icons.Default.ChevronRight,null,tint=LudoColors.Gold)}}}
