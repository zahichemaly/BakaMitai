package com.zc.bakamitai.feature.bookmarks.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zc.bakamitai.feature.bookmarks.domain.usecase.ApplyReleaseCheckUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.CheckBookmarkedReleasesUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.GetReleaseAlertsEnabledUseCase
import com.zc.bakamitai.feature.bookmarks.domain.usecase.ScheduleNextReleaseCheckUseCase
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinWorker

/**
 * The background release check (docs/5-bookmarks.md §5.3): checks every bookmark, saves then notifies (never torn
 * by a cancellation), and schedules the next check. It always succeeds: the check retries on its own ladder.
 * A cancelled run (replaced by a newer request, or stopped by the system) schedules nothing.
 *
 * WorkManager persists this class's name: **never rename or move it**.
 */
@KoinWorker
class ReleaseCheckWorker(
    context: Context,
    params: WorkerParameters,
    private val getAlertsEnabled: GetReleaseAlertsEnabledUseCase,
    private val checkReleases: CheckBookmarkedReleasesUseCase,
    private val applyReleaseCheck: ApplyReleaseCheckUseCase,
    private val scheduleNextCheck: ScheduleNextReleaseCheckUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val outcome = checkReleases(alertsEnabled = getAlertsEnabled())
        withContext(NonCancellable) { applyReleaseCheck(outcome) }
        currentCoroutineContext().ensureActive()
        scheduleNextCheck(outcome.nextCheckAt)
        return Result.success()
    }
}
