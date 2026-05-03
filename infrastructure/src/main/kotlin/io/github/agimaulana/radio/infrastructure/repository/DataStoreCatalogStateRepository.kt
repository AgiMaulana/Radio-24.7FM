package io.github.agimaulana.radio.infrastructure.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.agimaulana.radio.domain.api.repository.CatalogState
import io.github.agimaulana.radio.domain.api.repository.CatalogStateRepository
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreCatalogStateRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : CatalogStateRepository {
    override suspend fun save(state: CatalogState) {
        dataStore.edit { preferences ->
            val query = state.query
            val locationLat = state.locationLat
            val locationLon = state.locationLon

            if (query == null) {
                preferences.remove(KEY_QUERY)
            } else {
                preferences[KEY_QUERY] = query
            }
            if (locationLat == null) {
                preferences.remove(KEY_LOCATION_LAT)
            } else {
                preferences[KEY_LOCATION_LAT] = locationLat
            }
            if (locationLon == null) {
                preferences.remove(KEY_LOCATION_LON)
            } else {
                preferences[KEY_LOCATION_LON] = locationLon
            }
            preferences[KEY_PAGE] = state.page
            preferences[KEY_SOURCE] = state.source.name
        }
    }

    override suspend fun load(): CatalogState? {
        val preferences = try {
            dataStore.data.first()
        } catch (exception: IOException) {
            emptyPreferences()
        }
        if (preferences[KEY_SOURCE] == null) return null

        return CatalogState(
            query = preferences[KEY_QUERY],
            locationLat = preferences[KEY_LOCATION_LAT],
            locationLon = preferences[KEY_LOCATION_LON],
            page = preferences[KEY_PAGE] ?: 0,
            source = preferences[KEY_SOURCE]
                ?.let { source -> runCatching { CatalogState.Source.valueOf(source) }.getOrDefault(CatalogState.Source.ALL) }
                ?: CatalogState.Source.ALL,
        )
    }

    companion object {
        const val DATA_STORE_NAME = "catalog_state"

        internal val KEY_QUERY = stringPreferencesKey("query")
        internal val KEY_LOCATION_LAT = doublePreferencesKey("location_lat")
        internal val KEY_LOCATION_LON = doublePreferencesKey("location_lon")
        internal val KEY_PAGE = intPreferencesKey("page")
        internal val KEY_SOURCE = stringPreferencesKey("source")
    }
}
