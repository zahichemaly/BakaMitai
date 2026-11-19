package com.zc.bakamitai.common.data.mapper

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class ReleaseDateDTOMapperTest {

    @Test
    fun `per-show offsets all map to the same instant scale`() {
        // Sora (-0500), Nia Liston (+0100) and Paw and Palaces (+0800) report different offsets (docs/4-details.md D2).
        assertEquals(Instant.parse("2026-11-17T17:38:52Z"), "Tue, 17 Nov 2026 12:38:52 -0500".toReleaseInstant())
        assertEquals(Instant.parse("2026-11-17T17:38:52Z"), "Tue, 17 Nov 2026 18:38:52 +0100".toReleaseInstant())
        assertEquals(Instant.parse("2026-11-17T17:38:52Z"), "Wed, 18 Nov 2026 01:38:52 +0800".toReleaseInstant())
        assertEquals(Instant.parse("2026-07-07T17:41:51Z"), "Tue, 07 Jul 2026 19:41:51 +0200".toReleaseInstant())
    }

    @Test
    fun `surrounding whitespace is ignored`() {
        assertEquals(Instant.parse("2026-11-17T17:38:52Z"), "  Tue, 17 Nov 2026 12:38:52 -0500 ".toReleaseInstant())
    }

    @Test
    fun `a value without an offset, garbage or an impossible date is null`() {
        assertNull("Tue, 17 Nov 2026 12:38:52".toReleaseInstant())
        assertNull("Nov 17th".toReleaseInstant())
        assertNull("".toReleaseInstant())
        assertNull("Mon, 31 Nov 2026 10:03:53 -0500".toReleaseInstant())
    }
}
