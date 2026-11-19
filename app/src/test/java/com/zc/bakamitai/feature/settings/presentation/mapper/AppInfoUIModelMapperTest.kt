package com.zc.bakamitai.feature.settings.presentation.mapper

import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.settings.domain.model.AppInfo
import kotlin.test.Test
import kotlin.test.assertEquals

class AppInfoUIModelMapperTest {

    private val appInfo = AppInfo(versionName = "1.0", sourceCodeUrl = "https://github.com/zahichemaly/BakaMitai")

    @Test
    fun `the version label is BakaMitai v plus the version name`() {
        assertEquals(
            UiText.StringResource(StringResource.settings_footer_version, listOf("1.0")),
            appInfo.toVersionLabel(),
        )
    }
}
