package io.github.agimaulana.radio.core.design

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class GlassPlayerState(
    initialOffset: Float,
    private val minOffset: Float,
    private val maxOffset: Float,
    private val scope: CoroutineScope
) {
    val offsetY = Animatable(initialOffset)

    val isExpanded: Boolean
        get() = offsetY.value < (maxOffset / 2) // True if dragged more than halfway up

    val isFullyCollapsed: Boolean
        get() = offsetY.value >= maxOffset

    val canCollapse: Boolean
        get() = offsetY.value < maxOffset

    fun expand() {
        scope.launch {
            offsetY.animateTo(minOffset, spring(stiffness = Spring.StiffnessMediumLow))
        }
    }

    fun collapse() {
        scope.launch {
            offsetY.animateTo(maxOffset, spring(stiffness = Spring.StiffnessMediumLow))
        }
    }
}

@Composable
fun rememberGlassPlayerState(peekHeight: Dp): GlassPlayerState {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }
    val peekHeightPx = with(density) { peekHeight.toPx() }
    
    val minOffset = 0f
    val maxOffset = screenHeight - peekHeightPx

    return remember { GlassPlayerState(maxOffset, minOffset, maxOffset, scope) }
}
