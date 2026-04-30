package io.github.agimaulana.radio.infrastructure.repository

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.agimaulana.radio.domain.api.entity.RadioStation
import io.github.agimaulana.radio.domain.api.repository.PinnedStationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinnedStationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val moshi: Moshi
) : PinnedStationRepository {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val adapter = moshi.adapter<List<RadioStation>>(
        Types.newParameterizedType(List::class.java, RadioStation::class.java)
    )

    override fun getPinnedStations(): Flow<List<RadioStation>> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_PINNED_STATIONS) {
                trySend(getPinnedStationsFromPrefs())
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(getPinnedStationsFromPrefs())
        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    override suspend fun pinStation(station: RadioStation) {
        val current = getPinnedStationsFromPrefs().toMutableList()
        if (current.none { it.stationUuid == station.stationUuid }) {
            if (current.size >= MAX_PINS) {
                return // Cap at 8
            }
            current.add(station)
            savePinnedStationsToPrefs(current)
        }
    }

    override suspend fun unpinStation(stationUuid: String) {
        val current = getPinnedStationsFromPrefs().toMutableList()
        if (current.removeIf { it.stationUuid == stationUuid }) {
            savePinnedStationsToPrefs(current)
        }
    }

    override suspend fun isPinned(stationUuid: String): Boolean {
        return getPinnedStationsFromPrefs().any { it.stationUuid == stationUuid }
    }

    private fun getPinnedStationsFromPrefs(): List<RadioStation> {
        val json = sharedPreferences.getString(KEY_PINNED_STATIONS, null) ?: return emptyList()
        return try {
            adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun savePinnedStationsToPrefs(stations: List<RadioStation>) {
        val json = adapter.toJson(stations)
        sharedPreferences.edit().putString(KEY_PINNED_STATIONS, json).apply()
    }

    companion object {
        private const val PREFS_NAME = "pinned_stations_prefs"
        private const val KEY_PINNED_STATIONS = "pinned_stations"
        private const val MAX_PINS = 8
    }
}
