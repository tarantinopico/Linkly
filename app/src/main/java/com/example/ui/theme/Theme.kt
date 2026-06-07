package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkPremiumColorScheme = darkColorScheme(
    primary = MutedPurple,
    onPrimary = TextAccent,
    primaryContainer = MutedPurpleDark,
    secondary = MutedPurple,
    background = CharcoalBackground,
    onBackground = TextPrimary,
    surface = CharcoalBackground,
    onSurface = TextPrimary,
    surfaceVariant = CardBackground,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder
)

@Composable
fun LinklyTheme(
    content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = DarkPremiumColorScheme, typography = Typography, content = content)
}
