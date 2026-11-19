package com.zc.bakamitai.feature.details.data.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
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
 * The `f=show` response (docs/api.md §3.5): a show's episodes and batches. Each is an object keyed by
 * `"<show> - <episode>"` when it has entries and `[]` when it doesn't; [KeyedEpisodesSerializer] reads both.
 */
@Serializable
data class ShowEpisodesDTO(
    @SerialName("episode")
    @Serializable(with = KeyedEpisodesSerializer::class)
    val episodes: List<EpisodeDTO>?,
    @SerialName("batch")
    @Serializable(with = KeyedEpisodesSerializer::class)
    val batches: List<EpisodeDTO>?,
)

/**
 * Decode-only: an object's values, or an array's elements (PHP writes an empty map as `[]`), in response order.
 * Anything else isn't a list of entries.
 */
internal object KeyedEpisodesSerializer : KSerializer<List<EpisodeDTO>> {

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("com.zc.bakamitai.feature.details.data.dto.KeyedEpisodes")

    override fun deserialize(decoder: Decoder): List<EpisodeDTO> {
        val input = decoder as? JsonDecoder ?: throw SerializationException("ShowEpisodesDTO is JSON-only")
        val entries = when (val element = input.decodeJsonElement()) {
            is JsonObject -> element.values
            is JsonArray -> element
            else -> throw SerializationException("Unexpected episode list: $element")
        }
        return entries.map { input.json.decodeFromJsonElement(EpisodeDTO.serializer(), it) }
    }

    override fun serialize(encoder: Encoder, value: List<EpisodeDTO>): Unit =
        throw SerializationException("ShowEpisodesDTO is decode-only")
}
