package com.zc.bakamitai.compose.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey

@Stable
class AppState(
    val navigationState: NavigationState,
    val navigator: Navigator,
) {
    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() {
            return TopLevelDestination.entries.firstOrNull { it.route == navigationState.topLevelRoute }
        }

    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        navigator.navigate(topLevelDestination.route)
    }

    val shouldShowBottomBar: Boolean
        @Composable
        get() = currentTopLevelDestination != null
}

@Composable
fun rememberAppState(
    startRoute: NavKey = HomeRoute,
    topLevelRoutes: Set<NavKey> = TopLevelDestination.entries.map { it.route }.toSet()
): AppState {
    val navigationState = rememberNavigationState(
        startRoute = startRoute,
        topLevelRoutes = topLevelRoutes
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }
    return remember(navigationState, navigator) {
        AppState(navigationState, navigator)
    }
}
