package com.zc.bakamitai.feature.bookmarks.data.worker

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/**
 * The unique `release-check` chain. Works stay enqueued: the test WorkManager meets no constraint and no delay
 * until told to, so nothing runs here.
 */
@RunWith(AndroidJUnit4::class)
class ReleaseCheckSchedulerImplTest {

    private val now = Instant.parse("2026-11-16T12:10:00Z")
    private lateinit var workManager: WorkManager
    private lateinit var scheduler: ReleaseCheckSchedulerImpl

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder().setExecutor(SynchronousExecutor()).build(),
        )
        workManager = WorkManager.getInstance(context)
        scheduler = ReleaseCheckSchedulerImpl(workManager, FixedClock(now))
    }

    private suspend fun works(): List<WorkInfo> = workManager.getWorkInfosForUniqueWorkFlow(RELEASE_CHECK_WORK).first()

    @Test
    fun aRequestedCheckReplacesADelayedOne() = runTest {
        scheduler.scheduleNextCheck(now + 1.hours)
        val delayed = works().single()
        assertEquals(1.hours.inWholeMilliseconds, delayed.initialDelayMillis)

        scheduler.requestCheck()

        // The delayed work never started, so REPLACE drops it entirely.
        val replaced = workManager.getWorkInfoByIdFlow(delayed.id).first()
        assertTrue(replaced == null || replaced.state == WorkInfo.State.CANCELLED)
        val requested = works().single()
        assertTrue(requested.id != delayed.id)
        assertEquals(WorkInfo.State.ENQUEUED, requested.state)
        assertEquals(0, requested.initialDelayMillis)
        assertEquals(NetworkType.CONNECTED, requested.constraints.requiredNetworkType)
        assertTrue(RELEASE_CHECK_WORK in requested.tags)
    }

    @Test
    fun ensuringACheckKeepsTheScheduledOne() = runTest {
        scheduler.scheduleNextCheck(now + 1.hours)
        val scheduled = works().single()

        scheduler.ensureCheckScheduled()

        assertEquals(listOf(scheduled.id), works().map { it.id })
        assertEquals(WorkInfo.State.ENQUEUED, works().single().state)
    }

    @Test
    fun ensuringACheckWithNothingScheduledEnqueuesOne() = runTest {
        scheduler.ensureCheckScheduled()

        assertEquals(WorkInfo.State.ENQUEUED, works().single().state)
    }

    @Test
    fun theNextCheckIsChainedAfterTheCurrentOneWithItsDelay() = runTest {
        scheduler.requestCheck()
        val current = works().single()

        scheduler.scheduleNextCheck(now + 2.hours)

        val all = works()
        assertEquals(WorkInfo.State.ENQUEUED, all.single { it.id == current.id }.state)
        val next = all.single { it.id != current.id }
        assertEquals(WorkInfo.State.BLOCKED, next.state)
        assertEquals(2.hours.inWholeMilliseconds, next.initialDelayMillis)
        assertEquals(NetworkType.CONNECTED, next.constraints.requiredNetworkType)
    }

    @Test
    fun noNextCheckSchedulesNothing() = runTest {
        scheduler.scheduleNextCheck(null)

        assertEquals(emptyList(), works())
    }

    private class FixedClock(private val instant: Instant) : Clock {
        override fun now(): Instant = instant
    }
}
