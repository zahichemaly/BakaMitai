package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.feature.bookmarks.domain.repository.ReleaseCheckScheduler
import org.koin.core.annotation.Factory
import kotlin.time.Instant

/** From inside a release check: schedules the next one at [invoke]'s `at`, after the current one; `null` ends them. */
@Factory
class ScheduleNextReleaseCheckUseCase(private val scheduler: ReleaseCheckScheduler) {
    operator fun invoke(at: Instant?) = scheduler.scheduleNextCheck(at)
}
