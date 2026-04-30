package io.github.agimaulana.radio.feature.stationlist.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.contentColorFor
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.UiState.Station
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun LazyRadioStationList(
    stations: ImmutableList<Station>,
    pinnedStations: ImmutableList<Station>,
    onClick: (Station) -> Unit,
    onLongClick: (Station) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onReachEnd: () -> Unit = {},
) {
    val sectionLabelColor = contentColorFor(MaterialTheme.colorScheme.background).copy(alpha = 0.72f)

    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleIndex >= totalItems - 3 && totalItems > 0
        }.collect {
            if (it) {
                onReachEnd()
            }
        }
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (pinnedStations.isNotEmpty()) {
            item {
                PinnedStationRow(
                    pinnedStations = pinnedStations,
                    onStationClick = onClick,
                    onStationLongClick = onLongClick,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
        item {
            Text(
                text = "ALL STATIONS",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = sectionLabelColor,
                    letterSpacing = 1.5.sp
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        items(stations, key = { it.serverUuid }) {
            StationTile(
                station = it,
                onClick = { onClick(it) },
                onLongClick = { onLongClick(it) }
            )
        }
    }
}
