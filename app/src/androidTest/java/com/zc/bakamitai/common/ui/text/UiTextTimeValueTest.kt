package com.zc.bakamitai.common.ui.text

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zc.bakamitai.common.ui.resource.StringResource
import kotlinx.datetime.LocalTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

/** A time nested in a string resource follows the user's clock format, and changes with it. */
@RunWith(AndroidJUnit4::class)
class UiTextTimeValueTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val airingToday = UiText.StringResource(
        StringResource.bookmarks_airing_today,
        listOf(UiText.TimeValue(LocalTime(21, 30))),
    )

    @Test
    fun aNestedTimeFollowsTheProvidedClockAndRecomposesWhenItChanges() {
        var use24HourClock by mutableStateOf(false)
        composeRule.setContent {
            CompositionLocalProvider(LocalUse24HourClock provides use24HourClock) {
                Text(text = airingToday.asString())
            }
        }

        composeRule.onNodeWithText("TODAY 9:30 PM").assertExists()

        use24HourClock = true

        composeRule.onNodeWithText("TODAY 21:30").assertExists()
    }

    @Test
    fun outsideCompositionTheClockIsAParameter() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        assertEquals("TODAY 9:30 PM", airingToday.asString(context))
        assertEquals("TODAY 21:30", airingToday.asString(context, use24HourClock = true))
    }
}
