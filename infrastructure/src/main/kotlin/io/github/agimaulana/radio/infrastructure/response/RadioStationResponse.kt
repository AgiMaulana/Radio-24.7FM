package io.github.agimaulana.radio.infrastructure.response


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RadioStationResponse(
    @param:Json(name = "favicon") val favicon: String,
    @param:Json(name = "name") val name: String,
    @param:Json(name = "stationuuid") val stationuuid: String,
    @param:Json(name = "tags") val tags: String,
    @param:Json(name = "url") val url: String,
)