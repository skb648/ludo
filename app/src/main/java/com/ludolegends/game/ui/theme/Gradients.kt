package com.ludolegends.game.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================
// Premium gradient brushes — used across board, cards, tokens, dice
// ============================================================

/** Luxury dark sapphire radial background. */
fun sapphireRadialBrush(center: Offset = Offset(0.5f, 0.4f), radius: Float = 1400f): Brush =
    Brush.radialGradient(
        colors = listOf(SapphireRaised, SapphireBase, SapphireDeep),
        center = center,
        radius = radius
    )

/** Vertical gold trim gradient for borders & dividers. */
val GoldTrimGradient: Brush = Brush.linearGradient(
    colors = listOf(GoldBright, GoldPure, GoldDeep, GoldPure, GoldBright)
)

/** Horizontal gold gradient for buttons & headlines. */
val GoldHorizontalGradient: Brush = Brush.horizontalGradient(
    colors = listOf(GoldBright, GoldPure, GoldDeep)
)

/** Diagonal gold gradient for premium fills. */
val GoldDiagonalGradient: Brush = Brush.linearGradient(
    colors = listOf(GoldBright, GoldDeep),
    start = Offset(0f, 0f),
    end = Offset(1f, 1f)
)

/** Gold inner-glow ring gradient (for token highlight). */
fun goldRingGradient(): Brush = Brush.sweepGradient(
    colors = listOf(GoldBright, GoldDeep, GoldDark, GoldDeep, GoldBright)
)

/** Dark navy card gradient — used for primary layout cards. */
val SapphireCardGradient: Brush = Brush.linearGradient(
    colors = listOf(SapphireRaised, SapphireMid, SapphireBase),
    start = Offset(0f, 0f),
    end = Offset(1f, 1f)
)

/** Dice rectangle glow gradient. */
val DiceGlowGradient: Brush = Brush.radialGradient(
    colors = listOf(SapphireGlow, SapphireRaised, SapphireMid)
)

/** Action-green START button gradient. */
val ActionGreenGradient: Brush = Brush.linearGradient(
    colors = listOf(ActionGreenBright, ActionGreen),
    start = Offset(0f, 0f),
    end = Offset(1f, 1f)
)

/** Player token radial gradients — light center → dark edge */
fun tokenGradient(playerLight: Color, playerDark: Color): Brush =
    Brush.radialGradient(
        colors = listOf(playerLight, playerLight, playerDark),
        center = Offset(0.35f, 0.30f),
        radius = 0.85f
    )

/** Glossy dome highlight for premium 3D pawn top. */
val TokenGlossHighlight: Brush = Brush.radialGradient(
    colors = listOf(Color.White.copy(alpha = 0.85f), Color.White.copy(alpha = 0.0f)),
    center = Offset(0.35f, 0.25f),
    radius = 0.35f
)

/** Board frame gold bevel gradient. */
val BoardFrameGradient: Brush = Brush.linearGradient(
    colors = listOf(GoldBright, GoldDeep, GoldDark, GoldDeep, GoldBright),
    start = Offset(0f, 0f),
    end = Offset(1f, 1f)
)
