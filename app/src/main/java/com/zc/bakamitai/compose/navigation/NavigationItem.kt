package com.zc.bakamitai.compose.navigation

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import com.zc.bakamitai.compose.common.UiText

/**
 * Created by Zahi Chemaly on 09/12/2025.
 */
@Stable
data class NavigationItem(
    val title: UiText,
    val icon: ImageVector,
    val route: String
)

