package com.zc.bakamitai.feature.details.presentation.navigation

import androidx.navigation3.runtime.NavKey
import com.zc.bakamitai.common.ui.navigation.FullScreenNavKey
import kotlinx.serialization.Serializable

/** One show's Details, pushed onto the current tab; it hides the bottom navigation bar. */
@Serializable
data class DetailsNavKey(val slug: String) :
    NavKey,
    FullScreenNavKey
