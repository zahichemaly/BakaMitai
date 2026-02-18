package com.zc.bakamitai.compose.features.details.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.zc.bakamitai.compose.features.details.presentation.DetailsScreen
import com.zc.bakamitai.compose.features.details.presentation.DetailsUiModel
import kotlinx.serialization.Serializable

@Serializable
data class DetailsNavRoute(
    val title: String,
    val summary: String,
    val imageUrl: String,
)

fun NavController.navigateToDetailsScreen(
    params: DetailsUiModel, navOptions: NavOptions? = null
) {
    navigate(
        DetailsNavRoute(
            title = params.title,
            summary = params.summary,
            imageUrl = params.imageUrl
        ),
        navOptions = navOptions
    )
}

internal fun NavGraphBuilder.onDetailsNavigation() {
    composable<DetailsNavRoute>(
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
    ) { backStackEntry ->

        val route = backStackEntry.toRoute<DetailsNavRoute>()

        val params = DetailsUiModel(
            title = route.title,
            summary = route.summary,
            imageUrl = route.imageUrl,
            downloads = emptyList()
        )

        DetailsScreen(
            detailsUiModel = params
        )
    }
}
