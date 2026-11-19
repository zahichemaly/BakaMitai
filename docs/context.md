# BakaMitai — Project Context

This file is the source of truth for **what BakaMitai is**, **what each screen does**, and **how each screen gets its data**. Read it before implementing any feature.

- The SubsPlease endpoints, response shapes and quirks are documented in [api.md](api.md).
- How to write the code (layers, packages, naming, libraries, quality bar) lives in the project skills under `.claude/skills/` (`architecture`, `structure`, `conventions`, `tech-stack`, `library-management`, `best-practices`). This file never overrides them. Where the product needs something those skills don't cover yet, it's listed under [Open decisions](#7-open-decisions).

---

## 1. What the app is

**BakaMitai** is an Android app for tracking anime releases. It shows:

- what is airing **today** (still to come, and already released),
- the **weekly** release schedule,
- an A–Z catalogue of **all shows**,
- the user's **bookmarked** shows,
- a **details** page per show with its synopsis and per-episode download links.

Product principles:

- **No accounts.** No sign-in, no backend of our own. Everything the user saves (bookmarks, settings) stays on the device.
- **Material 3**: modern, sleek and easy on the eyes. Dark theme matters as much as light theme.
- **Read-only client of SubsPlease.** All anime data comes from `https://subsplease.org`, which has no official public API. We call the same undocumented JSON endpoints its own website uses, and scrape HTML only where no JSON exists (see [api.md](api.md)).
- **The app doesn't download files itself.** Download actions hand a magnet link, `.torrent` URL or XDCC page to another app (torrent client or browser).

App identity: name `BakaMitai`, package `com.zc.bakamitai`, minSdk 27. Source code: `https://github.com/zahichemaly/BakaMitai`.

---

## 2. Navigation shell

- **Top app bar** on every top-level screen: the title "BakaMitai", plus a **gear icon** (trailing action) that opens **Settings**.
- **Bottom navigation** with 4 top-level destinations, in this order:
  1. **Home**
  2. **Schedule**
  3. **All Shows**
  4. **Bookmarks**
- Tapping any anime anywhere (Home, Schedule, All Shows, Bookmarks) opens the **Details** screen for that show.
- Per the `tech-stack` skill, top-level navigation uses `NavigationSuiteScaffold` (bottom bar on phones, rail or drawer on larger windows) and Navigation 3 `NavKey`s.

---

## 3. Screens

Every network-backed screen needs **loading**, **error (with retry)** and **empty** states, plus pull-to-refresh where noted.

### 3.1 Home

Two sections, both about **today** (in the device's time zone):

| Section | Contents | Item shows |
|---|---|---|
| **Upcoming** | Shows scheduled for today that **have not been released yet** | cover, title, scheduled time, episode number |
| **Latest** | Shows **released today** already | cover, title, release time, episode number |

- No date is shown; it's always today.
- Sort both sections by time, ascending.
- Pull-to-refresh. Also refresh when the app comes back to the foreground on a new day.
- Data and derivation rules: [§4.1](#41-home).

### 3.2 Schedule

- **Tabs for each day of the week**: Monday → Sunday by default, or Sunday → Saturday when *First day of week* = Sunday (see Settings). Each tab shows the weekday and **this week's date** ("MON 16").
- The tab for **today** is selected when the screen opens, and keeps a dot while another day is selected.
- Each tab shows a **grid** of the shows airing that day, under a header with the day name, the device's UTC offset and the number of shows. Each item shows the **cover, title and release time**.
- **Today only:** each item has an **Aired** / **Upcoming** badge, and filter chips (All / Aired / Upcoming, with counts) narrow the grid. Other days have no status, because the weekly schedule has none.
- Pull-to-refresh. Also reload when the app comes back to the foreground on a new day, and select the new today.
- Data: [§4.2](#42-schedule). Plan: [2-schedule.md](2-schedule.md).

### 3.3 All Shows

- A list of **every show title**, sorted alphabetically, **grouped under sticky letter headers** ("A", "B", …), each with the number of shows under it.
- Items show **only the name**, on up to 2 lines. There's no cover on this screen.
- A **search bar** at the top filters by title as the user types. Filter locally (case-insensitive substring match), since the whole list is already on the device. When nothing matches, show a "No anime found" message.
- A row of **letter chips** under the search bar jumps to a section; the highlighted chip follows the scroll position. Only letters that are in the (filtered) list get a chip.
- Titles starting with a digit or symbol go under a single **"#"** header, placed before "A".
- A footer shows how many shows are listed ("Showing N shows").
- Pull-to-refresh fetches the list again; otherwise it's fetched once per app process.
- Data: [§4.3](#43-all-shows). Plan: [3-shows.md](3-shows.md).

### 3.4 Bookmarks

- A **grid** of the shows the user bookmarked (2 / 3 / 4 columns by width), newest first by default.
- Each item shows the **cover, title and date added** ("Added Nov 18, 2026"), plus a **next-airing chip** from the weekly schedule ("TODAY 9:30 PM", "MON 9:30 PM", or "NOT AIRING" when the show isn't on it).
- **Sort:** Recently added, Title A–Z, Release time. The choice is saved (the `bookmarks_sort` key, [§5](#5-local-persistence)).
- **Remove** from the card's bookmark button, with an **Undo** snackbar. Tapping a card opens Details.
- An **alerts chip** shows whether new-episode alerts are on. When they're on or switched off in Settings, it opens Settings; when Android blocks notifications, it offers the permission dialog or the system settings ([§3.7](#37-new-episode-alerts)).
- Pull-to-refresh refreshes the airing times. The empty state invites the user to bookmark shows, with a button to the Schedule tab.
- Works fully offline (covers load from Coil's cache when available).
- Data: [§5](#5-local-persistence). Plan: [5-bookmarks.md](5-bookmarks.md).

### 3.5 Details

Opens when the user taps any anime on Home, Schedule or All Shows (and Bookmarks once it exists). It's a pushed screen with a back arrow, the show's title in the top bar (once loaded) and no bottom bar on phones. The page has the same plain background as the other screens. The input is only the show's **slug** (the `page` value from the API, for example `sora-wa-akai-kawa-no-hotori`), because navigation keys carry ids only (`conventions` skill); the header shows a skeleton until the show page is parsed. Plan: [4-details.md](4-details.md).

Content:

- **Cover**, **title**, and a full-width **Bookmark** button under them.
- **Synopsis**: collapsed to 3 lines, with an expand/collapse toggle. When the show has none (or only SubsPlease's "will be added later" placeholder), the card says "No synopsis yet." with no toggle.
- **Downloads** section: a list of episodes, newest first, all collapsed when the screen opens. If the show has batches, show them in their own group above the episodes. Each episode expands to its links, **grouped by resolution** (1080p / 720p / SD). Each resolution offers **3 sources**:
  - **Magnet**: opens the `magnet:` URI with an installed torrent client.
  - **Torrent**: opens the `.torrent` URL (hosted on nyaa.si).
  - **XDCC**: opens SubsPlease's XDCC search page for that file in the browser.
  - If a resolution is missing for an episode, show it as disabled rather than hiding it, matching the site.
- If there are no episodes **and** no batches, show "No episodes available for this show yet."
- SubsPlease returns at most 52 episodes; when a show hits that cap, a short note says older episodes aren't available.
- Pull-to-refresh reloads the page and the episodes.

Actions:

- **Share** (top bar): share sheet (`ACTION_SEND`) with the title and `https://subsplease.org/shows/<slug>/`.
- **Bookmark** (hero button): toggles the bookmark. Filled icon and "Bookmarked" when bookmarked.
- **Open in web** (top bar): opens `https://subsplease.org/shows/<slug>/` in the browser.

If no app can handle a magnet or torrent link (`ActivityNotFoundException`), show a snackbar ("No torrent app found") with a **Copy link** action.

Data: [§4.4](#44-details).

### 3.6 Settings

A pushed screen (back arrow, no bottom bar on phones), opened from the **gear** in the top bar of every tab. A list of preferences, saved locally; each change applies at once, with no toast (a snackbar only if saving fails). **Picker settings** (*First day of week*, *Time format*, *Theme*) are rows that show their current value; tapping one opens a dialog with the options (choosing one saves it and closes the dialog). *New-episode alerts* is a switch:

| Preference | Options | Default | Effect |
|---|---|---|---|
| **First day of week** | Monday, Sunday | Monday | Tab order on Schedule |
| **Time format** | 12-hour (AM/PM), 24-hour | 12-hour | Every time shown in the app, including screens already open |
| **Theme** | System default, Light, Dark | System default | App color scheme and the system bars' icon colors, applied before the first frame |
| **New-episode alerts** | On, Off | On | Off → no background checks' requests and no permission request on the first bookmark ([§3.7](#37-new-episode-alerts)); turning it on asks for the notification permission if Android blocks notifications |
| **About** | — | — | Opens `https://github.com/zahichemaly/BakaMitai` |

**Footer**: "BakaMitai v{versionName}" (the build's `versionName`) and the disclaimer "Unofficial app. Release data from subsplease.org." Plan: [6-settings.md](6-settings.md).

### 3.7 New-episode alerts

Every bookmarked show gets a local notification when its next episode is **out on SubsPlease** (there's no push service; the app checks in the background).
- A background job (WorkManager) runs about 10 minutes after the show's weekly slot, confirms the release with one request that covers every bookmark, and retries over 24 hours while the episode is late ([api.md §7](api.md#7-client-guidelines) has the request budget).
- One notification per show, with the episode ("Episode 9 is out"); several new episodes after a long offline period give one notification with the count. Revisions (`1216v2`) and old episodes released late never notify.
- Tapping it opens that show's **Details** on top of the Home tab; Back returns to Home.
- Android 13+ asks for the notification permission when the user adds their first bookmark (unless alerts are switched off in Settings). Denied → bookmarks still work, no checks run, and the Bookmarks screen's alerts chip offers to turn alerts on.
- **Global switch:** *New-episode alerts* in Settings (default on). Off → checks make no requests and resolve silently, so turning it back on doesn't send a burst of old alerts.
- **Limits:** timing is best effort (about 10–15 min on a phone in use, up to an hour or two when idle, up to about a day for a rarely used app); some manufacturers' battery managers can hold checks back; shows not on the weekly schedule (movies, specials) only notify when another show's check sees them; batches never notify; a force stop stops checks until the app is opened. Plan: [5-bookmarks.md §5](5-bookmarks.md#5-notification-feasibility).

---

## 4. Data mapping per screen

Every call below passes `tz=<device IANA zone>`. Response shapes and quirks are in [api.md](api.md).

### 4.1 Home

The full derivation is in [1-home.md §4.3](1-home.md#43-derivation-domain); this is the summary.

1. Fetch these **in parallel**:
   - **today's schedule** ([api.md §3.1](api.md#31-todays-schedule)), required
   - **latest releases** ([api.md §3.3](api.md#33-latest-releases), `p=1`), required
   - **the newest releases** from search with an empty query ([api.md §3.4](api.md#34-search-releases)), best effort. `f=latest` sometimes lags by days, and this one doesn't.

   Then fetch `p=2` (best effort) only if page 1 isn't empty and every item on it was released today.
2. **Known releases** = page 1 + newest + page 2, with duplicates removed by `(page, episode)`.
3. **Latest** = known releases whose `release_date`, converted to the device zone, falls on **today's local date**. Time = the release time. Use the `episode` field as-is (it can be a batch range or "Movie"). The same show can appear more than once on one day, for example two episodes. Show each release separately.
   - Why not use `aired == true` from the schedule? The schedule doesn't carry episode numbers, and it misses unscheduled releases such as batches.
   - `release_date` is currently an hour early ([api.md §3.5](api.md#35-show-episodes)). It's shown as-is.
4. **Upcoming** = schedule items with `aired != true` **whose show isn't already in Latest**, which covers `aired` lagging the real release ([api.md §3.1](api.md#31-todays-schedule)). Time = the schedule `time`.
5. **Episode number for Upcoming items** = the highest *numeric* episode already released for that `page`, plus 1:
   - First look in the known releases you already have (match on `page`).
   - If the show isn't there (for example, it last aired a week ago), call search ([api.md §3.4](api.md#34-search-releases)) with the title, keep results where `page == slug`, and take the highest numeric `episode`. Run these lookups in parallel. There are only about 10 per day.
   - If it's still unknown, hide the episode badge. Never guess.
6. **Clock and zone** for "today" and time conversions: inject a `kotlin.time.Clock` (`kotlinx.datetime.Clock` is deprecated in kotlinx-datetime 0.8) and a `TimeZoneProvider` (`common/domain/util/`). Don't call system time or `TimeZone.currentSystemDefault()` directly. `CommonModule` provides the `Clock`.

### 4.2 Schedule

- Two calls **in parallel**: the weekly schedule ([api.md §3.2](api.md#32-weekly-schedule)), required, and today's schedule ([api.md §3.1](api.md#31-todays-schedule)), best effort. Map weekday names to `DayOfWeek`.
- **Today's `aired`** is joined onto today's tab by slug (today's bucket and today's schedule list the same shows). If today's call fails, today's cards simply have no status.
- **Week dates:** the 7 dates of the current week, starting on the first day of the week, computed from the injected `Clock` and `TimeZoneProvider`.
- **Tab order** comes from the *First day of week* setting (default Monday); Schedule re-reads it when the screen starts, so a change in Settings applies when you come back.
- **Selected tab on open** = today's `DayOfWeek` in the device zone.
- Grid items: cover, title, `time` formatted per the *Time format* setting, sorted by time and then title.
- **Caching:** the weekly schedule is kept in memory for about 1 h per zone ([api.md §7](api.md#7-client-guidelines)); pull-to-refresh bypasses it. Today's schedule is never cached, since `aired` changes during the day.

### 4.3 All Shows

- Scrape the all shows list ([api.md §4.1](api.md#41-all-shows-list)) into `List<Show(slug, title)>`, off the main thread. A page that yields no shows is a parsing error (`DomainError.InvalidData`), not an empty list.
- Remove exact duplicates only (same slug **and** title): several titles can share one slug, and each keeps its own row.
- Sort with a **case-insensitive** comparator.
- Group by first character: `A`–`Z` → that letter, anything else → `#`. `#` comes first.
- Search is **local** filtering. **Don't** call the search endpoint for it: that endpoint searches releases, not the show catalogue, and it's capped.

### 4.4 Details

1. Fetch the **show page** ([api.md §4.2](api.md#42-show-page)) to get the title, synopsis, cover and `sid`.
2. Then fetch **show episodes** ([api.md §3.5](api.md#35-show-episodes)) with that `sid` to get the episodes and batches.
   - The two calls are sequential, because `sid` is only in the HTML.
   - Show the header (title, cover, synopsis) as soon as step 1 finishes, while the downloads section is still loading.
3. **Episodes:** sort by `release_date`, newest first. Label each one with its episode string, plus the release date in the device zone.
4. **Downloads per episode:** group by resolution (1080p → 720p → SD; see [api.md §3.6](api.md#36-resolutions)). Each has Magnet, Torrent and XDCC. The `sid` and every URL are handled in `data`; the domain gets ready-to-open links.
   - XDCC URL = `https://subsplease.org/xdcc/?search=` + that resolution's `xdcc` value (see [api.md §5](api.md#5-xdcc)).
5. **Bookmark state:** observe whether `slug` is bookmarked through a DataStore-backed `Flow`. Toggling saves `{slug, title, coverUrl, addedAt = clock.now()}` or removes it.

### 4.5 Formatting rules (all screens)

- **Times:** the API's `"HH:mm"` → `LocalTime` → format with kotlinx-datetime according to the setting: The *Time format* setting applies everywhere, and screens already open update immediately (times are formatted when they're drawn). Notification timestamps are drawn by the system in the device's own format.
  - 12-hour: `amPmHour()`, `:`, `minute()`, space, `amPmMarker("AM", "PM")` → "8:00 AM"
  - 24-hour: `hour()`, `:`, `minute()` → "08:00"
- **Release instants:** `Instant` → `toLocalDateTime(timeZoneProvider.current())`, then format the same way.
- **Bookmark "added" date:** a short, locale-appropriate date, e.g. "Nov 18, 2026".

---

## 5. Local persistence

Settings use **Jetpack DataStore**; bookmarks (and their alert-tracking state) use **Room** (approved by the user, 2026-11-19, Bookmarks plan).

| Data | Store | Shape |
|---|---|---|
| **Settings** | One Preferences DataStore file `settings` (`common/data/local/SettingsLocalDataSource`, `SettingsRepository`, `AppSettings` in `common/`) | Keys stored by enum name: `week_start` (`MONDAY` \| `SUNDAY`), `time_format` (`TWELVE_HOUR` \| `TWENTY_FOUR_HOUR`), `theme_mode` (`SYSTEM` \| `LIGHT` \| `DARK`), `new_episode_alerts` (Boolean, default `true`), and Bookmarks' `bookmarks_sort` (`RECENTLY_ADDED` \| `TITLE` \| `NEXT_AIRING`) |
| **Bookmarks** | Room database `bookmarks.db` (`feature/bookmarks/data/local/`), one table, schema exported to `app/schemas/` | `slug` (primary key), `title`, `cover_url`, `added_at`; the weekly slot (`airing_day`, `airing_minute`, `airing_zone`, `airing_title`, `airing_checked_at`); alert tracking (`tracking_since`, `last_seen_episode`, `last_seen_released_at`, `resolved_through`). Details in [5-bookmarks.md §4.3](5-bookmarks.md#43-local-data). |

- The **slug** is the bookmark key. It's what Details needs to reload the show.
- Store the **absolute** cover URL so Bookmarks renders without extra lookups.
- DataStore reads **MUST** catch `IOException` and emit defaults (`best-practices` §4.2). Room errors map to `DomainError.Unknown`.
- The Details plan's bookmarks DataStore (`bookmarks.json`) was replaced without migration: the app hadn't shipped.
- The theme setting applies before the first frame: `AppViewModel` reads the settings, `AppRoute` draws nothing until they've loaded, and `MainActivity` holds the first draw until then, also setting the status- and navigation-bar icon colors from the app's theme. Known limit: the system splash still follows the device's dark mode when the theme is forced.

---

## 6. Suggested feature breakdown

Follow the `structure` skill's `feature/[name]/{data,domain,di,presentation}` layout.

| Feature package | Screen(s) | Remote / local sources |
|---|---|---|
| `home` | Home | today's schedule, latest releases, search |
| `schedule` | Schedule | weekly schedule |
| `shows` | All Shows | all shows list (HTML) |
| `details` | Details | show page (HTML), show episodes |
| `bookmarks` | Bookmarks (+ the bookmark toggle used by Details, exposed through its `domain` use cases) and new-episode alerts | Bookmarks Room database, weekly schedule, search releases (alerts) |
| `settings` | Settings | Settings DataStore (repository in `common/`) |

Shared across features (`common/`): the SubsPlease `HttpClient` ([api.md §7](api.md#7-client-guidelines)), any DTO used by two or more features (e.g. the schedule item and today's schedule, moved there by the Schedule plan), image-URL resolution (relative path → absolute, placeholder → `null`), the time formatter driven by the *Time format* setting, the UTC-offset label, `CoverImageView`, the top-level app bar and `EmptyStateView` (added by the All Shows plan, shared with Schedule). [2-schedule.md §5.1](2-schedule.md#51-shared-code-moved-from-featurehome) lists the moves.

A sketch of the domain models. Names follow the `conventions` skill: domain models take no suffix.

```kotlin
@JvmInline value class ShowSlug(val value: String)

data class Show(val slug: ShowSlug, val title: String, val coverUrl: String?)

data class ScheduledShow(val show: Show, val time: LocalTime, val aired: Boolean?)   // aired is null for weekly

data class Release(val show: Show, val episode: String, val releasedAt: Instant, val downloads: List<Download>)

data class ShowDetails(val show: Show, val synopsis: String?, val episodes: List<Episode>, val batches: List<Episode>)

data class Episode(val label: String, val releasedAt: Instant, val downloads: List<Download>)

data class Download(val resolution: Resolution, val magnetUri: String, val torrentUrl: String?, val xdccUrl: String?)

enum class Resolution { SD, HD, FHD }   // keep the raw value (e.g. 480) for the label

data class Bookmark(val show: Show, val addedAt: Instant)
```

---

## 7. Open decisions

1. ~~**HTML parser.**~~ **Decided (2026-11-19):** **Ksoup** (`com.fleeksoft.ksoup:ksoup`, a Kotlin port of jsoup), `data` layer only, behind a `[Name]HtmlParser` class per page. Added by the All Shows plan; Details reuses it.
2. **Custom Tabs vs. external browser** for "Open in web", Torrent and XDCC.
   - `androidx.browser` isn't approved either.
   - **Chosen for v1 (Details, 2026-11-19):** a plain `ACTION_VIEW` intent. Custom Tabs would need `androidx.browser` approved first.
3. ~~**Dynamic color (Material You)** on Android 12+ versus a fixed brand palette.~~ **Decided:** fixed brand palette from [DESIGN.md](DESIGN.md), no dynamic color. DESIGN.md defines the dark scheme; the light scheme is derived from the same tonal palettes, so the *Theme* setting (System / Light / Dark) still applies.

4. **Decided (2026-11-19, Bookmarks plan):** bookmarks are stored with **Room**, and new-episode alerts run with **WorkManager** (and `koin-androidx-workmanager` for injected workers). Both were approved by the user; the alerts follow the request budget in [api.md §7](api.md#7-client-guidelines).
