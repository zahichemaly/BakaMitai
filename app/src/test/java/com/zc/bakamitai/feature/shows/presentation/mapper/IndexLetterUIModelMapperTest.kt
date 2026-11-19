package com.zc.bakamitai.feature.shows.presentation.mapper

import com.zc.bakamitai.common.domain.model.ShowTestData.show
import com.zc.bakamitai.common.ui.resource.StringResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.shows.domain.model.ShowIndexSection
import com.zc.bakamitai.feature.shows.presentation.model.IndexLetterUIModel
import kotlin.test.Test
import kotlin.test.assertEquals

class IndexLetterUIModelMapperTest {

    private fun letterOf(letter: Char) = ShowIndexSection(letter, listOf(show("x"))).toIndexLetterUIModel()

    @Test
    fun `letters alternate their accent, starting with A on primary`() {
        assertEquals(true, letterOf('A').isPrimaryAccent)
        assertEquals(false, letterOf('B').isPrimaryAccent)
        assertEquals(true, letterOf('C').isPrimaryAccent)
        assertEquals(false, letterOf('Z').isPrimaryAccent)
    }

    @Test
    fun `a letter is shown and read as itself`() {
        assertEquals(
            IndexLetterUIModel('M', UiText.StringValue("M"), UiText.StringValue("M"), isPrimaryAccent = true),
            letterOf('M'),
        )
    }

    @Test
    fun `the other section shows # and reads Numbers and symbols, on secondary`() {
        assertEquals(
            IndexLetterUIModel(
                letter = '#',
                label = UiText.StringValue("#"),
                description = UiText.StringResource(StringResource.cd_all_shows_other_section),
                isPrimaryAccent = false,
            ),
            letterOf(ShowIndexSection.OTHER),
        )
    }
}
