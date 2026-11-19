package com.zc.bakamitai.common.ui.navigation

import androidx.navigation3.runtime.NavKey

/**
 * Handles forward and back navigation by updating [state]. Lives in `app/navigation/`; features never see it,
 * they get navigation lambdas built from it in `AppEntryProvider.kt`.
 */
class Navigator(val state: NavigationState) {

    /**
     * For a top-level route: selects its tab, or, when that tab is already selected, pops its stack back to the
     * root (reselecting a tab, e.g. from the rail while Details is open). For any other key: pushes it onto the
     * selected tab's stack, unless it's already on top (a double tap opens one screen).
     */
    fun navigate(key: NavKey) {
        if (key in state.backStacks.keys) {
            if (key == state.topLevelRoute) {
                val stack = state.backStacks.getValue(key)
                while (stack.size > 1) stack.removeAt(stack.lastIndex)
            } else {
                state.topLevelRoute = key
            }
        } else {
            val stack = state.backStacks.getValue(state.topLevelRoute)
            if (stack.lastOrNull() != key) stack.add(key)
        }
    }

    /**
     * Pops the selected tab's stack. At the root of a tab other than the start route, returns to the start
     * route instead ("exit through home"). At the root of the start route, does nothing: `NavDisplay` leaves the
     * app.
     */
    fun goBack() {
        val stack = state.backStacks.getValue(state.topLevelRoute)
        when {
            stack.size > 1 -> stack.removeAt(stack.lastIndex)
            state.topLevelRoute != state.startRoute -> state.topLevelRoute = state.startRoute
        }
    }
}
