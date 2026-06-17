package com.ludolegends.game.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ============================================================
// Premium typography — system SansSerif with bold weight stack
// Matches the reference's bold geometric sans-serif look
// ============================================================

val DisplayFamily = FontFamily.SansSerif
val BodyFamily    = FontFamily.SansSerif

val LudoTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplayFamily, fontWeight = FontWeight.W900,
        fontSize = 36.sp, letterSpacing = 0.5.sp, color = TextPrimary
    ),
    displayMedium = TextStyle(
        fontFamily = DisplayFamily, fontWeight = FontWeight.W800,
        fontSize = 28.sp, letterSpacing = 0.4.sp, color = TextPrimary
    ),
    displaySmall = TextStyle(
        fontFamily = DisplayFamily, fontWeight = FontWeight.W700,
        fontSize = 22.sp, letterSpacing = 0.3.sp, color = TextPrimary
    ),
    headlineLarge = TextStyle(
        fontFamily = DisplayFamily, fontWeight = FontWeight.W800,
        fontSize = 26.sp, color = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFamily, fontWeight = FontWeight.W700,
        fontSize = 20.sp, color = TextPrimary
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFamily, fontWeight = FontWeight.W700,
        fontSize = 18.sp, color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.W600,
        fontSize = 18.sp, color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.W600,
        fontSize = 16.sp, color = TextPrimary
    ),
    titleSmall = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.W500,
        fontSize = 14.sp, color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.W400,
        fontSize = 16.sp, color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.W400,
        fontSize = 14.sp, color = TextSecondary
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.W400,
        fontSize = 12.sp, color = TextTertiary
    ),
    labelLarge = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.W600,
        fontSize = 14.sp, letterSpacing = 0.5.sp, color = TextPrimary
    ),
    labelMedium = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.W500,
        fontSize = 12.sp, letterSpacing = 0.5.sp, color = TextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = BodyFamily, fontWeight = FontWeight.W500,
        fontSize = 10.sp, letterSpacing = 0.5.sp, color = TextTertiary
    )
)
