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

- A list of **every show title**, sorted alphabetically, **grouped under sticky letter headers** ("A", "B", …).
- Items show **only the name**. There's no cover on this screen.
- A **search bar** at the top filters by title as the user types. Filter locally (case-insensitive substring match), since the whole list is already on the device.
- Titles starting with a digit or symbol go under a single **"#"** header, placed before "A".
- Data: [§4.3](#43-all-shows).

### 3.4 Bookmarks

- A **grid** of the shows the user bookmarked, newest first.
- Each item shows the **cover, title and date added**, formatted for the user's locale.
- Empty state invites the user to bookmark shows from the Details screen.
- Works fully offline (covers load from Coil's cache when available).
- Data: [§5](#5-local-persistence).

### 3.5 Details

Opens when the user taps any anime. The input is the show's **slug** (the `page` value from the API, for example `sora-wa-akai-kawa-no-hotori`). Callers may also pass the title and cover they already have, so the header renders immediately while the rest loads.

Content:

- **Cover**, **title**
- **Synopsis**: collapsed to a few lines, with an expand/collapse toggle.
- **Downloads** section: a list of episodes, newest first. If the show has batches, show them in their own group above the episodes. Each episode expands to its links, **grouped by resolution** (480p / 720p / 1080p). Each resolution offers **3 sources**:
  - **Magnet**: opens the `magnet:` URI with an installed torrent client.
  - **Torrent**: opens the `.torrent` URL (hosted on nyaa.si).
  - **XDCC**: opens SubsPlease's XDCC search page for that file in the browser.
  - If a resolution is missing for an episode, show it as disabled rather than hiding it, matching the site.
- If there are no episodes, show "No episodes available for this show yet."

Actions (top bar):

- **Share**: share sheet (`ACTION_SEND`) with the title and `https://subsplease.org/shows/<slug>/`.
- **Bookmark**: toggles the bookmark. Filled icon when bookmarked.
- **Open in web**: opens `https://subsplease.org/shows/<slug>/` in the browser.

If no app can handle a magnet or torrent link (`ActivityNotFoundException`), show a snackbar ("No torrent app found") with a **Copy link** action.

Data: [§4.4](#44-details).

### 3.6 Settings

A list of preferences, saved locally:

| Preference | Options | Default | Effect |
|---|---|---|---|
| **First day of week** | Monday, Sunday | Monday | Tab order on Schedule |
| **Time format** | 12-hour (AM/PM), 24-hour | 12-hour | Every time shown in the app |
| **Theme** | System, Light, Dark | System | App color scheme |
| **About** | — | — | Opens `https://github.com/zahichemaly/BakaMitai` |

**Footer**: app name and version, for example "BakaMitai v1.0", with the version taken from the build's `versionName`.

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
- **Tab order** comes from the *First day of week* setting. Until Settings exists, the week starts on **Monday** and times are **12-hour**.
- **Selected tab on open** = today's `DayOfWeek` in the device zone.
- Grid items: cover, title, `time` formatted per the *Time format* setting, sorted by time and then title.
- **Caching:** the weekly schedule is kept in memory for about 1 h per zone ([api.md §7](api.md#7-client-guidelines)); pull-to-refresh bypasses it. Today's schedule is never cached, since `aired` changes during the day.

### 4.3 All Shows

- Scrape the all shows list ([api.md §4.1](api.md#41-all-shows-list)) into `List<Show(slug, title)>`.
- Sort with a **case-insensitive** comparator.
- Group by first character: `A`–`Z` → that letter, anything else → `#`. `#` comes first.
- Search is **local** filtering. **Don't** call the search endpoint for it: that endpoint searches releases, not the show catalogue, and it's capped.

### 4.4 Details

1. Fetch the **show page** ([api.md §4.2](api.md#42-show-page)) to get the title, synopsis, cover and `sid`.
2. Then fetch **show episodes** ([api.md §3.5](api.md#35-show-episodes)) with that `sid` to get the episodes and batches.
   - The two calls are sequential, because `sid` is only in the HTML.
   - Show the header (title, cover, synopsis) as soon as step 1 finishes, while the downloads section is still loading.
3. **Episodes:** sort by `release_date`, newest first. Label each one with its episode string, plus the release date in the device zone.
4. **Downloads per episode:** group by resolution (SD → 720p → 1080p; see [api.md §3.6](api.md#36-resolutions)). Each has Magnet, Torrent and XDCC.
   - XDCC URL = `https://subsplease.org/xdcc/?search=` + that resolution's `xdcc` value (see [api.md §5](api.md#5-xdcc)).
5. **Bookmark state:** observe whether `slug` is bookmarked through a DataStore-backed `Flow`. Toggling saves `{slug, title, coverUrl, addedAt = clock.now()}` or removes it.

### 4.5 Formatting rules (all screens)

- **Times:** the API's `"HH:mm"` → `LocalTime` → format with kotlinx-datetime according to the setting:
  - 12-hour: `amPmHour()`, `:`, `minute()`, space, `amPmMarker("AM", "PM")` → "8:00 AM"
  - 24-hour: `hour()`, `:`, `minute()` → "08:00"
- **Release instants:** `Instant` → `toLocalDateTime(timeZoneProvider.current())`, then format the same way.
- **Bookmark "added" date:** a short, locale-appropriate date, e.g. "Nov 18, 2026".

---

## 5. Local persistence

Everything is stored with **Jetpack DataStore**. That's the only approved persistence library, so there's no Room/SQL.

| Data | Store | Shape |
|---|---|---|
| **Settings** | Preferences DataStore (`common/data/local/`; the `structure` skill already plans `SettingsRepository` and `AppSettings` in `common/`) | `firstDayOfWeek: MONDAY \| SUNDAY`, `timeFormat: TWELVE_HOUR \| TWENTY_FOUR_HOUR`, `theme: SYSTEM \| LIGHT \| DARK` |
| **Bookmarks** | Typed DataStore with a kotlinx.serialization JSON serializer | `List<BookmarkEntity(slug: String, title: String, coverUrl: String?, addedAtEpochMillis: Long)>`, unique by `slug` |

- The **slug** is the bookmark key. It's what Details needs to reload the show.
- Store the **absolute** cover URL so Bookmarks renders without extra lookups.
- DataStore reads **MUST** catch `IOException` and emit defaults (`best-practices` §4.2).
- The theme setting must apply before first frame, so the app root reads it (`AppRoute`/`AppViewModel` in the `structure` skill).

---

## 6. Suggested feature breakdown

Follow the `structure` skill's `feature/[name]/{data,domain,di,presentation}` layout.

| Feature package | Screen(s) | Remote / local sources |
|---|---|---|
| `home` | Home | today's schedule, latest releases, search |
| `schedule` | Schedule | weekly schedule |
| `shows` | All Shows | all shows list (HTML) |
| `details` | Details | show page (HTML), show episodes |
| `bookmarks` | Bookmarks (+ the bookmark toggle used by Details, exposed through its `domain` use cases) | Bookmarks DataStore |
| `settings` | Settings | Settings DataStore (repository in `common/`) |

Shared across features (`common/`): the SubsPlease `HttpClient` ([api.md §7](api.md#7-client-guidelines)), any DTO used by two or more features (e.g. the schedule item and today's schedule, moved there by the Schedule plan), image-URL resolution (relative path → absolute, placeholder → `null`), the time formatter driven by the *Time format* setting, the UTC-offset label, `CoverImageView` and the top-level app bar. [2-schedule.md §5.1](2-schedule.md#51-shared-code-moved-from-featurehome) lists the moves.

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

1. **HTML parser.** All Shows and Details need HTML scraping, but no HTML parser is in the approved `tech-stack` list. The `tech-stack` skill says adding a library requires updating that skill first.
   - **Recommended:** add **Ksoup** (`com.fleeksoft.ksoup:ksoup`, a Kotlin port of jsoup), used only in `data`.
   - **Alternative:** narrowly scoped regex extraction in `data`. This is more fragile against markup changes.
2. **Custom Tabs vs. external browser** for "Open in web", Torrent and XDCC.
   - `androidx.browser` isn't approved either.
   - **Default:** a plain `ACTION_VIEW` intent (or Compose `LocalUriHandler`) until a decision is made.
3. ~~**Dynamic color (Material You)** on Android 12+ versus a fixed brand palette.~~ **Decided:** fixed brand palette from [DESIGN.md](DESIGN.md), no dynamic color. DESIGN.md defines the dark scheme; the light scheme is derived from the same tonal palettes, so the *Theme* setting (System / Light / Dark) still applies.
