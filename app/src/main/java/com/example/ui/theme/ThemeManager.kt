package com.example.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeManager(context: Context) {
    private val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    
    // Default blue: 0xFF1E88E5
    private val _accentColor = MutableStateFlow(Color(prefs.getLong("accent_color", 0xFF1E88E5)))
    val accentColor: StateFlow<Color> = _accentColor.asStateFlow()

    fun setAccentColor(color: Color) {
        prefs.edit().putLong("accent_color", color.value.toLong()).apply()
        _accentColor.value = color
    }
}
