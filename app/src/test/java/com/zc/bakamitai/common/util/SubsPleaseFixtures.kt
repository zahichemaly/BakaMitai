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

    /**
     * `GET /shows/sousou-no-frieren/` (HTML): the head's `<title>`, the site header and menu, the `h1`, the synopsis
     * (`<h2>` and two `<p>`, with a raw "—"), the empty release table with `sid="620"`, and the sidebar cover.
     */
    const val SHOW_PAGE = "show_page.html"

    /** `f=show&sid=620` (Sousou no Frieren): batch "01-28"; episodes "28", "27", "01"; three resolutions each. */
    const val SHOW_EPISODES = "show_episodes.json"

    /**
     * `f=show&sid=48` (Detective Conan), in API order: revisions next to the original, a named special;
     * `batch: []`.
     */
    const val SHOW_EPISODES_SPECIALS = "show_episodes_specials.json"

    /** `f=show&sid=58` (A3! Season Autumn & Winter): a "01-12" batch with 540p; episodes "12" and "01" with 360p. */
    const val SHOW_EPISODES_SD = "show_episodes_sd.json"

    /** `f=show&sid=582` (Bakuten!! Movie): the single "Movie" entry; `batch: []`. */
    const val SHOW_EPISODES_MOVIE = "show_episodes_movie.json"

    /** `f=show&sid=999999`, an unknown sid: `{"batch":[],"episode":[]}`. */
    const val SHOW_EPISODES_EMPTY = "show_episodes_empty.json"

    fun read(name: String): String {
        val url = requireNotNull(javaClass.classLoader?.getResource("subsplease/$name")) {
            "Missing fixture subsplease/$name"
        }
        return url.readText()
    }
}
