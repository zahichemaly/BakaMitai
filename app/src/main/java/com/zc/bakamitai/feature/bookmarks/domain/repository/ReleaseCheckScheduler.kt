package com.zc.bakamitai.feature.bookmarks.domain.repository

import kotlin.time.Instant

/** Schedules the background release check (a port: implemented with WorkManager in `data/worker/`). */
interface ReleaseCheckScheduler {

    /** Runs a check as soon as possible, replacing any scheduled one: the bookmarks or their slots changed. */
    fun requestCheck()

    /** Runs a check as soon as possible only when none is scheduled (a restored backup, a lost queue). */
    fun ensureCheckScheduled()

    /** From inside a check: runs the next one at [at], after the current one ends; `null` schedules nothing. */
    fun scheduleNextCheck(at: Instant?)
}
