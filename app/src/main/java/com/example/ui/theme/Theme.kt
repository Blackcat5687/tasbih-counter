package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.ThemeType

private val EmeraldColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    secondary = EmeraldSecondary,
    background = EmeraldDarkBackground,
    surface = EmeraldDarkSurface,
    onPrimary = Color(0xFF022C22), // Deep emerald on light emerald
    onSecondary = Color(0xFF022C22),
    onBackground = EmeraldOnSurface,
    onSurface = EmeraldOnSurface,
    onSurfaceVariant = EmeraldOnSurfaceSecondary,
    outlineVariant = EmeraldOutline,
    primaryContainer = EmeraldPrimaryContainer,
    onPrimaryContainer = EmeraldOnPrimaryContainer,
    secondaryContainer = EmeraldActivePill,
    onSecondaryContainer = EmeraldActiveText
)

private val CelestialColorScheme = darkColorScheme(
    primary = CelestialPrimary,
    secondary = CelestialSecondary,
    background = CelestialDarkBackground,
    surface = CelestialDarkSurface,
    onPrimary = Color(0xFF381E72), // Deep purple on lavender
    onSecondary = Color(0xFF332D41),
    onBackground = CelestialOnSurface,
    onSurface = CelestialOnSurface,
    onSurfaceVariant = CelestialOnSurfaceSecondary,
    outlineVariant = CelestialOutline,
    primaryContainer = CelestialPrimaryContainer,
    onPrimaryContainer = CelestialOnPrimaryContainer,
    secondaryContainer = CelestialActivePill,
    onSecondaryContainer = CelestialActiveText
)

private val AmberColorScheme = darkColorScheme(
    primary = AmberPrimary,
    secondary = AmberSecondary,
    background = AmberDarkBackground,
    surface = AmberDarkSurface,
    onPrimary = Color(0xFF451A03), // Deep brown on light gold
    onSecondary = Color(0xFF451A03),
    onBackground = AmberOnSurface,
    onSurface = AmberOnSurface,
    onSurfaceVariant = AmberOnSurfaceSecondary,
    outlineVariant = AmberOutline,
    primaryContainer = AmberPrimaryContainer,
    onPrimaryContainer = AmberOnPrimaryContainer,
    secondaryContainer = AmberActivePill,
    onSecondaryContainer = AmberActiveText
)

@Composable
fun TasbihTheme(
    themeType: ThemeType = ThemeType.EMERALD,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeType) {
        ThemeType.EMERALD -> EmeraldColorScheme
        ThemeType.CELESTIAL -> CelestialColorScheme
        ThemeType.AMBER -> AmberColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
