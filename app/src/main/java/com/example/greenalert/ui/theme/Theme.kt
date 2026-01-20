package com.example.greenalert.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = TextPrimaryDark,
    primaryContainer = GreenDark,
    onPrimaryContainer = TextPrimaryDark,
    secondary = TealSecondary,
    onSecondary = TextPrimaryDark,
    secondaryContainer = TealLight,
    onSecondaryContainer = TextPrimaryLight,
    tertiary = AccentOrange,
    background = DarkBackground,
    onBackground = TextPrimaryDark,
    surface = DarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondaryDark,
    error = AccentRed,
    onError = TextPrimaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = TextPrimaryDark,
    primaryContainer = GreenLight,
    onPrimaryContainer = TextPrimaryLight,
    secondary = TealSecondary,
    onSecondary = TextPrimaryDark,
    secondaryContainer = TealLight,
    onSecondaryContainer = TextPrimaryLight,
    tertiary = AccentOrange,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightCard,
    onSurfaceVariant = TextSecondaryLight,
    error = AccentRed,
    onError = TextPrimaryDark
)

@Composable
fun GreenAlertTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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