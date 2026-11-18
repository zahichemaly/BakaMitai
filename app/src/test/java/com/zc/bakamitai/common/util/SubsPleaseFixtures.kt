package com.zc.bakamitai.common.util

/** Trimmed real SubsPlease responses, in `src/test/resources/subsplease/`. */
object SubsPleaseFixtures {

    const val SCHEDULE_TODAY = "schedule_today.json"
    const val LATEST_PAGE_1 = "latest_page1.json"
    const val LATEST_LIMIT_REACHED = "latest_limit_reached.json"
    const val SEARCH_NEWEST = "search_newest.json"
    const val SEARCH_SHOW = "search_show.json"
    const val SEARCH_NO_RESULTS = "search_no_results.json"

    fun read(name: String): String {
        val url = requireNotNull(javaClass.classLoader?.getResource("subsplease/$name")) {
            "Missing fixture subsplease/$name"
        }
        return url.readText()
    }
}
