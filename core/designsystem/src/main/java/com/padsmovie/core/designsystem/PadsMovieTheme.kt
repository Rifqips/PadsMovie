package com.padsmovie.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    background = Color.White,
    onBackground = Color(0xFF17243B),
    surface = Color(0xFFF4F8FF),
    onSurface = Color(0xFF17243B),
    secondaryContainer = Color(0xFFDBEAFE),
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF0B1220),
    background = Color(0xFF0B1220),
    onBackground = Color(0xFFE5EEFF),
    surface = Color(0xFF111C2E),
    onSurface = Color(0xFFE5EEFF),
    secondaryContainer = Color(0xFF1E3A5F),
)

@Composable
fun PadsMovieTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
}
