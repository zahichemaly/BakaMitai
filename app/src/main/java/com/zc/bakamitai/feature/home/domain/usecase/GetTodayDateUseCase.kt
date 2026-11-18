package com.zc.bakamitai.feature.home.domain.usecase

import com.zc.bakamitai.common.domain.util.TimeZoneProvider
import kotlinx.datetime.LocalDate
import kotlinx.datetime.todayIn
import org.koin.core.annotation.Factory
import kotlin.time.Clock

/** Today's date in the device's time zone. */
@Factory
class GetTodayDateUseCase(private val clock: Clock, private val timeZoneProvider: TimeZoneProvider) {
    operator fun invoke(): LocalDate = clock.todayIn(timeZoneProvider.current())
}
