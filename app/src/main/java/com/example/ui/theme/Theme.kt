package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AlNurPrimaryDark,
    secondary = AlNurSecondaryDark,
    tertiary = AlNurTertiaryDark,
    background = AlNurBackgroundDark,
    surface = AlNurSurfaceDark,
    onPrimary = AlNurOnPrimaryDark,
    onSecondary = AlNurOnSecondaryDark,
    onSurface = AlNurOnSurfaceDark,
    onBackground = AlNurOnBackgroundDark
)

private val LightColorScheme = lightColorScheme(
    primary = AlNurPrimary,
    secondary = AlNurSecondary,
    tertiary = AlNurTertiary,
    background = AlNurBackground,
    surface = AlNurSurface,
    onPrimary = AlNurOnPrimary,
    onSecondary = AlNurOnSecondary,
    onSurface = AlNurOnSurface,
    onBackground = AlNurOnBackground
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
