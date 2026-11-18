package com.zc.bakamitai.feature.home.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zc.bakamitai.common.ui.util.ObserveAsEvents
import org.koin.compose.viewmodel.koinViewModel

/**
 * Home's navigation destination.
 *
 * @param onOpenDetails opens a show by slug; `null` while the Details screen doesn't exist (cards can't be opened).
 * @param onOpenSettings opens Settings; `null` while Settings doesn't exist (the action is hidden).
 */
@Composable
fun HomeRoute(
    onOpenDetails: ((slug: String) -> Unit)?,
    onOpenSettings: (() -> Unit)?,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LifecycleStartEffect(viewModel) {
        viewModel.onIntent(HomeIntent.ScreenStarted)
        onStopOrDispose {}
    }

    ObserveAsEvents(viewModel.effect) { effect ->
        when (effect) {
            is HomeEffect.OpenDetails -> onOpenDetails?.invoke(effect.slug)
            HomeEffect.OpenSettings -> onOpenSettings?.invoke()
            is HomeEffect.ShowError -> snackbarHostState.showSnackbar(effect.error.message.asString(context))
        }
    }

    HomeScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        canOpenShow = onOpenDetails != null,
        showSettingsAction = onOpenSettings != null,
        snackbarHostState = snackbarHostState,
    )
}
