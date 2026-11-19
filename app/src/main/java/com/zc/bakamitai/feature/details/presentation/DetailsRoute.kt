package com.zc.bakamitai.feature.details.presentation

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.common.ui.util.ObserveAsEvents
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Details' navigation destination. It opens links, the share sheet and snackbars itself, in response to effects.
 * Snackbars run in their own coroutine, so a 10-second one never holds back a later effect such as going back.
 *
 * @param slug the show to load.
 */
@Composable
fun DetailsRoute(
    slug: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailsViewModel = koinViewModel { parametersOf(slug) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.effect) { effect ->
        when (effect) {
            DetailsEffect.NavigateBack -> onNavigateBack()

            is DetailsEffect.OpenTorrentLink -> if (!context.openLink(effect.url)) {
                scope.launch {
                    snackbarHostState.offerCopy(
                        message = UiText.StringResource(StringResource.details_no_torrent_app),
                        url = effect.url,
                        context = context,
                        clipboard = clipboard,
                    )
                }
            }

            is DetailsEffect.OpenWebLink -> if (!context.openLink(effect.url)) {
                scope.launch {
                    snackbarHostState.offerCopy(
                        message = UiText.StringResource(StringResource.details_no_app_for_link),
                        url = effect.url,
                        context = context,
                        clipboard = clipboard,
                    )
                }
            }

            is DetailsEffect.Share ->
                context.share(title = effect.title.asString(context), text = effect.text.asString(context))

            is DetailsEffect.ShowError -> scope.launch {
                snackbarHostState.showSnackbar(effect.error.message.asString(context))
            }
        }
    }

    DetailsScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )
}

/** Opens [url] in whatever app handles it (the system asks when several do); `false` when none can. */
private fun Context.openLink(url: String): Boolean = try {
    startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    true
} catch (ignored: ActivityNotFoundException) {
    false
}

/** The system share sheet with [text], titled [title]. */
private fun Context.share(title: String, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = SHARE_MIME_TYPE
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_TITLE, title)
    }
    startActivity(Intent.createChooser(send, null))
}

/**
 * A long snackbar with [message] and "Copy link", which copies [url]. Android 13 and later confirm the copy
 * themselves; earlier versions get a "Link copied" snackbar.
 */
private suspend fun SnackbarHostState.offerCopy(message: UiText, url: String, context: Context, clipboard: Clipboard) {
    val result = showSnackbar(
        message = message.asString(context),
        actionLabel = UiText.StringResource(StringResource.details_copy_link).asString(context),
        duration = SnackbarDuration.Long,
    )
    if (result == SnackbarResult.ActionPerformed) {
        val label = UiText.StringResource(StringResource.details_clip_label).asString(context)
        clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(label, url)))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            showSnackbar(UiText.StringResource(StringResource.details_link_copied).asString(context))
        }
    }
}

private const val SHARE_MIME_TYPE = "text/plain"
