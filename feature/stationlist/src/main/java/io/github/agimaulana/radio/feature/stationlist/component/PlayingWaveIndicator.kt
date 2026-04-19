package io.github.agimaulana.radio.feature.stationlist.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * PlayingWaveIndicator
 *
 * Animated three-bar wave indicator for the currently playing station card.
 *
 * Usage:
 *   PlayingWaveIndicator()                          // default pink, 16dp tall
 *   PlayingWaveIndicator(color = Color.White)       // white bars
 *   PlayingWaveIndicator(barCount = 4, height = 20.dp)
 *
 * Drop anywhere — no external dependencies required.
 */

@Composable
fun PlayingWaveIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFF69B4),
    barCount: Int = 3,
    height: Dp = 16.dp,
    barWidth: Dp = 3.dp,
    barSpacing: Dp = 2.dp,
) {
    // Each bar gets a slightly different duration and delay for a natural feel
    val durations    = listOf(800, 600, 700)
    val delayOffsets = listOf(0,   150, 300)

    Row(
        modifier = modifier.height(height),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(barSpacing)
    ) {
        repeat(barCount) { index ->
            val duration = durations.getOrElse(index) { 700 }
            val delay    = delayOffsets.getOrElse(index) { index * 120 }

            val fraction by rememberInfiniteTransition(label = "wave_$index")
                .animateFloat(
                    initialValue = 0.25f,
                    targetValue  = 1f,
                    animationSpec = infiniteRepeatable(
                        animation  = tween(duration, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(delay)
                    ),
                    label = "bar_$index"
                )

            Box(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight(fraction)
                    .clip(RoundedCornerShape(barWidth / 2))
                    .background(color)
            )
        }
    }
}