package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun LinklyTheme(
    accentColor: Color = MutedPurpleDark, 
    content: @Composable () -> Unit,
) {
    val premiumDarkScheme = darkColorScheme(
        primary = accentColor,
        onPrimary = Color.White,
        primaryContainer = accentColor.copy(alpha = 0.2f),
        secondary = accentColor,
        background = CharcoalBackground, // Fallback background, but we prefer modifier gradient
        onBackground = TextPrimary,
        surface = CharcoalBackground,
        onSurface = TextPrimary,
        surfaceVariant = CardBackground,
        onSurfaceVariant = TextSecondary,
        outline = CardBorder
    )

    MaterialTheme(colorScheme = premiumDarkScheme, typography = Typography, content = content)
}
