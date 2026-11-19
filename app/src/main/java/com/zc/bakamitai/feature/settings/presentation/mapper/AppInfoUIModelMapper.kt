package com.zc.bakamitai.feature.settings.presentation.mapper

import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.settings.domain.model.AppInfo

private const val SCHEME_SEPARATOR = "://"

/** The source code URL without its scheme: "github.com/zahichemaly/BakaMitai". */
fun AppInfo.toSourceCodeLabel(): UiText = UiText.StringValue(sourceCodeUrl.substringAfter(SCHEME_SEPARATOR))

/** "BakaMitai v1.0". */
fun AppInfo.toVersionLabel(): UiText = UiText.StringResource(
    StringResource.settings_footer_version,
    listOf(versionName),
)
