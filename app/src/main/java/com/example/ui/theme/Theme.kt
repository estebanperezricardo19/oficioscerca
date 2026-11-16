package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryNavy,
    onPrimary = SurfaceWhite,
    primaryContainer = SecondaryTeal,
    onPrimaryContainer = SurfaceWhite,
    secondary = SecondaryTeal,
    onSecondary = SurfaceWhite,
    background = BackgroundSoft,
    surface = SurfaceWhite,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle
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

/**
 * High-contrast, crystal-clear color configuration for all input fields in OficiosCerca.
 * Prevents white-on-white text, invisible cursor, and low-contrast placeholders.
 */
@Composable
fun oficiosTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    disabledTextColor = TextSecondary,
    focusedContainerColor = SurfaceWhite,
    unfocusedContainerColor = SurfaceWhite,
    disabledContainerColor = Color(0xFFF1F5F7),
    cursorColor = SecondaryTeal,
    errorCursorColor = Color(0xFFB00020),
    focusedBorderColor = SecondaryTeal,
    unfocusedBorderColor = BorderSubtle,
    disabledBorderColor = Color(0xFFE0E0E0),
    errorBorderColor = Color(0xFFB00020),
    focusedLabelColor = SecondaryTeal,
    unfocusedLabelColor = TextSecondary,
    disabledLabelColor = TextSecondary,
    errorLabelColor = Color(0xFFB00020),
    focusedPlaceholderColor = TextSecondary,
    unfocusedPlaceholderColor = TextSecondary,
    disabledPlaceholderColor = TextSecondary,
    focusedLeadingIconColor = SecondaryTeal,
    unfocusedLeadingIconColor = SecondaryTeal,
    disabledLeadingIconColor = TextSecondary,
    focusedTrailingIconColor = SecondaryTeal,
    unfocusedTrailingIconColor = TextSecondary,
    disabledTrailingIconColor = TextSecondary
)

@Composable
fun OficiosCercaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // OficiosCerca has an established bright visual identity with White cards and Soft Backgrounds.
    // Ensure all typography has high contrast against light surfaces regardless of OS dark-mode setting.
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
