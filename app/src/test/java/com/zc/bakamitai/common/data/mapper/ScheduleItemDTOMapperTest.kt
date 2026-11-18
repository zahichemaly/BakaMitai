package com.zc.bakamitai.common.data.mapper

import com.zc.bakamitai.common.data.dto.ScheduleItemDTO
import com.zc.bakamitai.common.data.dto.TodayScheduleDTO
import com.zc.bakamitai.common.data.network.appJson
import com.zc.bakamitai.common.domain.model.ScheduledShow
import com.zc.bakamitai.common.domain.model.Show
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.common.util.SubsPleaseFixtures
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScheduleItemDTOMapperTest {

    private val item = ScheduleItemDTO(
        title = "Sasaki to Pii-chan S2",
        page = "sasaki-to-pii-chan-s2",
        imageUrl = "/wp-content/uploads/2026/10/159979.jpg",
        time = "08:00",
        aired = false,
    )

    @Test
    fun `maps a complete item`() {
        assertEquals(
            ScheduledShow(
                show = Show(
                    slug = ShowSlug("sasaki-to-pii-chan-s2"),
                    title = "Sasaki to Pii-chan S2",
                    coverUrl = "https://subsplease.org/wp-content/uploads/2026/10/159979.jpg",
                ),
                time = LocalTime(8, 0),
                aired = false,
            ),
            item.toDomain(),
        )
    }

    @Test
    fun `aired is carried, including null`() {
        assertEquals(true, item.copy(aired = true).toDomain()?.aired)
        assertNull(item.copy(aired = null).toDomain()?.aired)
    }

    @Test
    fun `an unparseable or missing time drops the item`() {
        assertNull(item.copy(time = "8pm").toDomain())
        assertNull(item.copy(time = null).toDomain())
    }

    @Test
    fun `a missing page or title drops the item`() {
        assertNull(item.copy(page = null).toDomain())
        assertNull(item.copy(page = "").toDomain())
        assertNull(item.copy(title = null).toDomain())
    }

    @Test
    fun `the placeholder cover maps to null`() {
        val mapped = item.copy(imageUrl = "/wp-includes/images/image-coming-soon-placeholder.png").toDomain()

        assertNull(mapped?.show?.coverUrl)
    }

    @Test
    fun `a missing schedule is empty`() {
        assertTrue(TodayScheduleDTO(schedule = null).toDomain().isEmpty())
    }

    @Test
    fun `the real response maps every item in order`() {
        val dto = appJson.decodeFromString<TodayScheduleDTO>(SubsPleaseFixtures.read(SubsPleaseFixtures.SCHEDULE_TODAY))

        val schedule = dto.toDomain()

        assertEquals(
            listOf(LocalTime(8, 0), LocalTime(10, 30), LocalTime(12, 0), LocalTime(12, 15)),
            schedule.map { it.time },
        )
        assertEquals(listOf(true, true, false, false), schedule.map { it.aired })
    }
}
