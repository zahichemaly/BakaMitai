package com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.mapper

import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseAlertsStatus
import com.zc.bakamitai.feature.bookmarks.presentation.model.BookmarkSortOption
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BookmarksHeaderUIModelMapperTest {

    @Test
    fun `the count is a plural`() {
        assertEquals(UiText.StringPlural(PluralResource.bookmarks_count, 1, listOf(1)), 1.toBookmarkCountLabel())
        assertEquals(UiText.StringPlural(PluralResource.bookmarks_count, 8, listOf(8)), 8.toBookmarkCountLabel())
    }

    @Test
    fun `the alerts chip says on, or off with what a tap does`() {
        val on = ReleaseAlertsStatus.ON.toAlertsChipUIModel()
        val switchedOff = ReleaseAlertsStatus.OFF.toAlertsChipUIModel()
        val blocked = ReleaseAlertsStatus.BLOCKED.toAlertsChipUIModel()

        assertTrue(on.isEnabled)
        assertEquals(UiText.StringResource(StringResource.bookmarks_alerts_on), on.label)
        assertEquals(UiText.StringResource(StringResource.cd_bookmarks_alerts_on), on.contentDescription)
        assertFalse(switchedOff.isEnabled)
        assertEquals(UiText.StringResource(StringResource.bookmarks_alerts_off), switchedOff.label)
        assertEquals(
            UiText.StringResource(StringResource.cd_bookmarks_alerts_switched_off),
            switchedOff.contentDescription,
        )
        assertFalse(blocked.isEnabled)
        assertEquals(UiText.StringResource(StringResource.bookmarks_alerts_off), blocked.label)
        assertEquals(UiText.StringResource(StringResource.cd_bookmarks_alerts_off), blocked.contentDescription)
    }

    @Test
    fun `there is one sort chip per option, in order`() {
        val chips = sortChipUIModels()

        assertEquals(BookmarkSortOption.entries, chips.map { it.option })
        assertEquals(
            listOf(
                StringResource.bookmarks_sort_recently_added,
                StringResource.bookmarks_sort_title,
                StringResource.bookmarks_sort_release_time,
            ).map { UiText.StringResource(it) },
            chips.map { it.label },
        )
    }
}
