package io.github.agimaulana.radio.feature.stationlist.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import io.github.agimaulana.radio.feature.stationlist.R

@Composable
fun BufferingIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BufferingIconTransition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
        ),
        label = "BufferingIconRotation"
    )

    Image(
        painter = painterResource(id = R.drawable.ic_buffering),
        contentDescription = null,
        modifier = modifier.rotate(rotation),
        colorFilter = if (tint != Color.Unspecified) ColorFilter.tint(tint) else null
    )
}
