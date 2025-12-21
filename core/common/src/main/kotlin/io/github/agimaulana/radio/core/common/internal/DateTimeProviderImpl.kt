package io.github.agimaulana.radio.core.common.internal

import io.github.agimaulana.radio.core.common.DateTimeProvider
import java.time.LocalDateTime
import java.time.ZoneId

internal class DateTimeProviderImpl : DateTimeProvider {
    override fun now(zoneId: ZoneId): LocalDateTime = LocalDateTime.now()
}
