package io.github.agimaulana.radio.core.design.glance.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.glance.GlanceComposable
import androidx.glance.GlanceTheme
import androidx.glance.material3.ColorProviders
import io.github.agimaulana.radio.core.design.LocalRadioColors
import io.github.agimaulana.radio.core.design.theme.DarkRadioColors
import io.github.agimaulana.radio.core.design.theme.LightRadioColors
import io.github.agimaulana.radio.core.design.theme.RadioColors

private fun colorSchemeFrom(radioColors: RadioColors, darkTheme: Boolean): ColorScheme {
    return if (darkTheme) {
        darkColorScheme(
            primary = radioColors.primary,
            onPrimary = radioColors.primaryForeground,
            primaryContainer = radioColors.primary.copy(alpha = 0.2f),
            onPrimaryContainer = radioColors.primary,

            secondary = radioColors.secondary,
            onSecondary = radioColors.secondaryForeground,
            secondaryContainer = radioColors.muted,
            onSecondaryContainer = radioColors.foreground,

            tertiary = radioColors.accent,
            onTertiary = radioColors.accentForeground,

            background = radioColors.background,
            onBackground = radioColors.foreground,

            surface = radioColors.card,
            onSurface = radioColors.cardForeground,
            surfaceVariant = radioColors.muted,
            onSurfaceVariant = radioColors.mutedForeground,

            error = radioColors.destructive,
            onError = radioColors.destructiveForeground,

            outline = radioColors.border,
            outlineVariant = radioColors.input
        )
    } else {
        lightColorScheme(
            primary = radioColors.primary,
            onPrimary = radioColors.primaryForeground,
            primaryContainer = radioColors.primary.copy(alpha = 0.1f),
            onPrimaryContainer = radioColors.primary,

            secondary = radioColors.secondary,
            onSecondary = radioColors.secondaryForeground,
            secondaryContainer = radioColors.secondary,
            onSecondaryContainer = radioColors.secondaryForeground,

            tertiary = radioColors.accent,
            onTertiary = radioColors.accentForeground,

            background = radioColors.background,
            onBackground = radioColors.foreground,

            surface = radioColors.card,
            onSurface = radioColors.cardForeground,
            surfaceVariant = radioColors.muted,
            onSurfaceVariant = radioColors.mutedForeground,

            error = radioColors.destructive,
            onError = radioColors.destructiveForeground,

            outline = radioColors.border,
            outlineVariant = radioColors.input
        )
    }
}

object RadioGlanceTheme {
    val colors = ColorProviders(
        light = colorSchemeFrom(LightRadioColors, darkTheme = false),
        dark = colorSchemeFrom(DarkRadioColors, darkTheme = true)
    )
}

@Composable
fun RadioGlanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable @GlanceComposable () -> Unit
) {
    val radioColors = if (darkTheme) DarkRadioColors else LightRadioColors

    CompositionLocalProvider(
        LocalRadioColors provides radioColors
    ) {
        GlanceTheme(
            colors = RadioGlanceTheme.colors,
            content = content
        )
    }
}
