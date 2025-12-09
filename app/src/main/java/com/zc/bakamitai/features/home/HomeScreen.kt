package com.zc.bakamitai.features.home

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Created by Zahi Chemaly on 04/12/2025.
 */
@Composable
fun HomeScreen() {
    LatestReleaseItem()
}

@Composable
fun LatestReleaseItem(modifier: Modifier = Modifier) {
    Text(modifier = modifier, text = "Hello")
}
