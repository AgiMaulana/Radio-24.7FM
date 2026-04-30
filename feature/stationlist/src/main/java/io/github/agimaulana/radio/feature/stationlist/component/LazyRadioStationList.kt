package io.github.agimaulana.radio.feature.stationlist.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.agimaulana.radio.core.design.RadioTheme
import io.github.agimaulana.radio.feature.stationlist.player.BufferingIcon
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.UiState.Station
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun LazyRadioStationList(
    stations: ImmutableList<Station>,
    pinnedStations: ImmutableList<Station>,
    isPinnedStationsLoading: Boolean,
    isStationsLoading: Boolean,
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
        if (isPinnedStationsLoading || pinnedStations.isNotEmpty()) {
            item {
                if (isPinnedStationsLoading && pinnedStations.isEmpty()) {
                    SectionLoading(
                        label = "PINNED",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                } else {
                    PinnedStationRow(
                        pinnedStations = pinnedStations,
                        onStationClick = onClick,
                        onStationLongClick = onLongClick,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
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
                modifier = Modifier.padding(
                    bottom = 8.dp,
                    start = 16.dp,
                    end = 16.dp
                )
            )
        }
        if (isStationsLoading && stations.isEmpty()) {
            item {
                SectionLoading(
                    label = null,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        } else {
            items(stations, key = { it.serverUuid }) {
                StationTile(
                    station = it,
                    onClick = { onClick(it) },
                    onLongClick = { onLongClick(it) },
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp
                    )
                )
            }
        }
        if (isStationsLoading && stations.isNotEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BufferingIcon(
                        modifier = Modifier.size(32.dp),
                        tint = RadioTheme.colors.primary,
                        tweenDurationMillis = 1500,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLoading(
    label: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        BufferingIcon(
            modifier = Modifier.size(if (label == null) 64.dp else 40.dp),
            tint = RadioTheme.colors.primary,
            tweenDurationMillis = 1500,
        )
    }
}
