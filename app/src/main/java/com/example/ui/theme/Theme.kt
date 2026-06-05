package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SecondaryGreen,
    onPrimary = DarkBackground,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = SecondaryGreen,
    secondary = PrimaryGreen,
    onSecondary = DarkBackground,
    secondaryContainer = DarkPrimaryContainer,
    tertiary = TertiaryAmber,
    onTertiary = DarkBackground,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkOnSurface
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = LightSurface,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = PrimaryGreen,
    secondary = SecondaryGreen,
    onSecondary = LightSurface,
    secondaryContainer = LightPrimaryContainer,
    tertiary = TertiaryAmber,
    onTertiary = LightSurface,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightBackground,
    onSurfaceVariant = TextEcoGray
)

@Composable
fun MyApplicationTheme(
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
