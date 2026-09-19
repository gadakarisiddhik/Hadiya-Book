package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkEmerald,
    onPrimary = Color.White,
    primaryContainer = DarkGreen,
    onPrimaryContainer = Color.White,
    secondary = DarkGold,
    onSecondary = Color(0xFF332700),
    secondaryContainer = Color(0xFF423508),
    onSecondaryContainer = Color(0xFFFFECC0),
    tertiary = DarkGold,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorder.copy(alpha = 0.5f)
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldGreen,
    onPrimary = Color.White,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnEmeraldContainer,
    secondary = MutedGold,
    onSecondary = Color.White,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = OnGoldContainer,
    tertiary = DarkGreen,
    onTertiary = Color.White,
    background = SoftOffWhite,
    onBackground = DarkCharcoal,
    surface = CardWhite,
    onSurface = DarkCharcoal,
    surfaceVariant = Color(0xFFEEF2EF),
    onSurfaceVariant = CharcoalMuted,
    outline = CharcoalBorder,
    outlineVariant = CharcoalBorder.copy(alpha = 0.5f)
)

@Composable
fun HadiyaBookTheme(
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

// Keep legacy alias in case of template tests
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    HadiyaBookTheme(darkTheme = darkTheme, content = content)
}
