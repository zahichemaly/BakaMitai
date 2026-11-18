package com.zc.bakamitai.common.ui.navigation

import androidx.navigation3.runtime.NavKey

/**
 * Handles forward and back navigation by updating [state]. Lives in `app/navigation/`; features never see it,
 * they get navigation lambdas built from it in `AppEntryProvider.kt`.
 */
class Navigator(val state: NavigationState) {

    /** Selects [key] when it is a top-level route, otherwise pushes it onto the selected tab's stack. */
    fun navigate(key: NavKey) {
        if (key in state.backStacks.keys) {
            state.topLevelRoute = key
        } else {
            state.backStacks.getValue(state.topLevelRoute).add(key)
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
