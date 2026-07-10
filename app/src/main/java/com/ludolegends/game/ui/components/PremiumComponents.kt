package com.ludolegends.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ludolegends.game.engine.PlayerColor
import com.ludolegends.game.ui.theme.LudoColors

fun Modifier.goldDoubleBorder(radius:Int=18)=this.border(2.dp,LudoColors.Gold,RoundedCornerShape(radius.dp)).padding(3.dp).border(1.dp,LudoColors.DeepGold.copy(.55f),RoundedCornerShape((radius-3).dp))
@Composable fun GoldCard(modifier:Modifier=Modifier,content:@Composable ColumnScope.()->Unit){Column(modifier.background(Brush.verticalGradient(listOf(Color(0xFF103E79),Color(0xFF071B3E))),RoundedCornerShape(20.dp)).goldDoubleBorder(20).padding(16.dp),content=content)}
@Composable fun GoldTitle(text:String,modifier:Modifier=Modifier){Text(text,modifier,color=LudoColors.Gold,fontSize=26.sp,fontWeight=FontWeight.Black,letterSpacing=1.sp)}
@Composable fun CoinPill(balance:Int,modifier:Modifier=Modifier){Row(modifier.background(Color(0xB3061734),CircleShape).border(1.dp,LudoColors.Gold,CircleShape).padding(horizontal=12.dp,vertical=7.dp),verticalAlignment=Alignment.CenterVertically){Text("●",color=LudoColors.Gold);Spacer(Modifier.width(6.dp));Text("%,d".format(balance),color=Color.White,fontWeight=FontWeight.Bold)}}
@Composable fun GlowGreenButton(text:String,onClick:()->Unit,modifier:Modifier=Modifier){Box(modifier.shadow(12.dp,RoundedCornerShape(16.dp),ambientColor=Color.Green,spotColor=Color.Green).background(Brush.verticalGradient(listOf(Color(0xFF55E56F),Color(0xFF168E3B))),RoundedCornerShape(16.dp)).border(2.dp,Color(0xFFB8FFC5),RoundedCornerShape(16.dp)).clickable(onClick=onClick).padding(vertical=14.dp,horizontal=24.dp),contentAlignment=Alignment.Center){Text(text,color=Color.White,fontWeight=FontWeight.Black,fontSize=18.sp)}}
@Composable fun PawnIcon(color:PlayerColor,modifier:Modifier=Modifier,glow:Boolean=false){val c=color.composeColor();Canvas(modifier){if(glow)drawCircle(c.copy(.28f),radius=size.minDimension*.65f);drawOval(Color.Black.copy(.35f),topLeft=Offset(size.width*.16f,size.height*.72f),size=androidx.compose.ui.geometry.Size(size.width*.68f,size.height*.2f));val path=Path().apply{moveTo(size.width*.30f,size.height*.7f);quadraticBezierTo(size.width*.38f,size.height*.48f,size.width*.42f,size.height*.36f);lineTo(size.width*.58f,size.height*.36f);quadraticBezierTo(size.width*.62f,size.height*.48f,size.width*.70f,size.height*.7f);close()};drawPath(path,Brush.linearGradient(listOf(c.lighten(),c,c.darken())));drawCircle(Brush.radialGradient(listOf(Color.White.copy(.8f),c),center=Offset(size.width*.4f,size.height*.22f),radius=size.width*.32f),radius=size.width*.22f,center=Offset(size.width*.5f,size.height*.27f));drawOval(c.darken(),topLeft=Offset(size.width*.20f,size.height*.66f),size=androidx.compose.ui.geometry.Size(size.width*.60f,size.height*.2f));drawOval(Color.White.copy(.3f),topLeft=Offset(size.width*.28f,size.height*.68f),size=androidx.compose.ui.geometry.Size(size.width*.30f,size.height*.06f));drawCircle(Color.White.copy(.65f),radius=size.width*.05f,center=Offset(size.width*.42f,size.height*.19f))}}
fun PlayerColor.composeColor()=when(this){PlayerColor.RED->LudoColors.Red;PlayerColor.GREEN->LudoColors.Green;PlayerColor.YELLOW->LudoColors.Yellow;PlayerColor.BLUE->LudoColors.Blue}
private fun Color.lighten()=lerp(this,Color.White,.32f);private fun Color.darken()=lerp(this,Color.Black,.30f)
