package com.zc.bakamitai.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.zc.bakamitai.features.bookmark.BookmarkScreen
import com.zc.bakamitai.features.home.HomeScreen
import com.zc.bakamitai.features.library.LibraryScreen
import com.zc.bakamitai.features.schedule.ScheduleScreen
import com.zc.bakamitai.navigation.Screen

/**
 * Created by Zahi Chemaly on 09/12/2025.
 */
@Composable
fun MainScreen() {

    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        bottomBar = { NavBar(navController) }
    ) { innerPadding ->

        val graph =
            navController.createGraph(startDestination = Screen.Home.route) {
                composable(route = Screen.Home.route) {
                    HomeScreen()
                }
                composable(route = Screen.Schedule.route) {
                    ScheduleScreen()
                }
                composable(route = Screen.Library.route) {
                    LibraryScreen()
                }
                composable(route = Screen.Bookmark.route) {
                    BookmarkScreen()
                }
            }
        NavHost(
            navController = navController,
            graph = graph,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
