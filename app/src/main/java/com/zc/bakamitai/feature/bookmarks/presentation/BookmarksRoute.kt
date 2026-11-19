package com.zc.bakamitai.feature.bookmarks.presentation

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.util.ObserveAsEvents
import com.zc.bakamitai.common.ui.util.canAskNotificationPermission
import com.zc.bakamitai.common.ui.util.openAppNotificationSettings
import com.zc.bakamitai.common.ui.util.rememberNotificationPermissionRequester
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

/**
 * Bookmarks' navigation destination. It shows the snackbars, the notification permission dialog and the system
 * notification settings itself, in response to effects; snackbars run in their own coroutine so they never hold
 * back navigation. Starting the screen (again) re-reads the alerts state: the user may come back from the settings.
 *
 * @param onOpenDetails opens a show's Details by slug.
 * @param onDiscoverShows opens the Schedule tab (the empty state's action).
 * @param onOpenSettings opens Settings: the top bar's gear, and the alerts chip while alerts are on or switched off.
 */
@Composable
fun BookmarksRoute(
    onOpenDetails: (slug: String) -> Unit,
    onDiscoverShows: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookmarksViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val requestPermission = rememberNotificationPermissionRequester { granted ->
        viewModel.onIntent(BookmarksIntent.AlertsPermissionResult(granted))
    }

    LifecycleStartEffect(viewModel) {
        viewModel.onIntent(BookmarksIntent.ScreenStarted)
        onStopOrDispose {}
    }

    ObserveAsEvents(viewModel.effect) { effect ->
        when (effect) {
            is BookmarksEffect.OpenDetails -> onOpenDetails(effect.slug)

            BookmarksEffect.OpenSchedule -> onDiscoverShows()

            BookmarksEffect.OpenSettings -> onOpenSettings()

            is BookmarksEffect.ShowRemoved -> scope.launch {
                val undone = snackbarHostState.showReplacing(
                    message = effect.message.asString(context),
                    actionLabel = UiText.StringResource(StringResource.bookmarks_undo).asString(context),
                )
                if (undone) viewModel.onIntent(BookmarksIntent.UndoRemoveClicked(effect.slug))
            }

            BookmarksEffect.EnableAlerts -> if (context.canAskNotificationPermission()) {
                requestPermission()
            } else {
                context.openAppNotificationSettings()
            }

            BookmarksEffect.ShowAlertsDenied -> scope.launch { snackbarHostState.offerSettings(context) }

            is BookmarksEffect.ShowError -> scope.launch {
                snackbarHostState.showReplacing(message = effect.error.message.asString(context), actionLabel = null)
            }
        }
    }

    BookmarksScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )
}

/** Shows [message] in place of the current snackbar (only the last removal can be undone); `true` on its action. */
private suspend fun SnackbarHostState.showReplacing(message: String, actionLabel: String?): Boolean {
    currentSnackbarData?.dismiss()
    return showSnackbar(message = message, actionLabel = actionLabel, duration = SnackbarDuration.Short) ==
        SnackbarResult.ActionPerformed
}

/** "Notifications are off for BakaMitai." with a "Settings" action that opens the notification settings. */
private suspend fun SnackbarHostState.offerSettings(context: Context) {
    val opened = showReplacing(
        message = UiText.StringResource(StringResource.bookmarks_alerts_denied).asString(context),
        actionLabel = UiText.StringResource(StringResource.bookmarks_alerts_settings).asString(context),
    )
    if (opened) context.openAppNotificationSettings()
}
