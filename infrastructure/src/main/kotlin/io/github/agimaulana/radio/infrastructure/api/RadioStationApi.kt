package io.github.agimaulana.radio.infrastructure.api

import io.github.agimaulana.radio.infrastructure.response.RadioStationResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RadioStationApi {

    @GET("/json/stations/search")
    suspend fun getRadioStations(
        @Query("geo_lat") lat: Double? = null,
        @Query("geo_long") lon: Double? = null,
        @Query("geo_distance") geoDistance: Int? = null,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int = 10,
        @Query("hidebroken") hideBroken: Boolean = true,
        @Query("order") order: String = "votes",
        @Query("reverse") reverse: Boolean = true,
    ): List<RadioStationResponse>

    @GET("/json/stations/bycountry/indonesia")
    suspend fun getRadioStationsByCountry(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int = 10,
        @Query("hidebroken") hideBroken: Boolean = true,
    ): List<RadioStationResponse>

    @Suppress("LongParameterList")
    @GET("/json/stations/search")
    suspend fun searchRadioStations(
        @Query("name") name: String,
        @Query("nameExact") nameExact: Boolean = false,
        @Query("countrycode") country: String = "ID",
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 100000,
    ): List<RadioStationResponse>

    @GET("/{format}/stations/byuuid/{uuid}")
    suspend fun getRadioStationByUuid(
        @Path("format") format: String,
        @Path("uuid") uuid: String
    ): List<RadioStationResponse>
}
