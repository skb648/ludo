package com.ludolegends.game.ui.board

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.ludolegends.game.engine.PlayerColor
import com.ludolegends.game.ui.components.composeColor
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable fun ConfettiOverlay(trigger:Long?,color:PlayerColor?,modifier:Modifier=Modifier){var progress by remember(trigger){mutableFloatStateOf(0f)};LaunchedEffect(trigger){if(trigger!=null){progress=0f;repeat(45){progress=it/44f;delay(24)}}};if(trigger!=null&&progress<1f){val pieces=remember(trigger){List(52){Triple(Random.nextFloat(),Random.nextFloat(),Random.nextFloat())}};Canvas(modifier){pieces.forEachIndexed{i,p->val x=(p.first+(.5f-p.second)*progress).mod(1f)*size.width;val y=(p.third*.15f+progress*(.7f+p.second))*size.height;drawRect(listOf(Color.Yellow,Color.White,color?.composeColor()?:Color.Cyan,Color.Magenta)[i%4],Offset(x,y),androidx.compose.ui.geometry.Size(7f,13f))}}}}
