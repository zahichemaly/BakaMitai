package com.zc.bakamitai.app

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
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
 * The first frame is held until [AppRoute] has read the settings and applied the theme, so the app never flashes
 * the other theme (the system's splash still follows the device's dark mode).
 *
 * Edge to edge, with a fully transparent system navigation bar: the app's bottom bar already draws behind it, so
 * without a scrim the two read as one bar. With 3-button navigation, Android would otherwise lay a translucent
 * scrim over it (contrast enforcement on API 29+, which every `enableEdgeToEdge` call may turn back on). The status-
 * and navigation-bar icons follow the app's theme, which may be forced against the system's dark mode.
 */
class MainActivity : ComponentActivity() {

    private val showDeepLinks = Channel<String>(Channel.BUFFERED)
    private val showDeepLinkEvents: Flow<String> = showDeepLinks.receiveAsFlow()

    /** [AppRoute] has composed with the settings: the first frame may be drawn. */
    private var isThemeApplied = false

    /** The theme the system bars are styled for; `null` before the first composition. */
    private var appliedDarkTheme: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT))
        disableNavigationBarContrast()
        if (savedInstanceState == null) intent.showSlugOrNull()?.let(showDeepLinks::trySend)
        setContent {
            AppRoute(showDeepLinks = showDeepLinkEvents, onThemeApply = ::onThemeApplied)
        }
        holdFirstFrameUntilThemeApplied()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.showSlugOrNull()?.let(showDeepLinks::trySend)
    }

    /**
     * Cancels drawing until [onThemeApplied] has run once (the starting window stays up meanwhile). Compose keeps
     * recomposing during the cancelled frames, so the frame that is finally drawn is in the chosen theme.
     */
    private fun holdFirstFrameUntilThemeApplied() {
        val content = findViewById<View>(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    if (!isThemeApplied) return false
                    content.viewTreeObserver.removeOnPreDrawListener(this)
                    return true
                }
            },
        )
    }

    /** Styles the system bars for the app's theme: light icons on the dark theme, dark icons on the light one. */
    private fun onThemeApplied(isDarkTheme: Boolean) {
        isThemeApplied = true
        if (appliedDarkTheme == isDarkTheme) return
        appliedDarkTheme = isDarkTheme
        val style = if (isDarkTheme) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }
        enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
        disableNavigationBarContrast()
    }

    /** No scrim behind 3-button navigation: the bottom bar shows through (API 29+). */
    private fun disableNavigationBarContrast() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) window.isNavigationBarContrastEnforced = false
    }
}
