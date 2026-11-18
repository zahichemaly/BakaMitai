package com.zc.bakamitai.app.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItemColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.NavDisplay
import com.zc.bakamitai.common.ui.navigation.Navigator
import com.zc.bakamitai.common.ui.navigation.rememberNavigationState
import com.zc.bakamitai.feature.home.presentation.navigation.HomeNavKey

private const val NAV_INDICATOR_ALPHA = 0.2f

/**
 * The app shell: the navigation suite (bottom bar on compact widths, rail from medium) around the
 * selected tab's back stack. Tabs whose feature isn't built yet are shown disabled.
 */
@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val topLevelRoutes = remember { TopLevelDestination.entries.mapNotNull { it.key }.toSet() }
    val navigationState = rememberNavigationState(startRoute = HomeNavKey, topLevelRoutes = topLevelRoutes)
    val navigator = remember(navigationState) { Navigator(navigationState) }
    val entryProvider = remember { appEntryProvider() }
    val itemColors = navigationSuiteItemColors()

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
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            navigationRailContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
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
