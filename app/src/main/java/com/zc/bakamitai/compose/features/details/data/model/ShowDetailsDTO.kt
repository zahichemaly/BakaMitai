package com.zc.bakamitai.compose.features.details.data.model

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("show", "", "")
data class ShowDetailsXml(
    @XmlSerialName("title", "", "")
    val title: String? = null,

    @XmlSerialName("synopsis", "", "")
    val synopsis: String? = null,

    @XmlSerialName("image", "", "")
    val image: ImageXml? = null,

    @XmlSerialName("table", "", "")
    val table: TableXml? = null,
)

@Serializable
data class ImageXml(
    @XmlSerialName("src", "", "")
    val src: String? = null
)

@Serializable
data class TableXml(
    @XmlSerialName("sid", "", "")
    val sid: String? = null
)
