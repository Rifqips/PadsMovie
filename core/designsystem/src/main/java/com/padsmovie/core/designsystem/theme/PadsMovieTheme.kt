package com.padsmovie.core.designsystem.theme

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
    surface = Color(0xFFFFFFFF),
    surfaceContainer = Color(0xFFEDF2FA),
    surfaceVariant = Color(0xFFDEE7F4),
    onSurfaceVariant = Color(0xFF42536B),
    primaryContainer = Color(0xFFDFE9FF),
    onPrimaryContainer = Color(0xFF12376F),
    onSurface = Color(0xFF17243B),
    secondaryContainer = Color(0xFFDBEAFE),
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF0B1220),
    background = Color(0xFF0B1220),
    onBackground = Color(0xFFE5EEFF),
    surface = Color(0xFF111C2E),
    surfaceContainer = Color(0xFF18263C),
    surfaceVariant = Color(0xFF253750),
    onSurfaceVariant = Color(0xFFD0DDEF),
    primaryContainer = Color(0xFF1C3C68),
    onPrimaryContainer = Color(0xFFDBE9FF),
    onSurface = Color(0xFFE5EEFF),
    secondaryContainer = Color(0xFF1E3A5F),
)

@Composable
fun PadsMovieTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
}
