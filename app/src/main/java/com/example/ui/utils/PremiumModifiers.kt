package com.example.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.premiumCardStyle(
    cornerRadius: Dp = 20.dp,
    containerColor: Color = Color(0xFF1A1C24),
    shadowColor: Color = Color(0x66000000),
    shadowElevation: Dp = 12.dp
) = this
    .shadow(elevation = shadowElevation, shape = RoundedCornerShape(cornerRadius), ambientColor = shadowColor, spotColor = shadowColor)
    .clip(RoundedCornerShape(cornerRadius))
    .background(containerColor)
    .drawBehind {
        // Subtle light top border for depth
        drawLine(
            color = Color(0x14FFFFFF),
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 2f
        )
    }

fun Modifier.premiumBackground() = this.background(
    brush = Brush.verticalGradient(
        colors = listOf(Color(0xFF15161C), Color(0xFF0E0F13))
    )
)
