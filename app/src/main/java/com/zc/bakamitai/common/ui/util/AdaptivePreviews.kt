package com.zc.bakamitai.common.ui.util

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Preview

/** Light and dark, at compact (phone) and expanded (tablet) widths. Required on every Screen and View. */
@Preview(name = "Compact · Light", widthDp = 360, heightDp = 800, showBackground = true)
@Preview(
    name = "Compact · Dark",
    widthDp = 360,
    heightDp = 800,
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
)
@Preview(name = "Expanded · Light", widthDp = 1280, heightDp = 800, showBackground = true)
@Preview(
    name = "Expanded · Dark",
    widthDp = 1280,
    heightDp = 800,
    showBackground = true,
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
)
annotation class AdaptivePreviews
