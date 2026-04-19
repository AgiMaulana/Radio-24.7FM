package io.github.agimaulana.radio.feature.stationlist.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import io.github.agimaulana.radio.core.design.theme.PreviewTheme
import io.github.agimaulana.radio.feature.stationlist.R

@Composable
internal fun MiniPlayerPlayPauseButton(
    isBuffering: Boolean,
    isPlaying: Boolean,
    onClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = MaterialTheme.colorScheme.surfaceTint
    val contentColor = contentColorFor(backgroundColor)
    IconButton(
        onClick = { onClick(isPlaying) },
        modifier = modifier.clip(CircleShape)
            .background(backgroundColor)
    ) {
        AnimatedContent(targetState = isBuffering to isPlaying) { (buffering, playing) ->
            when {
                buffering -> {
                    BufferingIcon(
                        tint = contentColor
                    )
                }

                playing -> {
                    Image(
                        painter = painterResource(id = R.drawable.ic_pause),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(contentColor),
                    )
                }

                else -> {
                    Image(
                        painter = painterResource(id = R.drawable.ic_play),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(contentColor),
                    )
                }
            }
        }

    }
}

@Preview
@Composable
private fun MiniPlayerPlayPauseButtonPlayingPreview() {
    PreviewTheme {
        MiniPlayerPlayPauseButton(
            isBuffering = false,
            isPlaying = true,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun MiniPlayerPlayPauseButtonPausedPreview() {
    PreviewTheme {
        MiniPlayerPlayPauseButton(
            isBuffering = false,
            isPlaying = false,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun MiniPlayerPlayPauseButtonBufferingPreview() {
    PreviewTheme {
        MiniPlayerPlayPauseButton(
            isBuffering = true,
            isPlaying = false,
            onClick = {},
        )
    }
}



