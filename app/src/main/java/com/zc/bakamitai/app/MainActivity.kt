package com.zc.bakamitai.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zc.bakamitai.common.ui.navigation.showSlugOrNull
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Thin Compose host; everything starts at [AppRoute]. It turns a new-episode notification's intent into a show to
 * open: on a fresh start (not a restored one, whose back stack is saved), and in [onNewIntent] while running.
 */
class MainActivity : ComponentActivity() {

    private val showDeepLinks = Channel<String>(Channel.BUFFERED)
    private val showDeepLinkEvents: Flow<String> = showDeepLinks.receiveAsFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
