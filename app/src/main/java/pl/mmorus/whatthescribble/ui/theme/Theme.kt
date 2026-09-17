package pl.mmorus.whatthescribble.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ScribbleOrangeLight,
    onPrimary = Color(0xFF431305),
    primaryContainer = ScribbleOrangeContainerDark,
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = ScribbleTealLight,
    onSecondary = Color(0xFF003730),
    secondaryContainer = ScribbleTealContainerDark,
    onSecondaryContainer = Color(0xFFB2DFDB),
    tertiary = ScribblePurpleLight,
    onTertiary = Color(0xFF280665),
    tertiaryContainer = ScribblePurpleContainerDark,
    onTertiaryContainer = Color(0xFFEADBFF),
    background = CanvasPaperDark,
    onBackground = Color(0xFFE9E5EF),
    surface = CanvasPaperCardDark,
    onSurface = Color(0xFFE9E5EF),
    surfaceVariant = Color(0xFF332D3F),
    onSurfaceVariant = Color(0xFFCAC3D4),
    outline = CanvasPaperBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = ScribbleOrange,
    onPrimary = Color.White,
    primaryContainer = ScribbleOrangeContainerLight,
    onPrimaryContainer = ScribbleOrangeDark,
    secondary = ScribbleTeal,
    onSecondary = Color.White,
    secondaryContainer = ScribbleTealContainerLight,
    onSecondaryContainer = ScribbleTealDark,
    tertiary = ScribblePurple,
    onTertiary = Color.White,
    tertiaryContainer = ScribblePurpleContainerLight,
    onTertiaryContainer = Color(0xFF311B92),
    background = CanvasPaperLight,
    onBackground = Color(0xFF2D2926),
    surface = CanvasPaperCardLight,
    onSurface = Color(0xFF2D2926),
    surfaceVariant = Color(0xFFF3ECE3),
    onSurfaceVariant = Color(0xFF6A645D),
    outline = CanvasPaperBorderLight
)

@Composable
fun WhatTheScribbleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep distinctive party branding by default
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}