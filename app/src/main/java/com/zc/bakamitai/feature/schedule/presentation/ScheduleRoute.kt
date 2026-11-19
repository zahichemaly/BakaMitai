package com.zc.bakamitai.feature.schedule.presentation

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
 * Schedule's navigation destination.
 *
 * @param onOpenDetails opens a show's Details by slug.
 * @param onOpenSettings opens Settings; `null` while Settings doesn't exist (the action is hidden).
 */
@Composable
fun ScheduleRoute(
    onOpenDetails: (slug: String) -> Unit,
    onOpenSettings: (() -> Unit)?,
    modifier: Modifier = Modifier,
    viewModel: ScheduleViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LifecycleStartEffect(viewModel) {
        viewModel.onIntent(ScheduleIntent.ScreenStarted)
        onStopOrDispose {}
    }

    ObserveAsEvents(viewModel.effect) { effect ->
        when (effect) {
            is ScheduleEffect.OpenDetails -> onOpenDetails(effect.slug)
            ScheduleEffect.OpenSettings -> onOpenSettings?.invoke()
            is ScheduleEffect.ShowError -> snackbarHostState.showSnackbar(effect.error.message.asString(context))
        }
    }

    ScheduleScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        showSettingsAction = onOpenSettings != null,
        snackbarHostState = snackbarHostState,
    )
}
