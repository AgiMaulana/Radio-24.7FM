package io.github.agimaulana.radio.feature.stationlist.datafactories

import io.github.agimaulana.radio.feature.stationlist.StationListViewModel

fun newUiStateStation(
    withServerUuid: String = "",
    withName: String = "",
    withGenre: String = "",
    withImageUrl: String = "",
    withIsPlaying: Boolean = false,
) = StationListViewModel.UiState.Station(
    serverUuid = withServerUuid,
    name = withName,
    genre = withGenre,
    imageUrl = withImageUrl,
    isPlaying = withIsPlaying,
)