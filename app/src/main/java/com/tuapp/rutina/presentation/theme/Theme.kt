package com.tuapp.rutina.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme

val Background = Color(0xFF0F0F0F)
val CardBackground = Color(0xFF1A1A1A)
val CardBorder = Color(0xFF2A2A2A)
val Accent = Color(0xFFFF6B35)
val Teal = Color(0xFF4ECDC4)
val TextPrimary = Color(0xFFE0E0E0)
val TextSecondary = Color(0xFF666666)

private val RutinaColors = ColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    primaryContainer = Accent,
    onPrimaryContainer = Color.White,
    secondary = Teal,
    onSecondary = Background,
    background = Background,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = CardBackground,
    surfaceContainerLow = CardBackground,
    surfaceContainerHigh = CardBackground,
    outline = CardBorder,
    outlineVariant = CardBorder,
)

@Composable
fun RutinaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RutinaColors,
        content = content
    )
}
