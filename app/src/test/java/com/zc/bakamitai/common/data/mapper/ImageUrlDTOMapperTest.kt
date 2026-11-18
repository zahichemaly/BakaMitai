package com.zc.bakamitai.common.data.mapper

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ImageUrlDTOMapperTest {

    @Test
    fun `a relative path is prefixed with the SubsPlease base URL without a double slash`() {
        assertEquals(
            "https://subsplease.org/wp-content/uploads/2026/10/159979.jpg",
            "/wp-content/uploads/2026/10/159979.jpg".toCoverUrl(),
        )
    }

    @Test
    fun `a path without a leading slash gets one`() {
        assertEquals(
            "https://subsplease.org/wp-content/uploads/2026/10/159979.jpg",
            "wp-content/uploads/2026/10/159979.jpg".toCoverUrl(),
        )
    }

    @Test
    fun `the placeholder image maps to null`() {
        assertNull("/wp-includes/images/image-coming-soon-placeholder.png".toCoverUrl())
        assertNull("https://subsplease.org/wp-includes/images/image-coming-soon-placeholder.png".toCoverUrl())
    }

    @Test
    fun `null and blank map to null`() {
        assertNull(null.toCoverUrl())
        assertNull("".toCoverUrl())
        assertNull("   ".toCoverUrl())
    }

    @Test
    fun `an absolute URL is kept`() {
        assertEquals("https://cdn.example.org/cover.jpg", "https://cdn.example.org/cover.jpg".toCoverUrl())
    }
}
