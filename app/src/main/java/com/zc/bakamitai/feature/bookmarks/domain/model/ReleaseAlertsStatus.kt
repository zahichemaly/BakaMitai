package com.zc.bakamitai.feature.bookmarks.domain.model

/**
 * Whether new-episode alerts are posted: the Settings switch combined with Android's permission and notification
 * settings for the app.
 */
enum class ReleaseAlertsStatus {
    /** Switched on, and Android lets the app post. */
    ON,

    /** Switched off in Settings. */
    OFF,

    /** Switched on, but Android blocks the app's notifications (permission denied, app or channel muted). */
    BLOCKED,
    ;

    companion object {
        fun of(isSwitchedOn: Boolean, isSystemAllowed: Boolean): ReleaseAlertsStatus = when {
            !isSwitchedOn -> OFF
            !isSystemAllowed -> BLOCKED
            else -> ON
        }
    }
}
