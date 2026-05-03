package io.github.agimaulana.radio.infrastructure.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import io.github.agimaulana.radio.domain.api.repository.CatalogState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

class DataStoreCatalogStateRepositoryTest {

    @Test
    fun load_returnsNullWhenEmpty() = runTest {
        val repository = repository()

        assertNull(repository.load())
    }

    @Test
    fun saveAndLoad_roundTripsState() = runTest {
        val repository = repository()
        val state = CatalogState(
            query = "talk",
            locationLat = 1.23,
            locationLon = 4.56,
            page = 3,
            source = CatalogState.Source.LOCATION,
        )

        repository.save(state)

        assertEquals(state, repository.load())
    }

    @Test
    fun loadFallsBackToAllForInvalidSource() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { tempFile("catalog-state-invalid") },
        )
        val repository = DataStoreCatalogStateRepository(dataStore)

        dataStore.edit { preferences ->
            preferences[DataStoreCatalogStateRepository.KEY_QUERY] = "news"
            preferences[DataStoreCatalogStateRepository.KEY_SOURCE] = "BROKEN"
        }

        assertEquals(
            CatalogState(
                query = "news",
                page = 0,
                source = CatalogState.Source.ALL,
            ),
            repository.load()
        )
    }

    private fun repository(): DataStoreCatalogStateRepository {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { tempFile("catalog-state") },
        )
        return DataStoreCatalogStateRepository(dataStore)
    }

    private fun tempFile(prefix: String): File {
        return File.createTempFile(prefix, ".preferences_pb").apply {
            deleteOnExit()
        }
    }
}
