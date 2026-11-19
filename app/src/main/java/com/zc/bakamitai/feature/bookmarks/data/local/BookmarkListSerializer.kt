package com.zc.bakamitai.feature.bookmarks.data.local

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.zc.bakamitai.feature.bookmarks.data.dto.BookmarkListDTO
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * Reads and writes `bookmarks.json` as UTF-8 JSON. A missing or blank file is no bookmarks; a file that doesn't
 * decode is a [CorruptionException], which the store's corruption handler replaces with no bookmarks.
 */
class BookmarkListSerializer(private val json: Json) : Serializer<BookmarkListDTO> {

    override val defaultValue: BookmarkListDTO = BookmarkListDTO(bookmarks = emptyList())

    override suspend fun readFrom(input: InputStream): BookmarkListDTO {
        val text = input.readBytes().decodeToString()
        if (text.isBlank()) return defaultValue
        return try {
            json.decodeFromString(BookmarkListDTO.serializer(), text)
        } catch (e: IllegalArgumentException) {
            // SerializationException is an IllegalArgumentException: malformed JSON or a missing required field.
            throw CorruptionException("bookmarks.json can't be decoded", e)
        }
    }

    override suspend fun writeTo(t: BookmarkListDTO, output: OutputStream) {
        output.write(json.encodeToString(BookmarkListDTO.serializer(), t).encodeToByteArray())
    }
}
