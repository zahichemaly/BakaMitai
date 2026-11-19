package com.zc.bakamitai.feature.bookmarks.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ReleaseAlertsStatusTest {

    @Test
    fun `switched off is OFF whatever Android allows`() {
        assertEquals(ReleaseAlertsStatus.OFF, ReleaseAlertsStatus.of(isSwitchedOn = false, isSystemAllowed = true))
        assertEquals(ReleaseAlertsStatus.OFF, ReleaseAlertsStatus.of(isSwitchedOn = false, isSystemAllowed = false))
    }

    @Test
    fun `switched on is ON when Android allows it, BLOCKED otherwise`() {
        assertEquals(ReleaseAlertsStatus.ON, ReleaseAlertsStatus.of(isSwitchedOn = true, isSystemAllowed = true))
        assertEquals(ReleaseAlertsStatus.BLOCKED, ReleaseAlertsStatus.of(isSwitchedOn = true, isSystemAllowed = false))
    }
}
