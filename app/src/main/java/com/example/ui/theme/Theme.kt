package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = PrimaryNavy,
    primaryContainer = SecondaryTeal,
    onPrimaryContainer = Color.White,
    secondary = AccentCyan,
    onSecondary = PrimaryNavy,
    background = Color(0xFF040E2E),
    surface = Color(0xFF091F47),
    onBackground = BackgroundSoft,
    onSurface = BackgroundSoft
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryNavy,
    onPrimary = SurfaceWhite,
    primaryContainer = BackgroundSoft,
    onPrimaryContainer = PrimaryNavy,
    secondary = SecondaryTeal,
    onSecondary = SurfaceWhite,
    secondaryContainer = Color(0xFFD6F2F5),
    onSecondaryContainer = PrimaryNavy,
    tertiary = AccentCyan,
    onTertiary = PrimaryNavy,
    background = BackgroundSoft,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFE2EDF1),
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle
)

@Composable
fun OficiosCercaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
