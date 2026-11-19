package com.zc.bakamitai.common.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

/** Opens [url] in whatever app handles it (the system asks when several do); `false` when none can. */
fun Context.openLink(url: String): Boolean = try {
    startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    true
} catch (ignored: ActivityNotFoundException) {
    false
}
