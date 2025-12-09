package com.zc.bakamitai.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home

/**
 * Created by Zahi Chemaly on 09/12/2025.
 */
val navigationItems = listOf(
    NavigationItem(
        title = "Home",
        icon = Icons.Default.Home,
        route = Screen.Home.route
    ),
    NavigationItem(
        title = "Schedule",
        icon = Icons.Default.DateRange,
        route = Screen.Schedule.route
    ),
    NavigationItem(
        title = "All Shows",
        icon = Icons.AutoMirrored.Filled.List,
        route = Screen.Library.route
    ),
    NavigationItem(
        title = "Bookmarks",
        icon = Icons.Default.Favorite,
        route = Screen.Bookmark.route
    )
)
