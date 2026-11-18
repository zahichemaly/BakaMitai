package com.zc.bakamitai.feature.home.data.dto

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
 * The polymorphic root of `f=latest` and `f=search` (docs/api.md §3.3–3.4): an object keyed by
 * `"<show> - <episode>"` whose values are the releases, `[]` when a search has no results, or
 * `{"error":"limit_reached"}` past the last page.
 *
 * @property isLimitReached `true` for the `limit_reached` response, which has no releases.
 */
@Serializable(with = ReleaseListDTOSerializer::class)
data class ReleaseListDTO(val releases: List<ReleaseDTO>, val isLimitReached: Boolean)

/** Decode-only: the app never sends a release list. */
internal object ReleaseListDTOSerializer : KSerializer<ReleaseListDTO> {

    private const val ERROR_KEY = "error"

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("com.zc.bakamitai.feature.home.data.dto.ReleaseListDTO")

    override fun deserialize(decoder: Decoder): ReleaseListDTO {
        val input = decoder as? JsonDecoder ?: throw SerializationException("ReleaseListDTO is JSON-only")
        return when (val root = input.decodeJsonElement()) {
            is JsonArray -> ReleaseListDTO(releases = emptyList(), isLimitReached = false)

            is JsonObject -> if (ERROR_KEY in root) {
                ReleaseListDTO(releases = emptyList(), isLimitReached = true)
            } else {
                ReleaseListDTO(
                    releases = root.values.map { input.json.decodeFromJsonElement(ReleaseDTO.serializer(), it) },
                    isLimitReached = false,
                )
            }

            else -> throw SerializationException("Unexpected release list root: $root")
        }
    }

    override fun serialize(encoder: Encoder, value: ReleaseListDTO): Unit =
        throw SerializationException("ReleaseListDTO is decode-only")
}
