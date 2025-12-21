package io.github.agimaulana.radio.infrastructure.datafactories

import io.github.agimaulana.radio.domain.api.entity.RadioStation

fun newRadioStation(
    withStationUuid: String = "",
    withName: String = "",
    withTags: List<String> = emptyList(),
    withImageUrl: String = "",
    withUrl: String = "",
    withResolvedUrl: String = "",
) = RadioStation(
    stationUuid = withStationUuid,
    name = withName,
    tags = withTags,
    imageUrl = withImageUrl,
    url = withUrl,
    resolvedUrl = withResolvedUrl
)
