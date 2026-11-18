package com.zc.bakamitai.common.domain.util

import kotlinx.datetime.TimeZone

/** Always returns [timeZone]; change it to simulate the user moving to another zone. */
class FixedTimeZoneProvider(var timeZone: TimeZone) : TimeZoneProvider {
    override fun current(): TimeZone = timeZone
}
