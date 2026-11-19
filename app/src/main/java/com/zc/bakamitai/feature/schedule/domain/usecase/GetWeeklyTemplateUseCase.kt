package com.zc.bakamitai.feature.schedule.domain.usecase

import com.zc.bakamitai.common.domain.util.TimeZoneProvider
import com.zc.bakamitai.feature.schedule.domain.model.WeeklyTemplate
import com.zc.bakamitai.feature.schedule.domain.repository.ScheduleRepository
import org.koin.core.annotation.Factory

/**
 * The weekly schedule in the device's current time zone, through the repository's short-lived cache (shared with
 * the Schedule tab) unless [forceRefresh].
 */
@Factory
class GetWeeklyTemplateUseCase(
    private val repository: ScheduleRepository,
    private val timeZoneProvider: TimeZoneProvider,
) {

    suspend operator fun invoke(forceRefresh: Boolean = false): Result<WeeklyTemplate> {
        val timeZone = timeZoneProvider.current()
        return repository.getWeeklySchedule(timeZone, forceRefresh).map { showsByDay ->
            WeeklyTemplate(timeZone = timeZone, showsByDay = showsByDay)
        }
    }
}
