package com.ludolegends.game.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object LudoColors {
    val Navy=Color(0xFF041126); val Sapphire=Color(0xFF082C5D); val Royal=Color(0xFF0A4A92)
    val Gold=Color(0xFFFFD700); val DeepGold=Color(0xFFD4AF37); val Cream=Color(0xFFFFF4CF)
    val Red=Color(0xFFE53935); val Green=Color(0xFF22B45B); val Yellow=Color(0xFFFFC928); val Blue=Color(0xFF1976D2)
    val Background=Brush.verticalGradient(listOf(Color(0xFF020817),Navy,Sapphire))
}
private val Scheme=darkColorScheme(primary=LudoColors.Gold,secondary=LudoColors.DeepGold,background=LudoColors.Navy,surface=LudoColors.Sapphire,onPrimary=LudoColors.Navy,onBackground=Color.White,onSurface=Color.White)
@Composable fun LudoLegendsTheme(content: @Composable () -> Unit){MaterialTheme(colorScheme=Scheme,content=content)}
