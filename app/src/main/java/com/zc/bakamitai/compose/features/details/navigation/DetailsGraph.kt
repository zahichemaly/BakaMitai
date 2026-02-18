package com.zc.bakamitai.compose.features.details.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.navigation
import kotlinx.serialization.Serializable

@Serializable
object DetailsGraph

fun NavGraphBuilder.onDetailsGraph() {
    navigation<DetailsGraph>(
        startDestination = DetailsNavRoute::class
    ) {
        onDetailsNavigation()
    }
}
