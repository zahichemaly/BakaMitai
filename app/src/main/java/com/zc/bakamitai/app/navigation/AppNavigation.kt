package com.zc.bakamitai.app.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItemColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.NavDisplay
import com.zc.bakamitai.common.ui.navigation.FullScreenNavKey
import com.zc.bakamitai.common.ui.navigation.Navigator
import com.zc.bakamitai.common.ui.navigation.rememberNavigationState
import com.zc.bakamitai.common.ui.util.ObserveAsEvents
import com.zc.bakamitai.feature.details.presentation.navigation.navigateToDetailsFromStart
import com.zc.bakamitai.feature.home.presentation.navigation.HomeNavKey
import kotlinx.coroutines.flow.Flow

private const val NAV_INDICATOR_ALPHA = 0.2f

/**
 * The app shell: the navigation suite (bottom bar on compact widths or heights, rail otherwise) around the
 * selected tab's back stack. A tab whose feature isn't built yet is shown disabled. The bottom bar hides while a
 * `FullScreenNavKey` (Details, Settings) is on top; reselecting the selected tab pops it back to its root.
 *
 * @param showDeepLinks slugs of shows to open from notifications: each opens the show's Details over Home.
 */
@Composable
fun AppNavigation(showDeepLinks: Flow<String>, modifier: Modifier = Modifier) {
    val topLevelRoutes = remember { TopLevelDestination.entries.mapNotNull { it.key }.toSet() }
    val navigationState = rememberNavigationState(startRoute = HomeNavKey, topLevelRoutes = topLevelRoutes)
    val navigator = remember(navigationState) { Navigator(navigationState) }
    val entryProvider = remember(navigator) { appEntryProvider(navigator) }
    ObserveAsEvents(showDeepLinks, navigator) { slug -> navigator.navigateToDetailsFromStart(slug) }
    val itemColors = navigationSuiteItemColors()
    // A full-screen key on top slides the bottom bar away; the rail of wider windows stays.
    val layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
    val suiteState = rememberNavigationSuiteScaffoldState()
    val hideBar by remember(navigationState, layoutType) {
        derivedStateOf {
            layoutType == NavigationSuiteType.NavigationBar &&
                navigationState.visibleKeys.lastOrNull() is FullScreenNavKey
        }
    }
    LaunchedEffect(suiteState, hideBar) {
        if (hideBar) suiteState.hide() else suiteState.show()
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { destination ->
                val key = destination.key
                val selected = key != null && key == navigationState.topLevelRoute
                item(
                    selected = selected,
                    onClick = { key?.let(navigator::navigate) },
                    icon = {
                        Icon(
                            imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = null,
                        )
                    },
                    enabled = key != null,
                    label = {
                        Text(text = destination.label.asString(), style = MaterialTheme.typography.labelMedium)
                    },
                    colors = itemColors,
                )
            }
        },
        modifier = modifier,
        layoutType = layoutType,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            navigationRailContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        state = suiteState,
    ) {
        NavDisplay(
            entries = navigationState.toDecoratedEntries(entryProvider),
            onBack = navigator::goBack,
        )
    }
}

/** Selected: primary icon and label on a translucent primary indicator. Unselected: on-surface-variant. */
@Composable
private fun navigationSuiteItemColors(): NavigationSuiteItemColors {
    val colors = MaterialTheme.colorScheme
    val indicator = colors.primary.copy(alpha = NAV_INDICATOR_ALPHA)
    return NavigationSuiteDefaults.itemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = colors.primary,
            selectedTextColor = colors.primary,
            indicatorColor = indicator,
            unselectedIconColor = colors.onSurfaceVariant,
            unselectedTextColor = colors.onSurfaceVariant,
        ),
        navigationRailItemColors = NavigationRailItemDefaults.colors(
            selectedIconColor = colors.primary,
            selectedTextColor = colors.primary,
            indicatorColor = indicator,
            unselectedIconColor = colors.onSurfaceVariant,
            unselectedTextColor = colors.onSurfaceVariant,
        ),
        navigationDrawerItemColors = NavigationDrawerItemDefaults.colors(
            selectedIconColor = colors.primary,
            selectedTextColor = colors.primary,
            selectedContainerColor = indicator,
            unselectedIconColor = colors.onSurfaceVariant,
            unselectedTextColor = colors.onSurfaceVariant,
        ),
    )
}
