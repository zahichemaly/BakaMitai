package com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarkcard.mapper

import com.zc.bakamitai.common.ui.component.timechip.model.TimeChipTone
import com.zc.bakamitai.common.ui.component.timechip.model.TimeChipUIModel
import com.zc.bakamitai.common.ui.mapper.toDateLabel
import com.zc.bakamitai.common.ui.mapper.toShortDayName
import com.zc.bakamitai.common.ui.mapper.toTimeLabel
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.bookmarks.domain.model.BookmarkedShow
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarkcard.model.BookmarkCardUIModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

fun BookmarkedShow.toBookmarkCardUIModel(): BookmarkCardUIModel {
    val title = UiText.StringValue(bookmark.show.title)
    return BookmarkCardUIModel(
        slug = bookmark.show.slug.value,
        title = title,
        coverUrl = bookmark.show.coverUrl,
        addedLabel = UiText.StringResource(StringResource.bookmarks_added_on, listOf(addedOn.toDateLabel())),
        airing = toAiringChip(),
        removeLabel = UiText.StringResource(StringResource.cd_bookmarks_remove, listOf(title)),
    )
}

fun List<BookmarkedShow>.toBookmarkCardUIModels(): ImmutableList<BookmarkCardUIModel> =
    map { it.toBookmarkCardUIModel() }.toImmutableList()

/** "TODAY 9:30 PM" (upcoming), "MON 9:30 PM", or "NOT AIRING" (muted) when the show isn't on the schedule. */
private fun BookmarkedShow.toAiringChip(): TimeChipUIModel {
    val next = nextAiring ?: return TimeChipUIModel(
        label = UiText.StringResource(StringResource.bookmarks_not_airing),
        tone = TimeChipTone.MUTED,
    )
    val time = next.time.toTimeLabel()
    return if (airsToday) {
        TimeChipUIModel(
            UiText.StringResource(StringResource.bookmarks_airing_today, listOf(time)),
            TimeChipTone.UPCOMING,
        )
    } else {
        TimeChipUIModel(
            label = UiText.StringResource(
                StringResource.bookmarks_airing_day,
                listOf(next.dayOfWeek.toShortDayName(), time),
            ),
            tone = TimeChipTone.DEFAULT,
        )
    }
}
