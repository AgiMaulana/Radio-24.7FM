package io.github.agimaulana.radio.core.common.internal

import io.github.agimaulana.radio.core.common.DateTimeProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class DateTimeProviderImplTest {

    private lateinit var dateTimeProvider: DateTimeProvider

    @Before
    fun setup() {
        dateTimeProvider = DateTimeProviderImpl()
    }

    @Test
    fun `now should return current LocalDateTime`() {
        val expectedNow = LocalDateTime.now().withSecond(0).withNano(0)

        val result = dateTimeProvider.now()

        assertEquals(expectedNow, result.withSecond(0).withNano(0))
    }
}
