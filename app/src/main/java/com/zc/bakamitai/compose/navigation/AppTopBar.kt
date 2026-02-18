package com.zc.bakamitai.compose.navigation

import androidx.compose.runtime.Composable
import com.zc.bakamitai.compose.common.UiText
import com.zc.bakamitai.ui.main.AppBar
import com.zc.bakamitai.ui.main.AppBarMenuAction
import timber.log.Timber

@Composable
fun AppTopBar(topLevelDestination: TopLevelDestination) {
    val title = UiText.StringResource(topLevelDestination.titleId).asString()
    AppBar(title) { onMenuAction ->
        Timber.d("Selected app bar menu action: $onMenuAction")
        when (onMenuAction) {
            AppBarMenuAction.Search -> {

            }

            AppBarMenuAction.Refresh -> {

            }

            AppBarMenuAction.Settings -> {

            }
        }
    }
}
