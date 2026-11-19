package com.zc.bakamitai.feature.shows.presentation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zc.bakamitai.common.ui.util.ObserveAsEvents
import org.koin.compose.viewmodel.koinViewModel

/**
 * All Shows' navigation destination.
 *
 * @param onOpenDetails opens a show by slug; `null` while the Details screen doesn't exist (rows can't be opened).
 * @param onOpenSettings opens Settings; `null` while Settings doesn't exist (the action is hidden).
 */
@Composable
fun AllShowsRoute(
    onOpenDetails: ((slug: String) -> Unit)?,
    onOpenSettings: (() -> Unit)?,
    modifier: Modifier = Modifier,
    viewModel: AllShowsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    ObserveAsEvents(viewModel.effect) { effect ->
        when (effect) {
            is AllShowsEffect.OpenDetails -> onOpenDetails?.invoke(effect.slug)
            AllShowsEffect.OpenSettings -> onOpenSettings?.invoke()
            is AllShowsEffect.ShowError -> snackbarHostState.showSnackbar(effect.error.message.asString(context))
        }
    }

    AllShowsScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        canOpenShow = onOpenDetails != null,
        showSettingsAction = onOpenSettings != null,
        snackbarHostState = snackbarHostState,
    )
}
