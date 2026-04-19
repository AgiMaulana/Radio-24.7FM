package io.github.agimaulana.radio.infrastructure.api

import io.github.agimaulana.radio.infrastructure.response.RadioStationResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RadioStationApi {

    @GET("/json/stations/bycountry/indonesia")
    suspend fun getRadioStations(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int = 10,
    ): List<RadioStationResponse>

    @GET("/{format}/stations/byname/{searchterm}")
    suspend fun getRadioStationsByName(
        @Path("format") format: String,
        @Path("searchterm") searchTerm: String
    ): List<RadioStationResponse>

    @GET("/{format}/stations/byuuid/{uuid}")
    suspend fun getRadioStationByUuid(
        @Path("format") format: String,
        @Path("uuid") uuid: String
    ): List<RadioStationResponse>
}
