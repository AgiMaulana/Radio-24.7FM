package io.github.agimaulana.radio.infrastructure.api

import io.github.agimaulana.radio.infrastructure.response.RadioStationResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface RadioStationApi {

    @GET("/json/stations/bycountry/indonesia")
    suspend fun getRadioStations(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int = 3,
    ): List<RadioStationResponse>
}