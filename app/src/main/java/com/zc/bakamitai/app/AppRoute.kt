package com.zc.bakamitai.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zc.bakamitai.app.navigation.AppNavigation
import com.zc.bakamitai.common.ui.text.LocalUse24HourClock
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import kotlinx.coroutines.flow.Flow
import org.koin.compose.viewmodel.koinViewModel

/**
 * App root: applies the user's theme and clock format around the navigation shell. It draws nothing until the
 * settings are read (a few milliseconds; `MainActivity` holds the first frame meanwhile), so the first frame is
 * already in the chosen theme. Then every change applies at once, everywhere.
 *
 * @param showDeepLinks slugs of shows to open from notifications, one event each.
 * @param onThemeApply called after each composition with whether the app is dark, so the Activity can style the
 * system bars to match and release the first frame.
 */
@Composable
fun AppRoute(
    showDeepLinks: Flow<String>,
    onThemeApply: (isDarkTheme: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (!state.isLoaded) return
    val isDarkTheme = state.darkThemeOverride ?: isSystemInDarkTheme()
    SideEffect { onThemeApply(isDarkTheme) }
    BakaMitaiTheme(darkTheme = isDarkTheme) {
        CompositionLocalProvider(LocalUse24HourClock provides state.use24HourClock) {
            AppNavigation(showDeepLinks = showDeepLinks, modifier = modifier.fillMaxSize())
        }
    }
}
