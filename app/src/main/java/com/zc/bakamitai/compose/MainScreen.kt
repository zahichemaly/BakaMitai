package com.zc.bakamitai.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import com.zc.bakamitai.compose.features.bookmark.BookmarkScreen
import com.zc.bakamitai.compose.features.details.onDetailsGraph
import com.zc.bakamitai.compose.features.home.presentation.HomeScreen
import com.zc.bakamitai.compose.features.library.LibraryScreen
import com.zc.bakamitai.compose.features.schedule.ScheduleScreen
import com.zc.bakamitai.compose.navigation.AppBottomNavBar
import com.zc.bakamitai.compose.navigation.AppTopBar
import com.zc.bakamitai.compose.navigation.MAIN_GRAPH
import com.zc.bakamitai.compose.navigation.TopLevelDestination
import com.zc.bakamitai.compose.navigation.rememberAppState

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
    val appState = rememberAppState()
    val currentTopLevelDestination = appState.currentTopLevelDestination ?: TopLevelDestination.Home

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        bottomBar = {
            if (appState.shouldShowBottomBar) {
                AppBottomNavBar(
                    onNavigate = { topLevelDestination ->
                        appState.navigateToTopLevelDestination(topLevelDestination)
                    })
            }
        },
        topBar = {
            if (appState.shouldShowBottomBar) {
                AppTopBar(topLevelDestination = currentTopLevelDestination)
            }
        }
    ) { innerPadding ->

        val graph = appState.navController.createGraph(
            startDestination = TopLevelDestination.Home.route,
            route = MAIN_GRAPH,
        ) {
            composable(route = TopLevelDestination.Home.route) {
                HomeScreen(appState.navController)
            }
            composable(route = TopLevelDestination.Schedule.route) {
                ScheduleScreen()
            }
            composable(route = TopLevelDestination.Library.route) {
                LibraryScreen()
            }
            composable(route = TopLevelDestination.Bookmark.route) {
                BookmarkScreen()
            }
            onDetailsGraph()
        }
        NavHost(
            navController = appState.navController,
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
