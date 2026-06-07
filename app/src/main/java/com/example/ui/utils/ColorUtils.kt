package com.example.ui.utils

import androidx.compose.ui.graphics.Color

fun String?.toColor(default: Color = Color.Gray): Color {
    if (this == null) return default
    return try {
        Color(android.graphics.Color.parseColor(this))
    } catch (e: Exception) {
        default
    }
}
