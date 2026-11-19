package com.zc.bakamitai.common.data.mapper

import kotlinx.datetime.format.DateTimeComponents
import kotlin.time.Instant

/**
 * Reads a SubsPlease `release_date` as an instant. It's RFC 1123 (`"Sun, 15 Nov 2026 10:03:53 -0500"`) with an
 * offset that varies per endpoint, per show and per item (docs/api.md §3.3, §3.5), so only the instant is
 * meaningful. `null` when the value doesn't parse, has no offset, or doesn't form a valid date (a day that doesn't
 * exist, a weekday that doesn't match).
 */
fun String.toReleaseInstant(): Instant? = DateTimeComponents.Formats.RFC_1123.parseOrNull(trim())
    ?.takeIf { it.offsetHours != null }
    ?.let { components -> runCatching { components.toInstantUsingOffset() }.getOrNull() }
