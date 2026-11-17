package com.example.unifind.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF003366),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001B3E),
    secondary = Color(0xFF004D80),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD2E4FF),
    onSecondaryContainer = Color(0xFF001D36),
    tertiary = Color(0xFF006689),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC3E8FF),
    onTertiaryContainer = Color(0xFF001E2C),
    background = Color(0xFFF8F9FF),
    onBackground = Color(0xFF191C20),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFE0E2EC),
    onSurfaceVariant = Color(0xFF43474E),
    outline = Color(0xFF74777F)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA8C8FF),
    onPrimary = Color(0xFF003062),
    primaryContainer = Color(0xFF004789),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = Color(0xFFA0C8FF),
    onSecondary = Color(0xFF00325B),
    secondaryContainer = Color(0xFF00497E),
    onSecondaryContainer = Color(0xFFD2E4FF),
    tertiary = Color(0xFF79D1FF),
    onTertiary = Color(0xFF003549),
    tertiaryContainer = Color(0xFF004C68),
    onTertiaryContainer = Color(0xFFC3E8FF),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE1E2E8),
    surface = Color(0xFF1A1C22),
    onSurface = Color(0xFFE1E2E8),
    surfaceVariant = Color(0xFF43474E),
    onSurfaceVariant = Color(0xFFC4C6CF),
    outline = Color(0xFF8E9099)
)

/**
 * OOP Concept: Encapsulation / Theme Abstraction
 * Wraps the Material 3 color scheme and typography for consistent app styling.
 */
@Composable
fun UniFindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
