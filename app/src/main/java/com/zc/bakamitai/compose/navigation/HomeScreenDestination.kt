package com.zc.bakamitai.compose.navigation

/**
 * Created by Zahi Chemaly on 09/12/2025.
 */

object HomeGraph {
    const val route = "home_graph"

    sealed class Destination(val route: String) {
        object Home : Destination("home_screen")
        object Schedule : Destination("schedule_screen")
        object Library : Destination("library_screen")
        object Bookmark : Destination("bookmark_screen")
    }
}
