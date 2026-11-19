package com.zc.bakamitai.feature.bookmarks.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EpisodeKeyTest {

    @Test
    fun `a revision shares its episode's key`() {
        assertEquals(EpisodeKey("1216"), EpisodeKey.of("1216v2"))
        assertEquals(EpisodeKey("07"), EpisodeKey.of(" 07V3 "))
        assertEquals(EpisodeKey.of("1216"), EpisodeKey.of("1216v2"))
    }

    @Test
    fun `numbers are parsed, decimals included`() {
        assertEquals(7.0, EpisodeKey.of("07").number)
        assertEquals(12.5, EpisodeKey.of("12.5").number)
        assertEquals(1216.0, EpisodeKey.of("1216v2").number)
    }

    @Test
    fun `specials and movies have no number and keep their label`() {
        assertNull(EpisodeKey.of("Egghead SP11").number)
        assertEquals(EpisodeKey("Egghead SP11"), EpisodeKey.of("Egghead SP11"))
        assertNull(EpisodeKey.of("Movie").number)
        assertNull(EpisodeKey.of("1e5").number)
    }

    @Test
    fun `a v suffix after a letter isn't a revision`() {
        assertEquals(EpisodeKey("Rev"), EpisodeKey.of("Rev"))
        assertEquals(EpisodeKey("Recap Lv2"), EpisodeKey.of("Recap Lv2"))
    }

    @Test
    fun `batch ranges are detected`() {
        assertTrue("01-24".isBatchRange())
        assertTrue("01 - 28".isBatchRange())
        assertFalse("07".isBatchRange())
        assertFalse("Movie".isBatchRange())
        assertFalse("1216v2".isBatchRange())
    }
}
