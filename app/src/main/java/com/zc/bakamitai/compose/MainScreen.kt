package com.zc.bakamitai.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.zc.bakamitai.compose.features.bookmark.BookmarkScreen
import com.zc.bakamitai.compose.features.home.presentation.HomeScreen
import com.zc.bakamitai.compose.features.library.LibraryScreen
import com.zc.bakamitai.compose.features.schedule.ScheduleScreen
import com.zc.bakamitai.compose.navigation.Screen
import com.zc.bakamitai.compose.navigation.navigationItems
import com.zc.bakamitai.ui.main.AppBar
import com.zc.bakamitai.ui.main.AppBarMenuAction
import timber.log.Timber

/**
 * Created by Zahi Chemaly on 09/12/2025.
 */
@Composable
fun MainScreen() {

    val navController = rememberNavController()
    val currentNavItem = remember {
        mutableStateOf(navigationItems.first())
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        bottomBar = {
            NavBar(onNavigate = { navigationItem ->
                currentNavItem.value = navigationItem
                navController.navigate(navigationItem.route)
            })
        },
        topBar = {
            AppBar(currentNavItem.value.title) { onMenuAction ->
                Timber.d("Selected app bar menu action: $onMenuAction")
                when (onMenuAction) {
                    AppBarMenuAction.Search -> {

                    }

                    AppBarMenuAction.Refresh -> {

                    }

                    AppBarMenuAction.Settings -> {

                    }
                }
            }
        }
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

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    MainScreen()
}
