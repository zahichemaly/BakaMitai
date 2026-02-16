package com.zc.bakamitai.compose.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import com.zc.bakamitai.R
import com.zc.bakamitai.compose.common.UiText

/**
 * Created by Zahi Chemaly on 09/12/2025.
 */
val navigationItems = listOf(
    NavigationItem(
        title = UiText.StringResource(R.string.home),
        icon = Icons.Default.Home,
        route = HomeDestination.Home.route
    ),
    NavigationItem(
        title = UiText.StringResource(R.string.schedule),
        icon = Icons.Default.DateRange,
        route = HomeDestination.Schedule.route
    ),
    NavigationItem(
        title = UiText.StringResource(R.string.all_shows),
        icon = Icons.AutoMirrored.Filled.List,
        route = HomeDestination.Library.route
    ),
    NavigationItem(
        title = UiText.StringResource(R.string.favorites),
        icon = Icons.Default.Favorite,
        route = HomeDestination.Bookmark.route
    )
)
