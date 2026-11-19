package com.zc.bakamitai.common.ui.navigation

import android.content.Context
import android.content.Intent

/** The extra holding the slug of the show a notification opens. */
const val EXTRA_SHOW_SLUG = "com.zc.bakamitai.extra.SHOW_SLUG"

/**
 * The intent that opens a show's Details from a notification: the app's launcher activity, found through the
 * package manager so `common` never imports `app`, with [slug] in [EXTRA_SHOW_SLUG]. It brings a running app to
 * the front (the activity gets it in `onNewIntent`) or starts it. Only the component is kept from the launch
 * intent, so it never matches the launcher's own intent.
 */
fun Context.showDetailsIntent(slug: String): Intent {
    val component = packageManager.getLaunchIntentForPackage(packageName)?.component
    return Intent()
        .setPackage(packageName)
        .apply { if (component != null) setComponent(component) }
        .putExtra(EXTRA_SHOW_SLUG, slug)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
}

/**
 * The show to open from this intent, or `null`: no or a blank slug, or an intent replayed from Recents
 * (`FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY`), which must not reopen Details.
 */
fun Intent.showSlugOrNull(): String? {
    if (flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0) return null
    return getStringExtra(EXTRA_SHOW_SLUG)?.takeIf { it.isNotBlank() }
}
