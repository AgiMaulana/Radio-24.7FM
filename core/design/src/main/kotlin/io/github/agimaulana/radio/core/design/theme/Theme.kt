package io.github.agimaulana.radio.core.design.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import io.github.agimaulana.radio.core.design.LocalRadioColors

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

@Composable
fun RadioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val radioColors = if (darkTheme) DarkRadioColors else LightRadioColors
    val colorScheme = colorSchemeFrom(radioColors, darkTheme)
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.apply {
                    statusBarColor = colorScheme.background.toArgb()
                    navigationBarColor = colorScheme.background.toArgb()
                    isStatusBarContrastEnforced = false
                    isNavigationBarContrastEnforced = false
                }
            }
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
            controller.apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalRadioColors provides radioColors
        // LocalBoilerplateTypography provides ... (if you have it)
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

@Composable
fun PreviewTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    RadioTheme(darkTheme, content)
}

