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
    primary = MinimalDarkPrimary,
    onPrimary = MinimalDarkOnPrimary,
    primaryContainer = MinimalDarkPrimaryContainer,
    onPrimaryContainer = MinimalDarkOnPrimaryContainer,
    secondary = MinimalDarkSecondary,
    onSecondary = MinimalDarkOnSecondary,
    background = MinimalDarkBackground,
    onBackground = MinimalDarkOnBackground,
    surface = MinimalDarkSurface,
    onSurface = MinimalDarkOnSurface,
    surfaceVariant = MinimalDarkSurfaceVariant,
    onSurfaceVariant = MinimalDarkOnSurfaceVariant,
    outline = MinimalDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = MinimalLightPrimary,
    onPrimary = MinimalLightOnPrimary,
    primaryContainer = MinimalLightPrimaryContainer,
    onPrimaryContainer = MinimalLightOnPrimaryContainer,
    secondary = MinimalLightSecondary,
    onSecondary = MinimalLightOnSecondary,
    background = MinimalLightBackground,
    onBackground = MinimalLightOnBackground,
    surface = MinimalLightSurface,
    onSurface = MinimalLightOnSurface,
    surfaceVariant = MinimalLightSurfaceVariant,
    onSurfaceVariant = MinimalLightOnSurfaceVariant,
    outline = MinimalLightOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set default dynamicColor to false to maintain the specific brand identity
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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
