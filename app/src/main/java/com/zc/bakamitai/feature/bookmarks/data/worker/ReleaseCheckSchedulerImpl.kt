package com.zc.bakamitai.feature.bookmarks.data.worker

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.zc.bakamitai.feature.bookmarks.domain.repository.ReleaseCheckScheduler
import org.koin.core.annotation.Single
import java.util.concurrent.TimeUnit
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/** The unique work name (and tag) of the release checks. Persisted by WorkManager: never rename it. */
const val RELEASE_CHECK_WORK = "release-check"

/**
 * Schedules [ReleaseCheckWorker] as one unique chain (docs/5-bookmarks.md §5.3), always with a network
 * constraint. The worker schedules its successor itself, appended after it, so it never cancels itself.
 */
@Single
class ReleaseCheckSchedulerImpl(private val workManager: WorkManager, private val clock: Clock) :
    ReleaseCheckScheduler {

    override fun requestCheck() {
        workManager.enqueueUniqueWork(RELEASE_CHECK_WORK, ExistingWorkPolicy.REPLACE, checkRequest(Duration.ZERO))
    }

    override fun ensureCheckScheduled() {
        workManager.enqueueUniqueWork(RELEASE_CHECK_WORK, ExistingWorkPolicy.KEEP, checkRequest(Duration.ZERO))
    }

    override fun scheduleNextCheck(at: Instant?) {
        if (at == null) return
        val delay = (at - clock.now()).coerceAtLeast(Duration.ZERO)
        workManager.enqueueUniqueWork(RELEASE_CHECK_WORK, ExistingWorkPolicy.APPEND_OR_REPLACE, checkRequest(delay))
    }

    private fun checkRequest(delay: Duration): OneTimeWorkRequest = OneTimeWorkRequestBuilder<ReleaseCheckWorker>()
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
        .setInitialDelay(delay.inWholeMilliseconds, TimeUnit.MILLISECONDS)
        .addTag(RELEASE_CHECK_WORK)
        .build()
}
