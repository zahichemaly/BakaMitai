package com.zc.bakamitai.feature.shows.presentation.component.showrow.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ShowRowPositionTest {

    @Test
    fun `a lone row is SINGLE`() {
        assertEquals(ShowRowPosition.SINGLE, ShowRowPosition.of(0, 1))
    }

    @Test
    fun `rows of a longer section are FIRST, MIDDLE then LAST`() {
        assertEquals(
            listOf(ShowRowPosition.FIRST, ShowRowPosition.MIDDLE, ShowRowPosition.LAST),
            List(3) { ShowRowPosition.of(it, 3) },
        )
        assertEquals(listOf(ShowRowPosition.FIRST, ShowRowPosition.LAST), List(2) { ShowRowPosition.of(it, 2) })
    }

    @Test
    fun `first and last cover SINGLE`() {
        assertEquals(listOf(true, false, false, true), ShowRowPosition.entries.map { it.isFirst })
        assertEquals(listOf(false, false, true, true), ShowRowPosition.entries.map { it.isLast })
    }
}
