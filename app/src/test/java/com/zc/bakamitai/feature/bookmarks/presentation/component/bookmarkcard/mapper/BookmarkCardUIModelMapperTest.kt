package com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarkcard.mapper

import com.zc.bakamitai.common.ui.component.timechip.model.TimeChipTone
import com.zc.bakamitai.common.ui.component.timechip.model.TimeChipUIModel
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.bookmarks.domain.BookmarksTestData.bookmark
import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkedShow
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarkcard.model.BookmarkCardUIModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class BookmarkCardUIModelMapperTest {

    private val frieren = bookmark("frieren", "2026-11-18T09:00:00Z", title = "Sousou no Frieren")
        .let { it.copy(show = it.show.copy(coverUrl = "https://subsplease.org/a.jpg")) }

    private fun shown(nextAiring: LocalDateTime?, airsToday: Boolean = false) =
        BookmarkedShow(frieren, addedOn = LocalDate(2026, 11, 18), nextAiring = nextAiring, airsToday = airsToday)

    @Test
    fun `a show airing later today has the upcoming chip`() {
        val title = UiText.StringValue("Sousou no Frieren")

        assertEquals(
            BookmarkCardUIModel(
                slug = "frieren",
                title = title,
                coverUrl = "https://subsplease.org/a.jpg",
                addedLabel = UiText.StringResource(
                    StringResource.bookmarks_added_on,
                    listOf(UiText.StringValue("Nov 18, 2026")),
                ),
                airing = TimeChipUIModel(
                    UiText.StringResource(StringResource.bookmarks_airing_today, listOf(UiText.StringValue("9:30 PM"))),
                    TimeChipTone.UPCOMING,
                ),
                removeLabel = UiText.StringResource(StringResource.cd_bookmarks_remove, listOf(title)),
            ),
            shown(LocalDateTime(2026, 11, 19, 21, 30), airsToday = true).toBookmarkCardUIModel(),
        )
    }

    @Test
    fun `a show airing another day has the weekday and the time`() {
        assertEquals(
            TimeChipUIModel(
                UiText.StringResource(
                    StringResource.bookmarks_airing_day,
                    listOf(UiText.StringResource(StringResource.day_short_monday), UiText.StringValue("9:30 PM")),
                ),
                TimeChipTone.DEFAULT,
            ),
            shown(LocalDateTime(2026, 11, 23, 21, 30)).toBookmarkCardUIModel().airing,
        )
    }

    @Test
    fun `a show that isn't on the schedule is not airing`() {
        assertEquals(
            TimeChipUIModel(UiText.StringResource(StringResource.bookmarks_not_airing), TimeChipTone.MUTED),
            shown(nextAiring = null).toBookmarkCardUIModel().airing,
        )
    }

    @Test
    fun `a list keeps its order`() {
        val other = shown(null).copy(bookmark = bookmark("dandadan", "2026-11-17T00:00:00Z"))

        assertEquals(listOf("frieren", "dandadan"), listOf(shown(null), other).toBookmarkCardUIModels().map { it.slug })
    }
}
