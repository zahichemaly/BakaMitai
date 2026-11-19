package com.zc.bakamitai.feature.settings.presentation

import android.Manifest
import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.util.ObserveAsEvents
import com.zc.bakamitai.common.ui.util.canAskNotificationPermission
import com.zc.bakamitai.common.ui.util.openAppNotificationSettings
import com.zc.bakamitai.common.ui.util.openLink
import com.zc.bakamitai.common.ui.util.rememberNotificationPermissionRequester
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

/**
 * Settings' navigation destination. It opens the source code page, the notification permission dialog (or the
 * app's notification settings) and snackbars itself, in response to effects. Starting the screen (again) re-reads
 * the alerts status: the user may come back from the system settings.
 *
 * @param onNavigateBack leaves Settings: the back arrow.
 */
@Composable
fun SettingsRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val requestNotifications = rememberNotificationEnabler { granted ->
        viewModel.onIntent(SettingsIntent.NotificationPermissionResult(granted))
    }

    LifecycleStartEffect(viewModel) {
        viewModel.onIntent(SettingsIntent.ScreenStarted)
        onStopOrDispose {}
    }

    ObserveAsEvents(viewModel.effect) { effect ->
        when (effect) {
            SettingsEffect.NavigateBack -> onNavigateBack()

            is SettingsEffect.OpenUrl -> if (!context.openLink(effect.url)) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        UiText.StringResource(StringResource.settings_no_browser).asString(context),
                    )
                }
            }

            SettingsEffect.RequestNotifications -> requestNotifications()

            is SettingsEffect.ShowError -> scope.launch {
                snackbarHostState.showSnackbar(effect.error.message.asString(context))
            }
        }
    }

    SettingsScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )
}

/**
 * A function that lets the app post notifications: the permission dialog while Android still shows it (Android
 * 13+), otherwise the app's notification settings. [onResult] gets the dialog's answer.
 *
 * Android stops showing the dialog after two denials and denies at once. That case is told apart from a denial in
 * the dialog by the rationale flag, which a denial in the dialog changes and an instant denial doesn't: then the
 * notification settings open instead.
 */
@Composable
private fun rememberNotificationEnabler(onResult: (granted: Boolean) -> Unit): () -> Unit {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val currentOnResult by rememberUpdatedState(onResult)
    val rationaleBeforeRequest = remember { RationaleHolder() }
    val requestPermission = rememberNotificationPermissionRequester { granted ->
        val rationaleAfterRequest = activity?.shouldShowNotificationRationale() ?: true
        if (!granted && !rationaleBeforeRequest.value && !rationaleAfterRequest) {
            context.openAppNotificationSettings()
        }
        currentOnResult(granted)
    }
    return remember(context, activity, requestPermission) {
        {
            if (context.canAskNotificationPermission()) {
                rationaleBeforeRequest.value = activity?.shouldShowNotificationRationale() ?: true
                requestPermission()
            } else {
                context.openAppNotificationSettings()
            }
        }
    }
}

/** Whether Android shows a rationale for the notification permission, i.e. the user denied it once in the dialog. */
private fun Activity.shouldShowNotificationRationale(): Boolean =
    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)

/** The rationale flag read just before the last permission request; not state (it never drives the UI). */
private class RationaleHolder(var value: Boolean = false)
