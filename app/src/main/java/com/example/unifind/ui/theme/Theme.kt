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
    primary = Color(0xFF2B3FD6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDDE3FF),
    onPrimaryContainer = Color(0xFF0A1A7A),
    secondary = Color(0xFF00A6A6),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCFF3F1),
    onSecondaryContainer = Color(0xFF003737),
    background = Color(0xFFF7F8FC),
    onBackground = Color(0xFF191C20),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFE6E9F5),
    onSurfaceVariant = Color(0xFF43474E),
    outline = Color(0xFF74777F)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFAEB9FF),
    onPrimary = Color(0xFF0A1A7A),
    primaryContainer = Color(0xFF1D2BA8),
    onPrimaryContainer = Color(0xFFDDE3FF),
    secondary = Color(0xFF5ED8D3),
    onSecondary = Color(0xFF003737),
    secondaryContainer = Color(0xFF004F4F),
    onSecondaryContainer = Color(0xFFCFF3F1),
    background = Color(0xFF0E1220),
    onBackground = Color(0xFFE2E2E9),
    surface = Color(0xFF151A2B),
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xFF232A42),
    onSurfaceVariant = Color(0xFFC3C6CF),
    outline = Color(0xFF8D9099)
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
