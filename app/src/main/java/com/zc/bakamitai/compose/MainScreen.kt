package com.zc.bakamitai.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import com.zc.bakamitai.compose.features.bookmark.BookmarkScreen
import com.zc.bakamitai.compose.features.details.onDetailsGraph
import com.zc.bakamitai.compose.features.home.presentation.HomeScreen
import com.zc.bakamitai.compose.features.library.LibraryScreen
import com.zc.bakamitai.compose.features.schedule.ScheduleScreen
import com.zc.bakamitai.compose.navigation.HomeDestination
import com.zc.bakamitai.compose.navigation.HomeGraph
import com.zc.bakamitai.compose.navigation.navigationItems
import com.zc.bakamitai.ui.main.AppBar
import com.zc.bakamitai.ui.main.AppBarMenuAction
import timber.log.Timber

/**
 * Created by Zahi Chemaly on 09/12/2025.
 */

@Composable
fun MainScreen() {
    AppTheme {
        MainScreenContent()
    }
}

@Composable
fun MainScreenContent() {
    val navController = rememberNavController()
    val currentNavItem = remember {
        mutableStateOf(navigationItems.first())
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    Timber.d("Current destination: $currentDestination")
    Timber.d("Current nav item: ${currentNavItem.value.title}")
    Timber.d("Current route: ${currentDestination?.route}")
    Timber.d("Current arguments: ${navBackStackEntry?.arguments}")

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
            AppBar(currentNavItem.value.title.asString()) { onMenuAction ->
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
            navController.createGraph(
                startDestination = HomeDestination.Home.route,
                route = HomeGraph.route
            ) {
                composable(route = HomeDestination.Home.route) {
                    HomeScreen(navController)
                }
                composable(route = HomeDestination.Schedule.route) {
                    ScheduleScreen()
                }
                composable(route = HomeDestination.Library.route) {
                    LibraryScreen()
                }
                composable(route = HomeDestination.Bookmark.route) {
                    BookmarkScreen()
                }
                onDetailsGraph()
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
