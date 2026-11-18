# SubsPlease API Reference

BakaMitai gets all of its anime data from `https://subsplease.org`. The site has **no official public API**, so this file documents the undocumented JSON endpoints its own website uses, the HTML pages we scrape where no JSON exists, and the RSS and XDCC sources.

What the app does with this data (which screen uses which call, and how) is in [context.md](context.md#4-data-mapping-per-screen).

> Everything here was verified against the live site on **2026-11-18**. The API is undocumented and can change without notice. Keep parsing tolerant (unknown keys ignored, every field nullable or defaulted) and keep all SubsPlease-specific knowledge inside the `data` layer.

---

## 1. Basics

- **Base URL:** `https://subsplease.org`
- **JSON API:** `GET https://subsplease.org/api/?f=<function>&tz=<IANA zone>&…`
- **`tz` is required on every API call.** Pass the device's IANA zone (`TimeZone.currentSystemDefault().id`, e.g. `Asia/Beirut`). It decides which day counts as "today", which weekday each show falls on, and the `time` values returned. Without it, `f=show` and `f=latest` return `200` with an **empty body**.
- **Content-Type is wrong.** API responses are served as `text/html; charset=UTF-8`, not `application/json`. Register Ktor's JSON content negotiation for `ContentType.Any` (or read the body as text and decode with `Json`). Otherwise deserialization fails.
- **No auth, no API key.** A plain app User-Agent (e.g. `BakaMitai/<version> (Android)`) is accepted. No Cloudflare challenge was observed for the API, the HTML pages or images.
- **No caching headers** (`cf-cache-status: DYNAMIC`). Cache in-app (see [§7](#7-client-guidelines)).
- **Images:** `image_url` is a site-relative path (e.g. `/wp-content/uploads/2026/07/158138.jpg`). Prefix it with `https://subsplease.org`. The path `/wp-includes/images/image-coming-soon-placeholder.png` means "no cover yet": map it to `null` and show our own placeholder.
- **Show page URL:** `https://subsplease.org/shows/<slug>/`. The **slug** (called `page` in JSON) is the stable **show ID** used across the whole app.

---

## 2. Endpoints at a glance

| Purpose | Request | Format | Used by |
|---|---|---|---|
| Today's schedule | `/api/?f=schedule&h=true&tz=<tz>` | JSON | Home |
| Weekly schedule | `/api/?f=schedule&tz=<tz>` | JSON | Schedule |
| Latest releases | `/api/?f=latest&tz=<tz>&p=<1\|2>` | JSON | Home |
| Search releases | `/api/?f=search&tz=<tz>&s=<query>` | JSON | Home (newest releases with an empty `s`, episode-number fallback) |
| Show episodes | `/api/?f=show&tz=<tz>&sid=<sid>` | JSON | Details |
| All shows list | `/shows/` | HTML | All Shows |
| Show page | `/shows/<slug>/` | HTML | Details (title, synopsis, cover, `sid`) |
| XDCC search page | `/xdcc/?search=<xdcc>` | HTML (web) | Details → XDCC button |
| RSS feeds | `/rss/?r=<1080\|720\|sd>[&t]` | RSS XML | Not used (see [§6](#6-rss-feeds-reference-only)) |

---

## 3. JSON endpoints

### 3.1 Today's schedule

`GET /api/?f=schedule&h=true&tz=<tz>`

```json
{
  "tz": "America/New_York",
  "schedule": [
    {
      "title": "Sasaki to Pii-chan S2",
      "page": "sasaki-to-pii-chan-s2",
      "image_url": "/wp-content/uploads/2026/10/159979.jpg",
      "time": "08:00",
      "aired": false
    }
  ]
}
```

- `schedule` is an **array** sorted by `time`.
- `time` is `"HH:mm"` (24-hour) **in the requested `tz`**, with no date.
- `aired` flips to `true` around release time, but **it's unverified whether it follows the actual release or the clock.** One show scheduled for 10:30 New York was `false` at 15:26Z and `true` at 15:33Z, when its release was already listed. If it follows the clock, a delayed show reads `aired: true` before its release exists. Home therefore also drops a show from Upcoming once today's release for it is known.
- `page` **may be missing.** The site's own script guards with `if (val.page)`. Drop items without it, since the slug is the app's show ID.
- **There's no episode number here.** [context.md §4.1](context.md#41-home) explains how to get one.

### 3.2 Weekly schedule

`GET /api/?f=schedule&tz=<tz>`

```json
{
  "tz": "America/New_York",
  "schedule": {
    "Monday":  [ { "title": "PSYREN", "page": "psyren", "image_url": "/wp-content/uploads/2026/10/160405.jpg", "time": "09:30" } ],
    "Tuesday": [ … ],
    …
    "Sunday":  [ … ]
  }
}
```

- `schedule` is an **object keyed by English weekday name** (`"Monday"` … `"Sunday"`). Don't depend on key order. Map each name to `kotlinx.datetime.DayOfWeek`.
- Items have **no `aired` field**.
- Weekday buckets are computed in the requested `tz`, so a show can move to a different day depending on the user's zone.

### 3.3 Latest releases

`GET /api/?f=latest&tz=<tz>&p=<page>`

```json
{
  "Sora wa Akai Kawa no Hotori - 20": {
    "time": "New",
    "release_date": "Tue, 17 Nov 2026 12:38:52 -0500",
    "show": "Sora wa Akai Kawa no Hotori",
    "episode": "20",
    "downloads": [
      { "res": "480",  "magnet": "magnet:?xt=urn:btih:…" },
      { "res": "720",  "magnet": "magnet:?xt=urn:btih:…" },
      { "res": "1080", "magnet": "magnet:?xt=urn:btih:…" }
    ],
    "xdcc": "%22%5BSubsPlease%5D+Sora+wa+Akai+Kawa+no+Hotori+-+20%22",
    "image_url": "/wp-content/uploads/2026/07/158138.jpg",
    "page": "sora-wa-akai-kawa-no-hotori"
  },
  "…": { … }
}
```

- The response is an **object keyed by `"<show> - <episode>"`**. Decode it as `Map<String, ReleaseDTO>` and use the **values**. Order is newest first, but sort by `release_date` yourself.
- **20 items per page.** Only `p=1` and `p=2` work. From `p=3` on, the response is `{"error":"limit_reached"}`, which has a different shape. Handle it.
- **The feed can lag by days.** On 2026-11-18 at 15:21 UTC, page 1 started on Sun 15 Nov while releases up to Wed 18 Nov existed; it had caught up by about 15:40 UTC, and lagged again at 20:28 UTC. The website's homepage makes the same call, so it's stale too. [Search with an empty `s`](#34-search-releases) stayed current, so Home merges both and removes duplicates.
- `release_date` is RFC 1123 (`EEE, dd MMM yyyy HH:mm:ss Z`). **Always parse it to an `Instant`.** Its offset **varies per item**, even for one show: some items use the requested `tz` (`-0500`), others the server's zone (`+0100`, `+0200` and `-0300` were all seen on one day). Never trust the offset; convert the `Instant` to the device zone yourself.
- **`release_date` is currently 1 hour early.** See [§3.5](#35-show-episodes); it applies to this endpoint too.
- `time` is only a display label: `"New"` (released in the last ~24 h) or `"Nov 17th"`. **Never use it in logic.**
- `page` may be missing, as in [§3.1](#31-todays-schedule). Drop those items.
- Titles (`show`) come through plain: no HTML entities or `\u` escapes were seen in schedule, latest or search responses, so no entity decoding is needed for JSON.
- `episode` is a **string**: usually `"07"`, but it can be a batch range (`"01-24"`) or `"Movie"`. Don't assume it's numeric.
- `downloads` here has **only `magnet`** (no `torrent`, no per-resolution `xdcc`).
- `xdcc` is already URL-encoded. Append it as-is to `https://subsplease.org/xdcc/?search=`. **Don't encode it again.**

### 3.4 Search releases

`GET /api/?f=search&tz=<tz>&s=<query>`

- Same shape as [latest releases](#33-latest-releases): an object keyed by `"<show> - <episode>"`, with `page` and `image_url`, and magnet-only `downloads`.
- **An empty query (`s=`) returns the 20 newest releases**, in the same shape and without the lag `f=latest` sometimes has. `p` is ignored. The website calls it when its search box is cleared.
- **No results returns `[]`**, an empty **array** rather than an object. Decode defensively: check whether the root is a `JsonArray` or a `JsonObject`.
- `time` labels are `"New"` / `"Nov 17th"` for an empty query and `"New"` / `"MM/dd/yy"` otherwise. Display only, never parse. `release_date` has the same offset and 1-hour issues as [latest releases](#33-latest-releases).
- Results appear capped (30 were returned for `s=sora`) and can include several shows whose titles match. Filter by `page` when you need one show.
- This searches **releases**, not the show catalogue. Use the [all shows list](#41-all-shows-list) for show lookups.
- URL-encode `s`.

### 3.5 Show episodes

`GET /api/?f=show&tz=<tz>&sid=<sid>`

```json
{
  "episode": {
    "Sora wa Akai Kawa no Hotori - 01": {
      "time": "07/07/26",
      "release_date": "Tue, 07 Jul 2026 19:41:51 +0200",
      "show": "Sora wa Akai Kawa no Hotori",
      "episode": "01",
      "downloads": [
        {
          "res": "1080",
          "torrent": "https://nyaa.si/view/2129523/torrent",
          "magnet": "magnet:?xt=urn:btih:…",
          "xdcc": "%22%5BSubsPlease%5D+Sora+wa+Akai+Kawa+no+Hotori+-+01+%281080p%29%22"
        }
      ]
    }
  },
  "batch": []
}
```

- `sid` is a numeric show ID that **only appears in the show page HTML** (see [§4.2](#42-show-page)). It's not the slug.
- `episode` and `batch` are **objects keyed by `"<show> - <episode>"` when they have entries, and `[]` (an empty array) when they don't**. Use a custom serializer or decode them as `JsonElement`.
- An unknown `sid` returns `{"batch":[],"episode":[]}`.
- `downloads` entries here include **`torrent`**, **`magnet`** and a **per-resolution `xdcc`** search string (already URL-encoded).
- `time` is `"MM/dd/yy"` or `"New"`. Display only.
- `release_date` here **ignores `tz`**. It came back in the server's zone (`+0200` in July, `+0100` in November) even though `tz=America/New_York` was requested. Parse to `Instant` and convert to the device zone yourself.
- **The instant is 1 hour early since European DST ended (25 Oct 2026).** Checked against nyaa.si upload times for *Tensei Shitara Ken Deshita S2*: episodes 07 (11 Nov) and 08 (18 Nov) are an hour early, episode 04 (21 Oct) is correct. The schedule `time` matches the real release. The app shows `release_date` as-is, because it can't tell when the server gets fixed; so in winter, release times look an hour early, and a release in the first local hour after midnight counts as the previous day.
- This response has **no `image_url` and no `page`**. Those come from the [show page](#42-show-page).

### 3.6 Resolutions

- `res` is a string: `"480"`, `"720"` or `"1080"` were observed. The site's own JS also treats `"360"` and `"540"` as **SD**.
- Map to a domain enum: `SD` (360/480/540, displayed with its actual value, e.g. "480p"), `HD` (720p), `FHD` (1080p). Ignore unknown values rather than crashing.

---

## 4. HTML pages (scraped)

The site is WordPress. These pages are server-rendered, but the markup isn't strictly valid, so parse leniently.

### 4.1 All shows list

`GET /shows/`, about 1,260 entries.

```html
<div class="all-shows">
  <h3 class="alphanum-category" id="A">A</h4>          <!-- note: mismatched closing tag on the live site -->
  <div class="all-shows-link"><a href="/shows/86-eighty-six" title="86 - Eighty Six">86 - Eighty Six</a></div>
  …
</div>
```

- Every `div.all-shows-link > a` is one show:
  - **slug** = `href` without `/shows/` and without a trailing `/`
  - **title** = the `title` attribute (or the link text), **HTML-entity decoded** (`&amp;`, `&#8217;`, …)
- The page has its own headers (`1`, `2`, …, `A` … `Z`). **Ignore them.** The app computes its own sections (see [context.md §3.3](context.md#33-all-shows)).
- The page is about 200 KB. Fetch it once and cache it (see [§7](#7-client-guidelines)).

### 4.2 Show page

`GET /shows/<slug>/`

| Data | Where in the HTML |
|---|---|
| Title | `h1.entry-title` (text, entity-decoded) |
| Synopsis | `div.series-syn` → its `<p>` elements (ignore the `<h2>Synopsis</h2>`). Join paragraphs with blank lines, strip tags, decode entities. It can be missing or empty. |
| Cover | `#secondary img.img-responsive` → `src` (site-relative, same rules as `image_url`) |
| `sid` | `table#show-release-table` → the `sid` attribute (e.g. `sid="1199"`) |

The episodes table on this page is **empty in the HTML**. The site fills it with JS from [`f=show&sid=…`](#35-show-episodes), which is what we do too.

---

## 5. XDCC

- **Button behavior:** open `https://subsplease.org/xdcc/?search=<xdcc>` in the browser, where `<xdcc>` is the already-encoded `xdcc` value from the API. This matches what the site does.
- Reference only (not needed for v1): the page gets its data from `GET https://subsplease.org/xdcc/search.php?t=<url-encoded query>`. That endpoint **returns an empty body unless the request has `Referer: https://subsplease.org/xdcc/`**. It replies with JavaScript lines rather than JSON:
  ```text
  p.k[1] = {b:"Ginpachi-Sensei", n:4450, s:678, f:"[SubsPlease] Sora wa Akai Kawa no Hotori - 20 (720p) [B97C27CB].mkv"};
  ```
  Here `b` = bot nick, `n` = pack number, `s` = size in MiB and `f` = file name. The IRC command is `/msg <b> xdcc send #<n>` on `irc://irc.rizon.net/subsplease`. A later version could offer a "Copy IRC command" option built from this.

---

## 6. RSS feeds (reference only)

`https://subsplease.org/rss/` (all resolutions), `?r=1080`, `?r=720`, `?r=sd`. Add `&t` (e.g. `?t&r=1080`) to get `.torrent` links instead of magnets. Each feed has the latest **50** files.

```xml
<item>
  <title>[SubsPlease] Sora wa Akai Kawa no Hotori - 20 (1080p) [064C1798].mkv</title>
  <link>magnet:?xt=urn:btih:…</link>            <!-- or https://nyaa.si/view/<id>/torrent with &t -->
  <guid isPermaLink="false">KTHKFGVV2T3C6UGLCH22EPT6BW6ZOMAD</guid>
  <pubDate>Tue, 17 Nov 2026 10:38:52 +0000</pubDate>
  <category>Sora wa Akai Kawa no Hotori - 1080</category>
  <subsplease:size>1.3 GiB</subsplease:size>
</item>
```

**Don't use RSS as a primary source:**

- It has no slug and no cover, so items can't be tied to a show without fuzzy title matching.
- One item per file, not per episode.
- **`pubDate` is wrong**: it's labelled `+0000` but was 7 hours off the JSON API's `release_date` for the same file.

The JSON API covers every screen. RSS is listed only so nobody rediscovers it as an option.

---

## 7. Client guidelines

- **One shared Ktor `HttpClient`** for SubsPlease (`common/data/network/`):
  - default base URL `https://subsplease.org`
  - a User-Agent header (`BakaMitai/<versionName> (Android)`)
  - JSON negotiation registered for `ContentType.Any` (see [§1](#1-basics))
  - `ignoreUnknownKeys = true`, `coerceInputValues = true`, `explicitNulls = false`
  - sensible timeouts
- **Polymorphic roots.** Decode `f=search`, `f=show` and paged `f=latest` responses through `JsonElement` before mapping:
  - `[]` = empty
  - `{"error": …}` = end of pages
- **Be a polite scraper.** SubsPlease is a volunteer-run site:
  - no polling and no background sync
  - fetch on screen open and on pull-to-refresh only
- **In-memory caches**, all invalidated by pull-to-refresh:
  - All shows list: for the process lifetime
  - weekly schedule: about 1 h
  - show page plus episodes, per slug: about 10 min
- **Errors** go through `safeApiCall` → `DomainError` → UI error model (`best-practices` skill). A scrape that can't find a required element (for example, no `sid`) is a **parsing error**, not a crash.
- **HTML parsing** needs a library that isn't approved yet. See [context.md §7](context.md#7-open-decisions).
- **Tests:** keep trimmed real responses as fixtures (`app/src/test/resources/subsplease/…`) and test DTO decoding and HTML parsing with Ktor `MockEngine`. Cover every quirk in this file:
  - empty `[]` roots
  - `limit_reached`
  - placeholder image
  - batch and "Movie" episodes
  - mismatched HTML tags
  - entity-encoded titles
