package com.zc.bakamitai.feature.shows.presentation.mapper

import com.zc.bakamitai.common.domain.model.ShowTestData.show
import com.zc.bakamitai.common.ui.resource.PluralResource
import com.zc.bakamitai.common.ui.text.UiText
import com.zc.bakamitai.feature.shows.domain.model.ShowIndex
import com.zc.bakamitai.feature.shows.domain.model.ShowIndexSection
import kotlin.test.Test
import kotlin.test.assertEquals

class ShowIndexUIModelMapperTest {

    private val index = ShowIndex(
        listOf(
            ShowIndexSection('#', listOf(show("86", "86 - Eighty Six"))),
            ShowIndexSection('A', listOf(show("a1", "Akame ga Kill!"), show("a2", "Angel Beats!"))),
        ),
    )

    @Test
    fun `each section maps to a model with its header key, letter and rows`() {
        val sections = index.toSectionUIModels()

        assertEquals(listOf("header-#", "header-A"), sections.map { it.headerKey })
        assertEquals(listOf('#', 'A'), sections.map { it.letter.letter })
        assertEquals(
            listOf(UiText.StringValue("Akame ga Kill!"), UiText.StringValue("Angel Beats!")),
            sections[1].rows.map { it.title },
        )
    }

    @Test
    fun `each section counts its rows`() {
        val sections = index.toSectionUIModels()

        assertEquals(
            listOf(
                UiText.StringPlural(PluralResource.all_shows_section_count, 1, listOf(1)),
                UiText.StringPlural(PluralResource.all_shows_section_count, 2, listOf(2)),
            ),
            sections.map { it.countLabel },
        )
    }

    @Test
    fun `the footer counts every show`() {
        assertEquals(UiText.StringPlural(PluralResource.all_shows_footer_count, 3, listOf(3)), index.toFooterLabel())
    }

    @Test
    fun `an empty index has no section and a zero footer`() {
        val empty = ShowIndex(emptyList())

        assertEquals(emptyList(), empty.toSectionUIModels())
        assertEquals(UiText.StringPlural(PluralResource.all_shows_footer_count, 0, listOf(0)), empty.toFooterLabel())
    }
}
