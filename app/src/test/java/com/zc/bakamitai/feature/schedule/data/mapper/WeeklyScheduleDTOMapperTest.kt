package com.zc.bakamitai.feature.schedule.data.mapper

import com.zc.bakamitai.common.data.dto.ScheduleItemDTO
import com.zc.bakamitai.common.data.network.appJson
import com.zc.bakamitai.common.domain.model.ScheduledShow
import com.zc.bakamitai.common.domain.model.Show
import com.zc.bakamitai.common.domain.model.ShowSlug
import com.zc.bakamitai.common.util.SubsPleaseFixtures
import com.zc.bakamitai.feature.schedule.data.dto.WeeklyScheduleDTO
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WeeklyScheduleDTOMapperTest {

    private fun item(slug: String, time: String? = "09:00", title: String? = slug, imageUrl: String? = null) =
        ScheduleItemDTO(title = title, page = slug, imageUrl = imageUrl, time = time, aired = null)

    @Test
    fun `every English weekday key maps to its day, ignoring case`() {
        val names = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val dto = WeeklyScheduleDTO(names.associateWith { listOf(item(it.lowercase())) })

        val days = dto.toDomain()

        assertEquals(DayOfWeek.entries.toSet(), days.keys)
        assertEquals("friday", days.getValue(DayOfWeek.FRIDAY).single().show.slug.value)
        assertEquals(
            setOf(DayOfWeek.MONDAY),
            WeeklyScheduleDTO(mapOf("monday" to listOf(item("a")))).toDomain().keys,
        )
        assertEquals(
            setOf(DayOfWeek.MONDAY),
            WeeklyScheduleDTO(mapOf(" MONDAY " to listOf(item("a")))).toDomain().keys,
        )
    }

    @Test
    fun `unknown keys are dropped and missing days are absent`() {
        val days = WeeklyScheduleDTO(mapOf("Funday" to listOf(item("a")), "Monday" to listOf(item("b")))).toDomain()

        assertEquals(setOf(DayOfWeek.MONDAY), days.keys)
    }

    @Test
    fun `items without a page, a title or a valid time are dropped`() {
        val days = WeeklyScheduleDTO(
            mapOf(
                "Monday" to listOf(
                    item("ok"),
                    ScheduleItemDTO(title = "No page", page = null, imageUrl = null, time = "09:00", aired = null),
                    item("no-title", title = null),
                    item("bad-time", time = "9pm"),
                    item("no-time", time = null),
                ),
            ),
        ).toDomain()

        assertEquals(listOf("ok"), days.getValue(DayOfWeek.MONDAY).map { it.show.slug.value })
    }

    @Test
    fun `covers resolve to absolute URLs, the placeholder to null, and aired is null`() {
        val days = WeeklyScheduleDTO(
            mapOf(
                "Tuesday" to listOf(
                    item("cover", imageUrl = "/wp-content/uploads/2026/10/160339.jpg"),
                    item("placeholder", imageUrl = "/wp-includes/images/image-coming-soon-placeholder.png"),
                ),
            ),
        ).toDomain().getValue(DayOfWeek.TUESDAY)

        assertEquals("https://subsplease.org/wp-content/uploads/2026/10/160339.jpg", days[0].show.coverUrl)
        assertNull(days[1].show.coverUrl)
        assertTrue(days.all { it.aired == null })
    }

    @Test
    fun `the real response maps with its order kept`() {
        val dto = appJson.decodeFromString(
            WeeklyScheduleDTO.serializer(),
            SubsPleaseFixtures.read(SubsPleaseFixtures.SCHEDULE_WEEK),
        )

        val days = dto.toDomain()

        assertEquals(DayOfWeek.entries.toSet(), days.keys)
        assertEquals(
            listOf(
                ScheduledShow(
                    show = Show(ShowSlug("kyouran-reijou-nia-liston"), "Kyouran Reijou Nia Liston", coverUrl = null),
                    time = LocalTime(7, 55),
                    aired = null,
                ),
                ScheduledShow(
                    show = Show(
                        slug = ShowSlug("chii-fuyo"),
                        title = "Chii Fuyo",
                        coverUrl = "https://subsplease.org/wp-content/uploads/2026/10/160339.jpg",
                    ),
                    time = LocalTime(10, 30),
                    aired = null,
                ),
            ),
            days.getValue(DayOfWeek.TUESDAY),
        )
        assertEquals(
            listOf(LocalTime(8, 0), LocalTime(8, 0), LocalTime(10, 30)),
            days.getValue(DayOfWeek.WEDNESDAY).map { it.time },
        )
    }
}
