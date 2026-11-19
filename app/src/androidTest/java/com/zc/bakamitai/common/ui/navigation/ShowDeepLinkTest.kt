package com.zc.bakamitai.common.ui.navigation

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class ShowDeepLinkTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun theIntentTargetsTheLauncherActivityWithTheSlugAndFlags() {
        val intent = context.showDetailsIntent("sousou-no-frieren")

        assertEquals("com.zc.bakamitai.app.MainActivity", intent.component?.className)
        assertEquals("sousou-no-frieren", intent.getStringExtra(EXTRA_SHOW_SLUG))
        val flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        assertTrue(intent.flags and flags == flags)
        assertEquals("sousou-no-frieren", intent.showSlugOrNull())
    }

    @Test
    fun aMissingOrBlankSlugIsNull() {
        assertNull(Intent().showSlugOrNull())
        assertNull(Intent().putExtra(EXTRA_SHOW_SLUG, "  ").showSlugOrNull())
    }

    @Test
    fun anIntentReplayedFromRecentsIsIgnored() {
        val intent = context.showDetailsIntent("sousou-no-frieren")
            .addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY)

        assertNull(intent.showSlugOrNull())
    }
}
