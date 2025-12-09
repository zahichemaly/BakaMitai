package com.zc.bakamitai.navigation

/**
 * Created by Zahi Chemaly on 09/12/2025.
 */
sealed class Screen(val route: String) {
    object Home : Screen("home_screen")
    object Schedule : Screen("schedule_screen")
    object Library : Screen("library_screen")
    object Bookmark : Screen("bookmark_screen")
}
