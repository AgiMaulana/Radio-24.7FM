package io.github.agimaulana.radio.feature.stationlist.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.agimaulana.radio.feature.stationlist.StationListViewModel.UiState.Station
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun LazyRadioStationList(
    stations: ImmutableList<Station>,
    onClick: (Station) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(stations) {
            StationTile(
                station = it,
                onClick = { onClick(it) }
            )
        }
    }
}
