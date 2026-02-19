package com.zc.bakamitai.compose.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.zc.bakamitai.compose.core.domain.StringResource
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute : NavKey

@Serializable
object ScheduleRoute : NavKey

@Serializable
object LibraryRoute : NavKey

@Serializable
object BookmarkRoute : NavKey

enum class TopLevelDestination(
    val titleId: Int,
    val icon: ImageVector,
    val route: NavKey,
) {
    Home(
        titleId = StringResource.home,
        icon = Icons.Default.Home,
        route = HomeRoute
    ),
    Schedule(
        titleId = StringResource.schedule,
        icon = Icons.Default.DateRange,
        route = ScheduleRoute
    ),
    Library(
        titleId = StringResource.all_shows,
        icon = Icons.AutoMirrored.Filled.List,
        route = LibraryRoute,
    ),
    Bookmark(
        titleId = StringResource.favorites,
        icon = Icons.Default.Favorite,
        route = BookmarkRoute
    )
}
