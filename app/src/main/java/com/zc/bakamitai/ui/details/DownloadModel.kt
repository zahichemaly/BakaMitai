package com.zc.bakamitai.ui.details

import com.zc.bakamitai.data.models.DownloadsItem
import com.zc.bakamitai.data.models.dtos.ShowDownloadDto

data class DownloadModel(val downloads: List<ShowDownloadDto>) {
    data class Summary(val title: String, val downloads: List<Details>) {
        data class Details(val download: DownloadsItem)
    }
}
