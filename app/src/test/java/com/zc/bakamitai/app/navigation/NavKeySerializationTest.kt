package com.zc.bakamitai.app.navigation

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.serialization.NavKeySerializer
import com.zc.bakamitai.common.data.network.appJson
import com.zc.bakamitai.feature.home.presentation.navigation.HomeNavKey
import com.zc.bakamitai.feature.schedule.presentation.navigation.ScheduleNavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Every `NavKey` survives process death: the back stacks are saved through `NavKeySerializer`. */
class NavKeySerializationTest {

    /** One sample of every key in the app. Add each new key here. */
    private val keys: List<NavKey> = listOf(HomeNavKey, ScheduleNavKey)

    @Test
    fun `every key round-trips through NavKeySerializer`() {
        val serializer = NavKeySerializer<NavKey>()
        keys.forEach { key ->
            val encoded = appJson.encodeToString(serializer, key)

            assertEquals(key, appJson.decodeFromString(serializer, encoded))
        }
    }

    @Test
    fun `every top-level destination key is a known key`() {
        TopLevelDestination.entries.mapNotNull { it.key }.forEach { key ->
            assertTrue(key in keys, "$key is missing from the sample list")
        }
    }
}
