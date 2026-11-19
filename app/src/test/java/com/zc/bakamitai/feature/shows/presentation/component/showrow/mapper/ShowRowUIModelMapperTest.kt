package com.zc.bakamitai.feature.shows.presentation.component.showrow.mapper

import com.zc.bakamitai.common.domain.model.ShowTestData.show
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.shows.presentation.component.showrow.model.ShowRowUIModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ShowRowUIModelMapperTest {

    @Test
    fun `a show maps to a row keyed by slug and title`() {
        assertEquals(
            ShowRowUIModel(
                key = "fruits-basket-2019|Fruits Basket (2019)",
                slug = "fruits-basket-2019",
                title = UiText.StringValue("Fruits Basket (2019)"),
            ),
            show("fruits-basket-2019", "Fruits Basket (2019)").toShowRowUIModel(),
        )
    }

    @Test
    fun `two titles of one slug get distinct keys`() {
        val first = show("fruits-basket-2019", "Fruits Basket (2019)").toShowRowUIModel()
        val second = show("fruits-basket-2019", "Fruits Basket (2019) S2").toShowRowUIModel()

        assertEquals(first.slug, second.slug)
        assertNotEquals(first.key, second.key)
    }
}
