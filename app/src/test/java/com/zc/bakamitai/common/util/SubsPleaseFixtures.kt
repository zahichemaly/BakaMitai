package com.zc.bakamitai.common.util

/** Trimmed real SubsPlease responses, in `src/test/resources/subsplease/`. */
object SubsPleaseFixtures {

    const val SCHEDULE_TODAY = "schedule_today.json"
    const val SCHEDULE_WEEK = "schedule_week.json"
    const val LATEST_PAGE_1 = "latest_page1.json"
    const val LATEST_LIMIT_REACHED = "latest_limit_reached.json"
    const val SEARCH_NEWEST = "search_newest.json"
    const val SEARCH_SHOW = "search_show.json"
    const val SEARCH_NO_RESULTS = "search_no_results.json"

    /** `GET /shows/` (HTML): the site menu's `/shows/` link and 10 entries under 6 mismatched `<h3>…</h4>`. */
    const val SHOWS_PAGE = "shows_page.html"

    fun read(name: String): String {
        val url = requireNotNull(javaClass.classLoader?.getResource("subsplease/$name")) {
            "Missing fixture subsplease/$name"
        }
        return url.readText()
    }
}
