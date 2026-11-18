package com.zc.bakamitai.common.util

import kotlin.time.Clock
import kotlin.time.Instant

/** A [Clock] that returns [instant] until a test moves it, e.g. to the next day. */
class FixedClock(var instant: Instant) : Clock {
    override fun now(): Instant = instant
}
