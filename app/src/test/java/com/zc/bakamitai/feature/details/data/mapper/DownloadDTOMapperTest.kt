package com.zc.bakamitai.feature.details.data.mapper

import com.zc.bakamitai.feature.details.data.dto.DownloadDTO
import com.zc.bakamitai.feature.details.domain.model.Download
import com.zc.bakamitai.feature.details.domain.model.Resolution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DownloadDTOMapperTest {

    private val xdcc = "%22%5BSubsPlease%5D+Sousou+no+Frieren+-+28+%28480p%29%22"

    private val download = DownloadDTO(
        res = "480",
        magnet = "magnet:?xt=urn:btih:NIOW75UOB6GLVTNTELL24BQNOQZXP2UD",
        torrent = "https://nyaa.si/view/1793087/torrent",
        xdcc = xdcc,
    )

    @Test
    fun `the XDCC value is appended to the search page without encoding it again`() {
        assertEquals(
            "https://subsplease.org/xdcc/?search=%22%5BSubsPlease%5D+Sousou+no+Frieren+-+28+%28480p%29%22",
            download.toDomain()?.xdccUrl,
        )
    }

    @Test
    fun `maps every link`() {
        assertEquals(
            Download(
                resolution = Resolution.SD,
                height = 480,
                magnetUri = "magnet:?xt=urn:btih:NIOW75UOB6GLVTNTELL24BQNOQZXP2UD",
                torrentUrl = "https://nyaa.si/view/1793087/torrent",
                xdccUrl = "https://subsplease.org/xdcc/?search=$xdcc",
            ),
            download.toDomain(),
        )
    }

    @Test
    fun `heights map to their resolution`() {
        fun map(res: String?) = download.copy(res = res).toDomain()?.let { it.resolution to it.height }

        assertEquals(Resolution.SD to 360, map("360"))
        assertEquals(Resolution.SD to 480, map("480"))
        assertEquals(Resolution.SD to 540, map("540"))
        assertEquals(Resolution.HD to 720, map("720"))
        assertEquals(Resolution.FHD to 1080, map(" 1080 "))
    }

    @Test
    fun `an unknown, non-numeric or missing resolution is dropped`() {
        assertNull(download.copy(res = "2160").toDomain())
        assertNull(download.copy(res = "abc").toDomain())
        assertNull(download.copy(res = null).toDomain())
    }

    @Test
    fun `links that aren't a magnet URI or an http URL are left out`() {
        val mapped = download.copy(magnet = "https://nyaa.si/x", torrent = "ftp://nyaa.si/x").toDomain()

        assertNull(mapped?.magnetUri)
        assertNull(mapped?.torrentUrl)
        assertEquals("http://nyaa.si/x", download.copy(torrent = "http://nyaa.si/x").toDomain()?.torrentUrl)
    }

    @Test
    fun `blank links are left out, and a download without any link is dropped`() {
        assertNull(download.copy(xdcc = " ").toDomain()?.xdccUrl)
        assertNull(download.copy(magnet = "", torrent = null, xdcc = " ").toDomain())
    }
}
