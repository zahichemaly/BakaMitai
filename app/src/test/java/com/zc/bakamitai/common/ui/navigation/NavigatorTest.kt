package com.zc.bakamitai.common.ui.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals

class NavigatorTest {

    private data object Home : NavKey

    private data object Schedule : NavKey

    private data class Details(val slug: String) : NavKey

    private val state = NavigationState(
        startRoute = Home,
        topLevelRoute = mutableStateOf(Home),
        backStacks = mapOf<NavKey, NavBackStack<NavKey>>(
            Home to NavBackStack(Home),
            Schedule to NavBackStack(Schedule),
        ),
    )
    private val navigator = Navigator(state)

    @Test
    fun `starts on the start route only`() {
        assertEquals(listOf<NavKey>(Home), state.visibleKeys)
    }

    @Test
    fun `navigating to a top-level route selects its tab above the start stack`() {
        navigator.navigate(Schedule)

        assertEquals(Schedule, state.topLevelRoute)
        assertEquals(listOf(Home, Schedule), state.visibleKeys)
    }

    @Test
    fun `navigating to another key pushes it onto the selected tab`() {
        navigator.navigate(Schedule)
        navigator.navigate(Details("frieren"))

        assertEquals(listOf(Schedule, Details("frieren")), state.backStacks.getValue(Schedule).toList())
        assertEquals(listOf<NavKey>(Home), state.backStacks.getValue(Home).toList())
    }

    @Test
    fun `a tab keeps its stack while another tab is selected`() {
        navigator.navigate(Schedule)
        navigator.navigate(Details("frieren"))
        navigator.navigate(Home)
        navigator.navigate(Schedule)

        assertEquals(listOf(Home, Schedule, Details("frieren")), state.visibleKeys)
    }

    @Test
    fun `pushing the key already on top does nothing`() {
        navigator.navigate(Details("frieren"))
        navigator.navigate(Details("frieren"))

        assertEquals(listOf(Home, Details("frieren")), state.visibleKeys)
        navigator.goBack()
        assertEquals(listOf<NavKey>(Home), state.visibleKeys)
    }

    @Test
    fun `pushing a different key on top of another still pushes`() {
        navigator.navigate(Details("frieren"))
        navigator.navigate(Details("one-piece"))

        assertEquals(listOf(Home, Details("frieren"), Details("one-piece")), state.visibleKeys)
    }

    @Test
    fun `reselecting the selected tab pops its stack to the root`() {
        navigator.navigate(Schedule)
        navigator.navigate(Details("frieren"))
        navigator.navigate(Details("one-piece"))

        navigator.navigate(Schedule)

        assertEquals(Schedule, state.topLevelRoute)
        assertEquals(listOf<NavKey>(Schedule), state.backStacks.getValue(Schedule).toList())
        assertEquals(listOf(Home, Schedule), state.visibleKeys)
    }

    @Test
    fun `reselecting the start tab pops its stack to the root`() {
        navigator.navigate(Details("frieren"))

        navigator.navigate(Home)

        assertEquals(listOf<NavKey>(Home), state.visibleKeys)
    }

    @Test
    fun `selecting another tab keeps both stacks`() {
        navigator.navigate(Details("frieren"))
        navigator.navigate(Schedule)
        navigator.navigate(Details("one-piece"))

        navigator.navigate(Home)

        assertEquals(listOf(Home, Details("frieren")), state.backStacks.getValue(Home).toList())
        assertEquals(listOf(Schedule, Details("one-piece")), state.backStacks.getValue(Schedule).toList())
    }

    @Test
    fun `back pops the selected tab`() {
        navigator.navigate(Details("frieren"))
        navigator.goBack()

        assertEquals(listOf<NavKey>(Home), state.visibleKeys)
    }

    @Test
    fun `back at the root of another tab returns to the start route`() {
        navigator.navigate(Schedule)
        navigator.goBack()

        assertEquals(Home, state.topLevelRoute)
        assertEquals(listOf<NavKey>(Home), state.visibleKeys)
    }

    @Test
    fun `back at the root of the start route does nothing`() {
        navigator.goBack()

        assertEquals(Home, state.topLevelRoute)
        assertEquals(listOf<NavKey>(Home), state.visibleKeys)
    }
}
