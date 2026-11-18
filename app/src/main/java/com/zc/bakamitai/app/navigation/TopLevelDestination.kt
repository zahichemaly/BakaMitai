package com.zc.bakamitai.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.home.presentation.navigation.HomeNavKey

/**
 * The navigation suite's tabs, in order (context.md §2).
 *
 * @property key the tab's root key, or `null` while its feature isn't built: the tab is shown disabled.
 */
enum class TopLevelDestination(
    val key: NavKey?,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: UiText,
) {
    HOME(
        key = HomeNavKey,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        label = UiText.StringResource(StringResource.nav_home),
    ),
    SCHEDULE(
        key = null,
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth,
        label = UiText.StringResource(StringResource.nav_schedule),
    ),
    ALL_SHOWS(
        key = null,
        selectedIcon = Icons.AutoMirrored.Filled.ViewList,
        unselectedIcon = Icons.AutoMirrored.Outlined.ViewList,
        label = UiText.StringResource(StringResource.nav_all_shows),
    ),
    BOOKMARKS(
        key = null,
        selectedIcon = Icons.Filled.Bookmark,
        unselectedIcon = Icons.Outlined.BookmarkBorder,
        label = UiText.StringResource(StringResource.nav_bookmarks),
    ),
}
