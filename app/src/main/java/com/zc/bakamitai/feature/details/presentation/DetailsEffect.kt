package com.zc.bakamitai.feature.details.presentation

import com.zc.bakamitai.common.ui.component.error.model.ErrorUIModel
import com.zc.bakamitai.common.ui.text.UiText

sealed interface DetailsEffect {

    data object NavigateBack : DetailsEffect

    /** A `magnet:` URI or a `.torrent` URL, for a torrent client. Without one: "No torrent app found". */
    data class OpenTorrentLink(val url: String) : DetailsEffect

    /** The XDCC search page or the show's page. Without an app for it: "No app can open this link". */
    data class OpenWebLink(val url: String) : DetailsEffect

    /** The share sheet, with [text] (the title and the page URL) and [title]. */
    data class Share(val title: UiText, val text: UiText) : DetailsEffect

    /** A refresh or a bookmark write failed while content is shown. */
    data class ShowError(val error: ErrorUIModel) : DetailsEffect
}
