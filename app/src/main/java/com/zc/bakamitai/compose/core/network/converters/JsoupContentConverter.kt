package com.zc.bakamitai.compose.core.network.converters

import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.serialization.Configuration
import io.ktor.serialization.ContentConverter
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser

private class JsoupContentConverter(
    private val baseUri: String,
    private val registeredContentType: ContentType // important: set by registration
) : ContentConverter {

    override suspend fun serialize(
        contentType: ContentType,
        charset: Charset,
        typeInfo: TypeInfo,
        value: Any?
    ): OutgoingContent? {
        // Usually not needed for scraping. Implement if you ever POST/PUT Document.
        return null
    }

    override suspend fun deserialize(
        charset: Charset,
        typeInfo: TypeInfo,
        content: ByteReadChannel
    ): Any? {
        // Only handle Jsoup Document
        if (typeInfo.type != Document::class) return null

        val bytes = content.readRemaining().readByteArray()

        val parser = when (registeredContentType.withoutParameters()) {
            ContentType.Application.Xml,
            ContentType.Text.Html,
            ContentType.Text.Xml -> Parser.xmlParser()

            else -> Parser.htmlParser()
        }

        return Jsoup.parse(bytes.inputStream(), charset.name(), baseUri, parser)
    }
}


fun Configuration.registerJsoupHtml(baseUri: String) {
    register(
        ContentType.Text.Html,
        JsoupContentConverter(baseUri, ContentType.Text.Html)
    )
}
