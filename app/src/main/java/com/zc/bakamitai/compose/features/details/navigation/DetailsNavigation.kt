package com.zc.bakamitai.compose.features.details.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.zc.bakamitai.compose.features.details.presentation.DetailsScreen
import com.zc.bakamitai.compose.features.details.presentation.DetailsUiModel
import kotlinx.serialization.Serializable

@Serializable
data class DetailsNavRoute(
    val title: String,
    val summary: String,
    val imageUrl: String,
) : NavKey

fun EntryProviderScope<NavKey>.onDetailsNavigation() {
    entry<DetailsNavRoute> { key ->
        val params = DetailsUiModel(
            title = key.title,
            summary = key.summary,
            imageUrl = key.imageUrl,
            downloads = emptyList()
        )

        DetailsScreen(
            detailsUiModel = params
        )
    }
}

