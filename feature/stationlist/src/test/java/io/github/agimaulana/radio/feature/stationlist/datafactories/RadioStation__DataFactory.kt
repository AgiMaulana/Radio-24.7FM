package io.github.agimaulana.radio.feature.stationlist.datafactories

import io.github.agimaulana.radio.domain.api.entity.RadioStation

fun newRadioStation(
    withStationUuid: String = "",
    withName: String = "",
    withTags: List<String> = emptyList(),
    withImageUrl: String = "",
    withUrl: String = "",
) = RadioStation(
    stationUuid = withStationUuid,
    name = withName,
    tags = withTags,
    imageUrl = withImageUrl,
    url = withUrl,
)
