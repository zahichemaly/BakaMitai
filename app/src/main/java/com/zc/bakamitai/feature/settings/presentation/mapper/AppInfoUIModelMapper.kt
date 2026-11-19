package com.zc.bakamitai.feature.settings.presentation.mapper

import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.settings.domain.model.AppInfo

/** "BakaMitai v1.0". */
fun AppInfo.toVersionLabel(): UiText = UiText.StringResource(
    StringResource.settings_footer_version,
    listOf(versionName),
)
