package com.zc.bakamitai.feature.schedule.data.dto

import com.zc.bakamitai.common.data.dto.ScheduleItemDTO
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject

/**
 * `f=schedule` without `h` (docs/api.md §3.2): `schedule` is an object keyed by English weekday name
 * (`"Monday"` … `"Sunday"`) whose values are that day's shows. Other root fields (`tz`) are ignored.
 *
 * Decoded defensively: an empty PHP array arrives as `[]`, so a `schedule` that is `[]`, `null` or missing is
 * "no shows", and a day value that isn't an array is an empty day.
 *
 * @property days the shows per weekday key, as sent; the keys aren't validated here.
 */
@Serializable(with = WeeklyScheduleDTOSerializer::class)
data class WeeklyScheduleDTO(val days: Map<String, List<ScheduleItemDTO>>)

/** Decode-only: the app never sends a schedule. */
internal object WeeklyScheduleDTOSerializer : KSerializer<WeeklyScheduleDTO> {

    private const val SCHEDULE_KEY = "schedule"

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("com.zc.bakamitai.feature.schedule.data.dto.WeeklyScheduleDTO")

    override fun deserialize(decoder: Decoder): WeeklyScheduleDTO {
        val input = decoder as? JsonDecoder ?: throw SerializationException("WeeklyScheduleDTO is JSON-only")
        val root = input.decodeJsonElement() as? JsonObject
            ?: throw SerializationException("Unexpected weekly schedule root")
        val days = (root[SCHEDULE_KEY] as? JsonObject).orEmpty().mapValues { (_, items) ->
            (items as? JsonArray).orEmpty()
                .filterIsInstance<JsonObject>()
                .map { input.json.decodeFromJsonElement(ScheduleItemDTO.serializer(), it) }
        }
        return WeeklyScheduleDTO(days)
    }

    override fun serialize(encoder: Encoder, value: WeeklyScheduleDTO): Unit =
        throw SerializationException("WeeklyScheduleDTO is decode-only")
}
