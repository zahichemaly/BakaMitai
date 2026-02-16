package com.zc.bakamitai.compose.navigation

/**
 * Created by Zahi Chemaly on 09/12/2025.
 */
object HomeGraph {
    const val route = "tabs"
}

sealed class HomeDestination(val route: String) {
    data object Home : HomeDestination("tab/home")
    data object Schedule : HomeDestination("tab/schedule")
    data object Library : HomeDestination("tab/library")
    data object Bookmark : HomeDestination("tab/bookmark")
}
