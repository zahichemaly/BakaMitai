package com.zc.bakamitai.app

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zc.bakamitai.common.ui.navigation.showSlugOrNull
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Thin Compose host; everything starts at [AppRoute]. It turns a new-episode notification's intent into a show to
 * open: on a fresh start (not a restored one, whose back stack is saved), and in [onNewIntent] while running.
 *
 * Edge to edge, with a fully transparent system navigation bar: the app's bottom bar already draws behind it, so
 * without a scrim the two read as one bar. With 3-button navigation, Android would otherwise lay a translucent
 * scrim over it (contrast enforcement on API 29+, the default scrim color below). The navigation bar's icons follow
 * the system's light/dark mode, like the app theme; when Settings adds a theme override, pass the app's dark state.
 */
class MainActivity : ComponentActivity() {

    private val showDeepLinks = Channel<String>(Channel.BUFFERED)
    private val showDeepLinkEvents: Flow<String> = showDeepLinks.receiveAsFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) window.isNavigationBarContrastEnforced = false
        if (savedInstanceState == null) intent.showSlugOrNull()?.let(showDeepLinks::trySend)
        setContent {
            AppRoute(showDeepLinks = showDeepLinkEvents)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.showSlugOrNull()?.let(showDeepLinks::trySend)
    }
}
