package com.zc.bakamitai.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.zc.bakamitai.compose.features.bookmark.BookmarkScreen
import com.zc.bakamitai.compose.features.details.navigation.onDetailsNavigation
import com.zc.bakamitai.compose.features.home.presentation.HomeScreen
import com.zc.bakamitai.compose.features.library.LibraryScreen
import com.zc.bakamitai.compose.features.schedule.ScheduleScreen
import com.zc.bakamitai.compose.navigation.AppBottomNavBar
import com.zc.bakamitai.compose.navigation.AppTopBar
import com.zc.bakamitai.compose.navigation.BookmarkRoute
import com.zc.bakamitai.compose.navigation.HomeRoute
import com.zc.bakamitai.compose.navigation.LibraryRoute
import com.zc.bakamitai.compose.navigation.ScheduleRoute
import com.zc.bakamitai.compose.navigation.TopLevelDestination
import com.zc.bakamitai.compose.navigation.rememberAppState
import com.zc.bakamitai.compose.navigation.toEntries

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

    val entryProvider = remember {
        entryProvider {
            entry<HomeRoute> {
                HomeScreen(appState.navigator)
            }
            entry<ScheduleRoute> {
                ScheduleScreen()
            }
            entry<LibraryRoute> {
                LibraryScreen()
            }
            entry<BookmarkRoute> {
                BookmarkScreen()
            }
            this.onDetailsNavigation()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        bottomBar = {
            if (appState.shouldShowBottomBar) {
                AppBottomNavBar(
                    currentRoute = appState.navigationState.topLevelRoute,
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
        NavDisplay(
            modifier = Modifier.padding(innerPadding),
            entries = appState.navigationState.toEntries(entryProvider),
            onBack = { appState.navigator.goBack() }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    MainScreen()
}
