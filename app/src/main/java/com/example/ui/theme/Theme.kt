package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun LinklyTheme(
    accentColor: Color = Color(0xFF1E88E5), // default blue
    content: @Composable () -> Unit,
) {
    val dynamicDarkScheme = darkColorScheme(
        primary = accentColor,
        onPrimary = Color.White,
        primaryContainer = accentColor.copy(alpha = 0.2f),
        secondary = accentColor,
        background = CharcoalBackground,
        onBackground = TextPrimary,
        surface = CharcoalBackground,
        onSurface = TextPrimary,
        surfaceVariant = CardBackground,
        onSurfaceVariant = TextSecondary,
        outline = CardBorder
    )

    MaterialTheme(colorScheme = dynamicDarkScheme, typography = Typography, content = content)
}
