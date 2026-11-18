package com.zc.bakamitai.common.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer

/**
 * Creates the app's [NavigationState], kept across configuration changes and process death.
 *
 * @param startRoute the top-level route the app opens on and exits through; must be in [topLevelRoutes].
 * @param topLevelRoutes one key per top-level destination (tab); each gets its own back stack.
 */
@Composable
fun rememberNavigationState(startRoute: NavKey, topLevelRoutes: Set<NavKey>): NavigationState {
    val topLevelRoute = rememberSerializable(
        startRoute,
        topLevelRoutes,
        serializer = MutableStateSerializer(NavKeySerializer()),
    ) {
        mutableStateOf(startRoute)
    }
    val backStacks = topLevelRoutes.associateWith { key -> rememberNavBackStack(key) }

    return remember(startRoute, topLevelRoutes) {
        NavigationState(
            startRoute = startRoute,
            topLevelRoute = topLevelRoute,
            backStacks = backStacks,
        )
    }
}

/**
 * Multiple back stacks, one per top-level destination, following the "exit through home" pattern: the
 * [startRoute] stack is always shown first, plus at most the selected [topLevelRoute] stack. Only [Navigator]
 * changes it.
 *
 * @param startRoute the top-level route the user exits the app through.
 * @param topLevelRoute the state backing the selected top-level route.
 * @param backStacks the back stack of each top-level route. A tab keeps its stack while another is selected.
 */
class NavigationState(
    val startRoute: NavKey,
    topLevelRoute: MutableState<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>,
) {

    /** The selected top-level route. */
    var topLevelRoute: NavKey by topLevelRoute

    /** Keys currently displayed, bottom to top: the start stack, then the selected stack if it differs. */
    val visibleKeys: List<NavKey>
        get() = topLevelRoutesInUse().flatMap { backStacks[it].orEmpty() }

    /**
     * The visible entries for `NavDisplay`. Each back stack has its own saved-state and ViewModel-store
     * decorators, so every entry keeps its UI state and ViewModel while its tab is in the background.
     */
    @Composable
    fun toDecoratedEntries(entryProvider: (NavKey) -> NavEntry<NavKey>): List<NavEntry<NavKey>> {
        val decoratedEntries = backStacks.mapValues { (_, stack) ->
            rememberDecoratedNavEntries(
                backStack = stack,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider,
            )
        }
        return topLevelRoutesInUse().flatMap { decoratedEntries[it].orEmpty() }
    }

    private fun topLevelRoutesInUse(): List<NavKey> =
        if (topLevelRoute == startRoute) listOf(startRoute) else listOf(startRoute, topLevelRoute)
}
