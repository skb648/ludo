// Ludo Legends v5.0 — complete source sync · 2026-07-10
package com.ludolegends.game.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================
// Material 3 dark color scheme tuned for sapphire navy base
// ============================================================
private val LudoColorScheme = darkColorScheme(
    primary = GoldBright,
    onPrimary = SapphireDeep,
    primaryContainer = GoldDeep,
    onPrimaryContainer = SapphireBase,
    secondary = SapphireGlow,
    onSecondary = TextPrimary,
    secondaryContainer = SapphireRaised,
    onSecondaryContainer = TextPrimary,
    tertiary = ActionGreen,
    onTertiary = SapphireDeep,
    tertiaryContainer = ActionGreenBright,
    onTertiaryContainer = SapphireDeep,
    background = SapphireBase,
    onBackground = TextPrimary,
    surface = SapphireMid,
    onSurface = TextPrimary,
    surfaceVariant = SapphireRaised,
    onSurfaceVariant = TextSecondary,
    surfaceTint = GoldBright,
    outline = GoldDeep,
    outlineVariant = DividerLine,
    error = PlayerRed,
    onError = TextPrimary,
    errorContainer = PlayerRedDark,
    onErrorContainer = TextPrimary,
    inverseSurface = GoldBright,
    inverseOnSurface = SapphireDeep,
    inversePrimary = SapphireBase,
    scrim = SapphireDeep
)

@Composable
fun LudoLegendsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LudoColorScheme,
        typography = LudoTypography,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(SapphireRaised, SapphireBase, SapphireDeep),
                            radius = 1400f
                        )
                    )
            ) { content() }
        }
    )
}
