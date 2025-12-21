package io.github.agimaulana.radio.domain.api.entity

data class RadioStation(
    val stationUuid: String,
    val name: String,
    val tags: List<String>,
    val imageUrl: String,
    val url: String,
    val resolvedUrl: String,
)
