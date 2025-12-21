package io.github.agimaulana.radio.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun GlassSlidingPlayerLayout(
    state: GlassPlayerState,
    modifier: Modifier = Modifier,
    peekHeight: Dp = 80.dp,
    miniPlayerContent: @Composable (progress: Float) -> Unit,
    fullPlayerContent: @Composable (progress: Float) -> Unit,
    mainContent: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }
    val peekHeightPx = with(LocalDensity.current) { peekHeight.toPx() }
    val maxOffset = screenHeight - peekHeightPx
    val scope = rememberCoroutineScope()

    val progress by remember {
        derivedStateOf { ((maxOffset - state.offsetY.value) / maxOffset).coerceIn(0f, 1f) }
    }

    Box(modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().blur(if (progress > 0.01f) (progress * 25).dp else 0.dp)) {
            mainContent()
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(0, state.offsetY.value.roundToInt()) }
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.6f))
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        scope.launch { state.offsetY.snapTo((state.offsetY.value + delta).coerceIn(0f, maxOffset)) }
                    },
                    onDragStopped = { velocity ->
                        if (velocity < -500f || state.offsetY.value < screenHeight / 2) {
                            state.expand()
                        } else {
                            state.collapse()
                        }
                    }
                )
        ) {
            Box(Modifier.fillMaxSize()) {
                if (progress < 0.8f) miniPlayerContent(progress)
                if (progress > 0.1f) fullPlayerContent(progress)
            }
        }
    }
}
