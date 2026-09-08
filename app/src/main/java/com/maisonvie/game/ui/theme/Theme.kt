package com.maisonvie.game.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AccentPurple = Color(0xFF7C4DFF)
private val BackgroundDark = Color(0xFF12141C)

private val MaisonVieColorScheme = darkColorScheme(
    primary = AccentPurple,
    background = BackgroundDark,
    surface = BackgroundDark
)

@Composable
fun MaisonVieTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MaisonVieColorScheme,
        typography = Typography,
        content = content
    )
}
