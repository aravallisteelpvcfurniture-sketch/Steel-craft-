package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SteelDarkPrimary,
    onPrimary = SteelDarkOnPrimary,
    primaryContainer = SteelDarkPrimaryContainer,
    onPrimaryContainer = SteelDarkOnPrimaryContainer,
    secondary = SteelDarkSecondary,
    onSecondary = SteelDarkOnSecondary,
    secondaryContainer = SteelDarkSecondaryContainer,
    onSecondaryContainer = SteelDarkOnSecondaryContainer,
    background = SteelDarkBackground,
    surface = SteelDarkSurface,
    surfaceVariant = SteelDarkSurfaceVariant,
    onSurface = SteelDarkOnSurface,
    onSurfaceVariant = SteelDarkOnSurfaceVariant,
    outline = SteelDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = SteelPrimary,
    onPrimary = SteelOnPrimary,
    primaryContainer = SteelPrimaryContainer,
    onPrimaryContainer = SteelOnPrimaryContainer,
    secondary = SteelSecondary,
    onSecondary = SteelOnSecondary,
    secondaryContainer = SteelSecondaryContainer,
    onSecondaryContainer = SteelOnSecondaryContainer,
    background = SteelBackground,
    surface = SteelSurface,
    surfaceVariant = SteelSurfaceVariant,
    onSurface = SteelOnSurface,
    onSurfaceVariant = SteelOnSurfaceVariant,
    outline = SteelOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded steel & amber identity consistent
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
