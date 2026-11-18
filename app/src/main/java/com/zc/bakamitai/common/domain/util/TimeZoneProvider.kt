package com.zc.bakamitai.common.domain.util

import kotlinx.datetime.TimeZone
import org.koin.core.annotation.Single

/**
 * The device's time zone, injected like [DispatcherProvider] so tests can fix it. Read it on every load
 * (never cache it), so a zone change is picked up by the next request.
 */
interface TimeZoneProvider {
    fun current(): TimeZone
}

@Single
class SystemTimeZoneProvider : TimeZoneProvider {
    override fun current(): TimeZone = TimeZone.currentSystemDefault()
}
