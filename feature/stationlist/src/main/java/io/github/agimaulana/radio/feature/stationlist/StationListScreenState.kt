package io.github.agimaulana.radio.feature.stationlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.UiState.Station

@Composable
internal fun rememberToolbarDimensions(
    density: Density,
    statusBarPadding: Dp
): ToolbarDimensionsHelper {
    val expandedHeight = 320.dp + statusBarPadding
    val collapsedHeight = 64.dp + statusBarPadding
    val expandedHeightPx = with(density) { expandedHeight.toPx() }
    val collapsedHeightPx = with(density) { collapsedHeight.toPx() }
    val toolbarHeightRangePx = expandedHeightPx - collapsedHeightPx

    var toolbarOffsetPx by remember { mutableStateOf(0f) }

    val progress = if (toolbarHeightRangePx > 0f) {
        (toolbarOffsetPx / toolbarHeightRangePx).coerceIn(0f, 1f)
    } else {
        0f
    }

    return ToolbarDimensionsHelper(
        expandedHeight = expandedHeight,
        collapsedHeight = collapsedHeight,
        progress = progress,
        onScroll = { delta ->
            val oldOffset = toolbarOffsetPx
            toolbarOffsetPx = (toolbarOffsetPx - delta).coerceIn(0f, toolbarHeightRangePx)
            oldOffset - toolbarOffsetPx
        }
    )
}

internal class ToolbarDimensionsHelper(
    val expandedHeight: Dp,
    val collapsedHeight: Dp,
    val progress: Float,
    val onScroll: (Float) -> Float
) {
    fun toData() = ToolbarDimensions(expandedHeight, collapsedHeight, progress)
}

@Stable
internal class StationContextMenuState(
    station: Station? = null,
    showMenu: Boolean = false,
    useBottomSheet: Boolean = true,
) {
    var station by mutableStateOf(station)
        private set

    var showMenu by mutableStateOf(showMenu)
        private set

    var useBottomSheet by mutableStateOf(useBottomSheet)
        private set

    fun show(station: Station, useBottomSheet: Boolean = true) {
        this.station = station
        this.useBottomSheet = useBottomSheet
        showMenu = true
    }

    fun dismiss() {
        showMenu = false
    }
}

@Composable
internal fun rememberStationContextMenuState(): StationContextMenuState {
    return remember { StationContextMenuState() }
}
