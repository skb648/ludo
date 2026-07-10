package com.ludolegends.game.ui.board

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.ludolegends.game.engine.*
import com.ludolegends.game.ui.components.composeColor
import com.ludolegends.game.vm.*
import kotlin.math.hypot

@Composable fun LudoBoardCanvas(game:GameState,hop:HopFrame?,burst:CaptureBurst?,landing:LandingPulse?,modifier:Modifier=Modifier,onTokenTap:(Int)->Unit){
    val transition=rememberInfiniteTransition(label="turn");val pulse by transition.animateFloat(.35f,1f,infiniteRepeatable(tween(850),RepeatMode.Reverse),label="pulse")
    Canvas(modifier.aspectRatio(1f).pointerInput(game.movableTokenIds){detectTapGestures{tap->val cell=size.width/15f;val best=game.tokens.filter{it.id in game.movableTokenIds}.minByOrNull{val c=BoardGeometry.cellFor(it.color,it.pos,it.id%4);hypot(tap.x-(c.col+.5f)*cell,tap.y-(c.row+.5f)*cell)};if(best!=null){val c=BoardGeometry.cellFor(best.color,best.pos,best.id%4);if(hypot(tap.x-(c.col+.5f)*cell,tap.y-(c.row+.5f)*cell)<cell*1.2f)onTokenTap(best.id)}}}){
        val s=size.width/15f;drawRect(Color(0xFFFFF7DF));
        drawBase(PlayerColor.RED,0f,0f,s);drawBase(PlayerColor.GREEN,9*s,0f,s);drawBase(PlayerColor.BLUE,0f,9*s,s);drawBase(PlayerColor.YELLOW,9*s,9*s,s)
        BoardGeometry.MAIN_TRACK.forEachIndexed{i,c->drawCell(c,s,if(c in BoardGeometry.STAR_CELLS)Color(0xFFFFE7A0) else Color.White);if(c in BoardGeometry.STAR_CELLS)drawStar(center(c,s),s*.22f,LudoRules.SAFE_ABSOLUTE_CELLS.contains(i))}
        PlayerColor.entries.forEach{color->BoardGeometry.HOME_COLUMNS.getValue(color).forEach{drawCell(it,s,color.composeColor().copy(.78f))};BoardGeometry.START_CELLS[color]?.let{drawCell(it,s,color.composeColor())}}
        drawCenter(s)
        val visible=game.tokens.filter{hop?.tokenId!=it.id};visible.groupBy{BoardGeometry.cellFor(it.color,it.pos,it.id%4)}.forEach{(cell,tokens)->tokens.forEachIndexed{i,t->val off=stackOffset(i,tokens.size,s);drawPawn(center(cell,s)+off,s*.35f,t.color.composeColor(),t.id in game.movableTokenIds,pulse)}}
        hop?.let{val x=(it.from.col+(it.to.col-it.from.col)*it.progress+.5f)*s;val y=(it.from.row+(it.to.row-it.from.row)*it.progress+.5f)*s-it.lift*s*.7f;drawPawn(Offset(x,y),s*.38f,game.token(it.tokenId).color.composeColor(),false,1f)}
        burst?.let{val c=center(it.cell,s);repeat(16){i->val a=i/16f*6.283f;val r=s*(.3f+(i%4)*.18f);drawCircle(listOf(Color.White,Color.Yellow,it.color.composeColor())[i%3],s*.08f,c+Offset(kotlin.math.cos(a)*r,kotlin.math.sin(a)*r))}}
    }
}
private fun DrawScope.drawBase(color:PlayerColor,x:Float,y:Float,s:Float){drawRect(color.composeColor(),Offset(x,y),androidx.compose.ui.geometry.Size(6*s,6*s));drawRoundRect(Color.White,Offset(x+s,y+s),androidx.compose.ui.geometry.Size(4*s,4*s),androidx.compose.ui.geometry.CornerRadius(s*.25f));listOf(Offset(x+2*s,y+2*s),Offset(x+4*s,y+2*s),Offset(x+2*s,y+4*s),Offset(x+4*s,y+4*s)).forEach{drawCircle(color.composeColor().copy(.2f),s*.46f,it);drawCircle(color.composeColor(),s*.46f,it,style=Stroke(s*.09f))}}
private fun DrawScope.drawCell(c:BoardGeometry.Cell,s:Float,color:Color){drawRect(color,Offset(c.col*s,c.row*s),androidx.compose.ui.geometry.Size(s,s));drawRect(Color(0xFF5D6370),Offset(c.col*s,c.row*s),androidx.compose.ui.geometry.Size(s,s),style=Stroke(s*.025f))}
private fun DrawScope.drawCenter(s:Float){val c=Offset(7.5f*s,7.5f*s);val p1=Path().apply{moveTo(6*s,6*s);lineTo(9*s,6*s);lineTo(c.x,c.y);close()};val p2=Path().apply{moveTo(9*s,6*s);lineTo(9*s,9*s);lineTo(c.x,c.y);close()};val p3=Path().apply{moveTo(9*s,9*s);lineTo(6*s,9*s);lineTo(c.x,c.y);close()};val p4=Path().apply{moveTo(6*s,9*s);lineTo(6*s,6*s);lineTo(c.x,c.y);close()};drawPath(p1,PlayerColor.GREEN.composeColor());drawPath(p2,PlayerColor.YELLOW.composeColor());drawPath(p3,PlayerColor.BLUE.composeColor());drawPath(p4,PlayerColor.RED.composeColor())}
private fun DrawScope.drawPawn(c:Offset,r:Float,color:Color,glow:Boolean,pulse:Float){if(glow)drawCircle(color.copy(.18f+.18f*pulse),r*1.7f,c);drawCircle(Color.Black.copy(.35f),r,c+Offset(0f,r*.55f));drawCircle(color,r,c);drawCircle(Color.White.copy(.65f),r*.26f,c+Offset(-r*.28f,-r*.3f));drawCircle(Color.White.copy(.8f),r, c, style=Stroke(r*.12f))}
private fun DrawScope.drawStar(c:Offset,r:Float,safe:Boolean){val p=Path();repeat(10){i->val a=-1.5708f+i*0.6283f;val rr=if(i%2==0)r else r*.45f;val x=c.x+kotlin.math.cos(a)*rr;val y=c.y+kotlin.math.sin(a)*rr;if(i==0)p.moveTo(x,y)else p.lineTo(x,y)};p.close();drawPath(p,if(safe)Color(0xFFFFB300)else Color.Gray)}
private fun center(c:BoardGeometry.Cell,s:Float)=Offset((c.col+.5f)*s,(c.row+.5f)*s)
private fun stackOffset(i:Int,n:Int,s:Float):Offset=if(n<=1)Offset.Zero else Offset((if(i%2==0)-1 else 1)*s*.15f,(if(i/2==0)-1 else 1)*s*.15f)
