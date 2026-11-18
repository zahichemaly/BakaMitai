package com.zc.bakamitai.feature.schedule.data.dto

import com.zc.bakamitai.common.data.dto.ScheduleItemDTO
import com.zc.bakamitai.common.data.network.appJson
import com.zc.bakamitai.common.util.SubsPleaseFixtures
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class WeeklyScheduleDTOTest {

    private fun decode(json: String): WeeklyScheduleDTO = appJson.decodeFromString(WeeklyScheduleDTO.serializer(), json)

    @Test
    fun `the real response decodes every weekday with its items`() {
        val dto = decode(SubsPleaseFixtures.read(SubsPleaseFixtures.SCHEDULE_WEEK))

        assertEquals(
            listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"),
            dto.days.keys.toList(),
        )
        assertEquals(listOf(1, 2, 3, 1, 2, 1, 1), dto.days.values.map { it.size })
        assertEquals(
            ScheduleItemDTO(
                title = "PSYREN",
                page = "psyren",
                imageUrl = "/wp-content/uploads/2026/10/160405.jpg",
                time = "09:30",
                aired = null,
            ),
            dto.days.getValue("Monday").single(),
        )
    }

    @Test
    fun `keys are kept whatever their order`() {
        val dto = decode(
            """
            {"schedule":{
              "Sunday":[{"title":"B","page":"b","time":"10:00"}],
              "Monday":[{"title":"A","page":"a","time":"09:00"}]
            }}
            """.trimIndent(),
        )

        assertEquals(setOf("Sunday", "Monday"), dto.days.keys)
        assertEquals("a", dto.days.getValue("Monday").single().page)
    }

    @Test
    fun `an empty array, null or missing schedule has no days`() {
        assertTrue(decode("""{"tz":"Etc/GMT","schedule":[]}""").days.isEmpty())
        assertTrue(decode("""{"tz":"Etc/GMT","schedule":null}""").days.isEmpty())
        assertTrue(decode("""{"tz":"Etc/GMT"}""").days.isEmpty())
    }

    @Test
    fun `a day value that isn't an array is an empty day`() {
        val dto = decode("""{"schedule":{"Monday":{},"Tuesday":"x","Wednesday":null}}""")

        assertEquals(mapOf("Monday" to emptyList(), "Tuesday" to emptyList(), "Wednesday" to emptyList()), dto.days)
    }

    @Test
    fun `unknown root and item fields are ignored and missing item fields are null`() {
        val dto = decode(
            """
            {"tz":"Asia/Tokyo","extra":1,"x":[],
             "schedule":{"Monday":[{"title":"A","page":"a","time":"09:00","rating":9}]}}
            """.trimIndent(),
        )

        assertEquals(
            ScheduleItemDTO(title = "A", page = "a", imageUrl = null, time = "09:00", aired = null),
            dto.days.getValue("Monday").single(),
        )
    }

    @Test
    fun `an array root fails`() {
        assertFailsWith<SerializationException> { decode("[]") }
    }
}
