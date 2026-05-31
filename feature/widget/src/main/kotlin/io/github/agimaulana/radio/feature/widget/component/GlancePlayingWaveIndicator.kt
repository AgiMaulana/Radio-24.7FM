package io.github.agimaulana.radio.feature.widget.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.unit.ColorProvider
import io.github.agimaulana.radio.core.design.RadioTheme

@Suppress("FunctionNaming")
@Composable
fun GlancePlayingWaveIndicator(
    modifier: GlanceModifier = GlanceModifier,
    height: Dp = 16.dp,
    barWidth: Dp = 3.dp,
    barSpacing: Dp = 2.dp,
) {
    val color = RadioTheme.colors.ring

    Row(
        modifier = modifier.height(height),
        verticalAlignment = Alignment.Bottom,
    ) {
        Spacer(
            modifier = GlanceModifier
                .size(width = barWidth, height = height * 0.6f)
                .background(ColorProvider(color))
        )
        Spacer(modifier = GlanceModifier.width(barSpacing))
        Spacer(
            modifier = GlanceModifier
                .size(width = barWidth, height = height * 0.9f)
                .background(ColorProvider(color))
        )
        Spacer(modifier = GlanceModifier.width(barSpacing))
        Spacer(
            modifier = GlanceModifier
                .size(width = barWidth, height = height * 0.4f)
                .background(ColorProvider(color))
        )
    }
}
