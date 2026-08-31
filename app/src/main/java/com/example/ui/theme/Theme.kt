package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = Color(0xFF002229),
    primaryContainer = Color(0xFF003642),
    onPrimaryContainer = JarvisCyanLight,
    secondary = JarvisBlue,
    onSecondary = Color.White,
    secondaryContainer = JarvisBlueContainer,
    onSecondaryContainer = Color(0xFFD1E4FF),
    tertiary = JarvisGold,
    onTertiary = Color(0xFF2E2000),
    tertiaryContainer = Color(0xFF4C3600),
    onTertiaryContainer = Color(0xFFFFDF9E),
    background = JarvisObsidian,
    onBackground = JarvisTextPrimary,
    surface = JarvisCardBg,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisSurfaceVariant,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisCardBorder,
    outlineVariant = JarvisCardBorder.copy(alpha = 0.5f),
    error = JarvisRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Jarvis defaults to sleek high-tech dark theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun JarvisTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MyApplicationTheme(darkTheme = darkTheme, content = content)
}
