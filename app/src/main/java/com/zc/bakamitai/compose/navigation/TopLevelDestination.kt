package com.zc.bakamitai.compose.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.zc.bakamitai.R

enum class TopLevelDestination(
    val titleId: Int,
    val icon: ImageVector,
    val route: String,
) {
    Home(
        titleId = R.string.home,
        icon = Icons.Default.Home,
        route = "home"
    ),
    Schedule(
        titleId = R.string.schedule,
        icon = Icons.Default.DateRange,
        route = "schedule"
    ),
    Library(
        titleId = R.string.all_shows,
        icon = Icons.AutoMirrored.Filled.List,
        route = "library",
    ),
    Bookmark(
        titleId = R.string.favorites,
        icon = Icons.Default.Favorite,
        route = "bookmarks"
    )
}

const val MAIN_GRAPH = "main_graph"
