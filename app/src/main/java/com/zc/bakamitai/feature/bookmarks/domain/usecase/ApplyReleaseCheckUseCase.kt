package com.zc.bakamitai.feature.bookmarks.domain.usecase

import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseCheckOutcome
import com.zc.bakamitai.feature.bookmarks.domain.repository.BookmarkRepository
import com.zc.bakamitai.feature.bookmarks.domain.repository.ReleaseAlertNotifier
import org.koin.core.annotation.Factory

/**
 * Saves a check's tracking state, then posts its alerts only if the save succeeded: a later check that sees the
 * saved state stays quiet, so an episode never notifies twice. Run it in a `NonCancellable` context.
 */
@Factory
class ApplyReleaseCheckUseCase(
    private val repository: BookmarkRepository,
    private val notifier: ReleaseAlertNotifier,
) {

    suspend operator fun invoke(outcome: ReleaseCheckOutcome) {
        repository.saveReleaseWatches(outcome.updates).onSuccess { notifier.notify(outcome.alerts) }
    }
}
