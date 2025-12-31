package io.github.agimaulana.radio.core.design

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
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

    val dynamicInset = rememberPlayerOffset(state)

    Box(modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize()
                .blur(if (progress > 0.01f) (progress * 25).dp else 0.dp)
        ) {
            mainContent()
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(0, (state.offsetY.value-dynamicInset).roundToInt()) }
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
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
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = (1f - progress * 3f).coerceIn(0f, 1f)
                    }
            ) {
                miniPlayerContent(progress)
            }

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = progress
                    }
            ) {
                fullPlayerContent(progress)
            }
        }
    }
}

@Composable
private fun rememberPlayerOffset(glassPlayerState: GlassPlayerState): Float {
    val density = LocalDensity.current
    val navigationBarInset = WindowInsets.navigationBars.getBottom(density)

    // On Android 15+, Edge-to-Edge is mandatory.
    // On Android 14 and below, we should only apply this lift if we are
    // certain the miniplayer is being covered by the nav bar.
    val isSupportEdge2EdgeByDefault = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM // API 35+

    return remember(glassPlayerState.offsetY.value, navigationBarInset) {
        if (navigationBarInset <= 0) {
            0f
        } else if (!isSupportEdge2EdgeByDefault) {
            // On Android 14, even if inset is 126, the system usually
            // pads the root view. If we subtract here, it floats.
            0f
        } else {
            // Android 15/16 logic: Lift the player smoothly
            val dragFactor = (glassPlayerState.offsetY.value / navigationBarInset).coerceIn(0f, 1f)
            navigationBarInset * dragFactor
        }
    }
}
