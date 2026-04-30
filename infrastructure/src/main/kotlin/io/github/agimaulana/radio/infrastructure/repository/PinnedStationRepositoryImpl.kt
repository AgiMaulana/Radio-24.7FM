package io.github.agimaulana.radio.infrastructure.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.repository.PinnedStationLimitReachedException
import io.github.agimaulana.radio.domain.api.repository.PinnedStationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.pinnedStationsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = PinnedStationRepositoryImpl.DATA_STORE_NAME,
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, PinnedStationRepositoryImpl.PREFS_NAME))
    }
)

@Singleton
class PinnedStationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    moshi: Moshi
) : PinnedStationRepository {
    private val dataStore = context.pinnedStationsDataStore

    private val adapter = moshi.adapter<List<RadioStation>>(
        Types.newParameterizedType(List::class.java, RadioStation::class.java)
    )

    override fun getPinnedStations(): Flow<List<RadioStation>> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                decodePinnedStations(preferences[KEY_PINNED_STATIONS])
            }
    }

    override suspend fun pinStation(station: RadioStation) {
        dataStore.edit { preferences ->
            val current = decodePinnedStations(preferences[KEY_PINNED_STATIONS]).toMutableList()
            val isAlreadyPinned = current.any { it.stationUuid == station.stationUuid }
            if (isAlreadyPinned) return@edit
            if (current.size >= MAX_PINS) {
                throw PinnedStationLimitReachedException(MAX_PINS)
            }
            current.add(station)
            preferences[KEY_PINNED_STATIONS] = adapter.toJson(current)
        }
    }

    override suspend fun unpinStation(stationUuid: String) {
        dataStore.edit { preferences ->
            val current = decodePinnedStations(preferences[KEY_PINNED_STATIONS]).toMutableList()
            if (current.removeIf { it.stationUuid == stationUuid }) {
                preferences[KEY_PINNED_STATIONS] = adapter.toJson(current)
            }
        }
    }

    override suspend fun isPinned(stationUuid: String): Boolean {
        return getPinnedStations().first().any { it.stationUuid == stationUuid }
    }

    private fun decodePinnedStations(json: String?): List<RadioStation> {
        if (json.isNullOrEmpty()) return emptyList()
        return try {
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        const val PREFS_NAME = "pinned_stations_prefs"
        const val DATA_STORE_NAME = "pinned_stations"
        val KEY_PINNED_STATIONS = stringPreferencesKey("pinned_stations")
        private const val MAX_PINS = 8
    }
}
