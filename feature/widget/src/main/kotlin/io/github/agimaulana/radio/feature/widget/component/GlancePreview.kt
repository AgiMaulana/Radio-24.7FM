package io.github.agimaulana.radio.feature.widget.component

import androidx.compose.runtime.Composable
import androidx.glance.GlanceComposable
import io.github.agimaulana.radio.core.design.glance.theme.RadioGlanceTheme

/**
 * A helper to provide a Glance-compatible environment for standard Compose Previews.
 */
@Composable
fun GlancePreview(
    content: @Composable @GlanceComposable () -> Unit
) {
    GlanceAppWidgetView {
        RadioGlanceTheme {
            content()
        }
    }
}
