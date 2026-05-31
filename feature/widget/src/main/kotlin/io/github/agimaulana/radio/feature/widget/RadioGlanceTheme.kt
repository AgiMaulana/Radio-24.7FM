package io.github.agimaulana.radio.feature.widget

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.glance.GlanceTheme
import androidx.glance.material3.ColorProviders
import io.github.agimaulana.radio.core.design.theme.DarkRadioColors
import io.github.agimaulana.radio.core.design.theme.LightRadioColors

/**
 * Radio Glance Theme that maps the core design system colors to Glance ColorProviders.
 */
@Composable
fun RadioGlanceTheme(content: @Composable () -> Unit) {
    GlanceTheme(
        colors = ColorProviders(
            light = lightColorScheme(
                primary = LightRadioColors.primary,
                onPrimary = LightRadioColors.primaryForeground,
                primaryContainer = LightRadioColors.primary.copy(alpha = 0.1f),
                onPrimaryContainer = LightRadioColors.primary,
                secondary = LightRadioColors.secondary,
                onSecondary = LightRadioColors.secondaryForeground,
                secondaryContainer = LightRadioColors.secondary,
                onSecondaryContainer = LightRadioColors.secondaryForeground,
                tertiary = LightRadioColors.accent,
                onTertiary = LightRadioColors.accentForeground,
                background = LightRadioColors.background,
                onBackground = LightRadioColors.foreground,
                surface = LightRadioColors.card,
                onSurface = LightRadioColors.cardForeground,
                surfaceVariant = LightRadioColors.muted,
                onSurfaceVariant = LightRadioColors.mutedForeground,
                error = LightRadioColors.destructive,
                onError = LightRadioColors.destructiveForeground,
                outline = LightRadioColors.border,
                outlineVariant = LightRadioColors.input
            ),
            dark = darkColorScheme(
                primary = DarkRadioColors.primary,
                onPrimary = DarkRadioColors.primaryForeground,
                primaryContainer = DarkRadioColors.primary.copy(alpha = 0.2f),
                onPrimaryContainer = DarkRadioColors.primary,
                secondary = DarkRadioColors.secondary,
                onSecondary = DarkRadioColors.secondaryForeground,
                secondaryContainer = DarkRadioColors.muted,
                onSecondaryContainer = DarkRadioColors.foreground,
                tertiary = DarkRadioColors.accent,
                onTertiary = DarkRadioColors.accentForeground,
                background = DarkRadioColors.background,
                onBackground = DarkRadioColors.foreground,
                surface = DarkRadioColors.card,
                onSurface = DarkRadioColors.cardForeground,
                surfaceVariant = DarkRadioColors.muted,
                onSurfaceVariant = DarkRadioColors.mutedForeground,
                error = DarkRadioColors.destructive,
                onError = DarkRadioColors.destructiveForeground,
                outline = DarkRadioColors.border,
                outlineVariant = DarkRadioColors.input
            )
        ),
        content = content
    )
}
