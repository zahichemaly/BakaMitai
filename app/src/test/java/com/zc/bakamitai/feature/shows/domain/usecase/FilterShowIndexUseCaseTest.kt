package com.zc.bakamitai.feature.shows.domain.usecase

import com.zc.bakamitai.common.domain.model.ShowTestData.show
import com.zc.bakamitai.feature.shows.domain.model.ShowIndex
import com.zc.bakamitai.feature.shows.domain.model.ShowIndexSection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class FilterShowIndexUseCaseTest {

    private val filter = FilterShowIndexUseCase()

    private val index = ShowIndex(
        listOf(
            ShowIndexSection('#', listOf(show("86", "86 - Eighty Six"))),
            ShowIndexSection('C', listOf(show("cm", "Chainsaw Man"), show("cl", "Clannad"))),
            ShowIndexSection(
                'D',
                listOf(show("dc", "Detective Conan"), show("dcm", "Detective Conan Movies"), show("dn", "Death Note")),
            ),
        ),
    )

    private fun ShowIndex.titles() = sections.flatMap { section -> section.shows.map { it.title } }

    @Test
    fun `blank and whitespace queries keep the index`() {
        assertSame(index, filter(index, ""))
        assertSame(index, filter(index, "   "))
    }

    @Test
    fun `the query is trimmed`() {
        assertEquals(listOf("Detective Conan", "Detective Conan Movies"), filter(index, "  conan ").titles())
    }

    @Test
    fun `case is ignored`() {
        assertEquals(listOf("Detective Conan", "Detective Conan Movies"), filter(index, "CONAN").titles())
    }

    @Test
    fun `matches inside a word count`() {
        assertEquals(listOf("Chainsaw Man"), filter(index, "ainsaw").titles())
    }

    @Test
    fun `sections emptied by the filter are dropped, the order is kept`() {
        val filtered = filter(index, "n")

        assertEquals(listOf('C', 'D'), filtered.sections.map { it.letter })
        assertEquals(
            listOf("Chainsaw Man", "Clannad", "Detective Conan", "Detective Conan Movies", "Death Note"),
            filtered.titles(),
        )
        assertEquals(5, filtered.showCount)
    }

    @Test
    fun `no match leaves no section`() {
        assertTrue(filter(index, "zzz").sections.isEmpty())
    }
}
