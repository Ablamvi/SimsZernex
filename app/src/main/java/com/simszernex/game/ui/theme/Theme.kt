package com.simszernex.game.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7C4DFF),
    onPrimary = Color.White,
    secondary = Color(0xFF00E5FF),
    onSecondary = Color.Black,
    background = Color(0xFF0D0B1A),
    onBackground = Color(0xFFE8E6F0),
    surface = Color(0xFF1A1730),
    onSurface = Color(0xFFE8E6F0),
    surfaceVariant = Color(0xFF2A2545),
    tertiary = Color(0xFFFF6B9D)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF5E35B1),
    onPrimary = Color.White,
    secondary = Color(0xFF00B8D4),
    background = Color(0xFFF5F3FF),
    onBackground = Color(0xFF1A1730),
    surface = Color.White,
    onSurface = Color(0xFF1A1730)
)

@Composable
fun SimsZernexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
