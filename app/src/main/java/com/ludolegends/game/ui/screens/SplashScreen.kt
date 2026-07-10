package com.ludolegends.game.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludolegends.game.ui.theme.LudoColors
import kotlinx.coroutines.delay

@Composable fun SplashScreen(onFinished:()->Unit){var progress by remember{mutableFloatStateOf(0f)};LaunchedEffect(Unit){repeat(101){progress=it/100f;delay(12)};onFinished()};val inf=rememberInfiniteTransition(label="dice");val turn by inf.animateFloat(-5f,5f,infiniteRepeatable(tween(700),RepeatMode.Reverse),label="turn");Box(Modifier.fillMaxSize().background(LudoColors.Background),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){GoldenDice(Modifier.size(118.dp).rotate(turn));Spacer(Modifier.height(28.dp));Text("LUDO",color=Color.White,fontWeight=FontWeight.Black,fontSize=48.sp);Text("LEGENDS",color=LudoColors.Gold,fontWeight=FontWeight.Black,fontSize=30.sp,letterSpacing=8.sp);Spacer(Modifier.height(54.dp));LinearProgressIndicator(progress={progress},Modifier.width(230.dp).height(8.dp),color=LudoColors.Gold,trackColor=Color.White.copy(.15f));Spacer(Modifier.height(10.dp));Text("LOADING  ${(progress*100).toInt()}%",color=Color.White.copy(.75f),fontSize=12.sp)}}}
@Composable private fun GoldenDice(modifier:Modifier){Canvas(modifier){drawRoundRect(Brush.linearGradient(listOf(Color(0xFFFFF3A0),LudoColors.Gold,Color(0xFFB57C00))),cornerRadius=androidx.compose.ui.geometry.CornerRadius(size.width*.2f));listOf(.28f to .28f,.72f to .28f,.5f to .5f,.28f to .72f,.72f to .72f).forEach{drawCircle(Color(0xFF061837),size.width*.07f,androidx.compose.ui.geometry.Offset(size.width*it.first,size.height*it.second))}}}
