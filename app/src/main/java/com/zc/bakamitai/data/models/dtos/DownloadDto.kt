package com.zc.bakamitai.data.models.dtos

import android.net.Uri
import com.zc.bakamitai.data.Constants

data class DownloadDto(
    val res: Int = 720,
    val magnet: String,
    val torrent: String,
    val xdcc: String
) {

    fun getFormattedResolution(): String {
        return if (res > 0) return res.toString()
        else Constants.Common.NOT_AVAILABLE
    }

    fun getXdccLink(): String {
        val uri = Uri.parse(Constants.Api.BASE_URL)
            .buildUpon()
            .appendPath("xdcc")
        var url = uri.toString()
        url += "/?search=$xdcc"
        return url
    }
}
