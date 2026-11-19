package com.zc.bakamitai.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zc.bakamitai.app.navigation.AppNavigation
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme
import kotlinx.coroutines.flow.Flow

/**
 * App root: applies the theme around the navigation shell. When the Settings feature adds `AppViewModel`, the
 * theme setting is read here.
 *
 * @param showDeepLinks slugs of shows to open from notifications, one event each.
 */
@Composable
fun AppRoute(showDeepLinks: Flow<String>, modifier: Modifier = Modifier) {
    BakaMitaiTheme {
        AppNavigation(showDeepLinks = showDeepLinks, modifier = modifier.fillMaxSize())
    }
}
