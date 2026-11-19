package com.zc.bakamitai.feature.details.presentation.mapper

import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.details.domain.DetailsTestData.overview
import com.zc.bakamitai.feature.details.presentation.model.DetailsHeaderUIModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DetailsHeaderUIModelMapperTest {

    @Test
    fun `maps the title, cover and synopsis`() {
        assertEquals(
            DetailsHeaderUIModel(
                title = UiText.StringValue("BanG Dream! It’s MyGO!!!!!"),
                coverUrl = "https://subsplease.org/a.jpg",
                synopsis = UiText.StringValue("One.\n\nTwo."),
            ),
            overview(
                title = "BanG Dream! It’s MyGO!!!!!",
                coverUrl = "https://subsplease.org/a.jpg",
                synopsis = "One.\n\nTwo.",
            ).toHeaderUIModel(),
        )
    }

    @Test
    fun `no synopsis or cover stays null`() {
        val header = overview(coverUrl = null, synopsis = null).toHeaderUIModel()

        assertNull(header.synopsis)
        assertNull(header.coverUrl)
    }
}
