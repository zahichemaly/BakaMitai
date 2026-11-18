package com.zc.bakamitai.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.theme.BakaMitaiTheme

/**
 * App root: applies the theme around the app's content. Until the first feature adds `AppNavigation` (and
 * the settings feature adds `AppViewModel` for the theme setting), the content is the wordmark.
 */
@Composable
fun AppRoute(modifier: Modifier = Modifier) {
    BakaMitaiTheme {
        Surface(modifier = modifier.fillMaxSize()) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = UiText.StringResource(StringResource.app_name).asString(),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
