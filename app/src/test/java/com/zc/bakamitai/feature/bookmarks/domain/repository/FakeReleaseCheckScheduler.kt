package com.zc.bakamitai.feature.bookmarks.domain.repository

import kotlin.time.Instant

/** Records every scheduling call, in order. */
class FakeReleaseCheckScheduler : ReleaseCheckScheduler {

    sealed interface Call {
        data object RequestCheck : Call

        data object EnsureScheduled : Call

        data class ScheduleNext(val at: Instant?) : Call
    }

    val calls = mutableListOf<Call>()

    override fun requestCheck() {
        calls += Call.RequestCheck
    }

    override fun ensureCheckScheduled() {
        calls += Call.EnsureScheduled
    }

    override fun scheduleNextCheck(at: Instant?) {
        calls += Call.ScheduleNext(at)
    }
}
