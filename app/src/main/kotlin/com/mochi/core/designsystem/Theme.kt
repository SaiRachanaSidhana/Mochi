package com.mochi.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors =
    lightColorScheme(
        primary = MochiAccent,
        onPrimary = Color.White,
        primaryContainer = AccentContainerLight,
        onPrimaryContainer = MochiInk,
        secondary = Color(0xFF2E7D5C),
        onSecondary = Color.White,
        secondaryContainer = MochiGreen,
        onSecondaryContainer = MochiInk,
        background = MochiBackground,
        onBackground = MochiInk,
        surface = Color.White,
        onSurface = MochiInk,
        surfaceVariant = SurfaceVariantLight,
        onSurfaceVariant = OnSurfaceVariantLight,
        outline = OutlineLight,
    )

private val DarkColors =
    darkColorScheme(
        primary = AccentDark,
        onPrimary = MochiInk,
        primaryContainer = AccentContainerDark,
        onPrimaryContainer = OnBackgroundDark,
        secondary = MochiGreen,
        onSecondary = MochiInk,
        secondaryContainer = GreenContainerDark,
        onSecondaryContainer = OnBackgroundDark,
        background = BackgroundDark,
        onBackground = OnBackgroundDark,
        surface = SurfaceDark,
        onSurface = OnBackgroundDark,
        surfaceVariant = SurfaceVariantDark,
        onSurfaceVariant = OnSurfaceVariantDark,
        outline = OutlineDark,
    )

@Composable
fun MochiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MochiTypography,
        content = content,
    )
}
