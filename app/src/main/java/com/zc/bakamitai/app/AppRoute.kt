package com.zc.bakamitai.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.zc.bakamitai.app.navigation.AppNavigation
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme

/**
 * App root: applies the theme around the navigation shell. When the Settings feature adds `AppViewModel`, the
 * theme setting is read here.
 */
@Composable
fun AppRoute(modifier: Modifier = Modifier) {
    BakaMitaiTheme {
        AppNavigation(modifier = modifier.fillMaxSize())
    }
}
