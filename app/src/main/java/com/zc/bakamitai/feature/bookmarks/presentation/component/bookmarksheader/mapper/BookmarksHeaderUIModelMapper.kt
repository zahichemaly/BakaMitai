package com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.SortByAlpha
import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.bookmarks.domain.model.ReleaseAlertsStatus
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.model.AlertsChipUIModel
import com.zc.bakamitai.feature.bookmarks.presentation.component.bookmarksheader.model.SortChipUIModel
import com.zc.bakamitai.feature.bookmarks.presentation.model.BookmarkSortOption
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/** One chip per sort option, in order: Recently added, Title (A–Z), Release time. */
fun sortChipUIModels(): ImmutableList<SortChipUIModel> = BookmarkSortOption.entries.map { option ->
    when (option) {
        BookmarkSortOption.RECENTLY_ADDED ->
            SortChipUIModel(
                option,
                UiText.StringResource(StringResource.bookmarks_sort_recently_added),
                Icons.Outlined.Schedule,
            )

        BookmarkSortOption.TITLE ->
            SortChipUIModel(
                option,
                UiText.StringResource(StringResource.bookmarks_sort_title),
                Icons.Outlined.SortByAlpha,
            )

        BookmarkSortOption.RELEASE_TIME ->
            SortChipUIModel(
                option,
                UiText.StringResource(StringResource.bookmarks_sort_release_time),
                Icons.Outlined.Sensors,
            )
    }
}.toImmutableList()

/**
 * The alerts chip for the alerts status: "ALERTS ON" (opens Settings), or "ALERTS OFF" when switched off in Settings
 * (opens Settings) or blocked by Android (turns them on).
 */
fun ReleaseAlertsStatus.toAlertsChipUIModel(): AlertsChipUIModel = when (this) {
    ReleaseAlertsStatus.ON -> AlertsChipUIModel(
        isEnabled = true,
        label = UiText.StringResource(StringResource.bookmarks_alerts_on),
        icon = Icons.Outlined.NotificationsActive,
        contentDescription = UiText.StringResource(StringResource.cd_bookmarks_alerts_on),
    )

    ReleaseAlertsStatus.OFF -> AlertsChipUIModel(
        isEnabled = false,
        label = UiText.StringResource(StringResource.bookmarks_alerts_off),
        icon = Icons.Outlined.NotificationsOff,
        contentDescription = UiText.StringResource(StringResource.cd_bookmarks_alerts_switched_off),
    )

    ReleaseAlertsStatus.BLOCKED -> AlertsChipUIModel(
        isEnabled = false,
        label = UiText.StringResource(StringResource.bookmarks_alerts_off),
        icon = Icons.Outlined.NotificationsOff,
        contentDescription = UiText.StringResource(StringResource.cd_bookmarks_alerts_off),
    )
}

/** "1 show", "8 shows". */
fun Int.toBookmarkCountLabel(): UiText = UiText.StringPlural(PluralResource.bookmarks_count, this, listOf(this))
