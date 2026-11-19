package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.feature.bookmarks.domain.repository.ReleaseCheckScheduler
import org.koin.core.annotation.Factory

/**
 * The Bookmarks screen's slot refresh: updates the slots (forced by pull-to-refresh), then asks for a check when
 * one changed, or makes sure one is scheduled otherwise (also after a failure: a restored backup has no queue).
 */
@Factory
class RefreshAiringSlotsUseCase(
    private val updateAiringSlots: UpdateAiringSlotsUseCase,
    private val scheduler: ReleaseCheckScheduler,
) {

    suspend operator fun invoke(forceRefresh: Boolean): Result<Unit> {
        val result = updateAiringSlots(forceRefresh)
        if (result.getOrDefault(false)) scheduler.requestCheck() else scheduler.ensureCheckScheduled()
        return result.map { }
    }
}
