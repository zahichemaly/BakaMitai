# 1. Home

> **Status:** Implemented · **Design:** [Google Stitch project `5201581584167289517`, screen `4b4780fd73dc42a19a12891c56942249`](https://stitch.google.com/projects/5201581584167289517?node-id=4b4780fd73dc42a19a12891c56942249) · **Planned:** 2026-11-18
> **Spec:** [context.md §2](context.md#2-navigation-shell), [§3.1](context.md#31-home), [§4.1](context.md#41-home), [§4.5](context.md#45-formatting-rules-all-screens) · **API:** [api.md §1](api.md#1-basics), [§3.1](api.md#31-todays-schedule), [§3.3](api.md#33-latest-releases), [§3.4](api.md#34-search-releases), [§7](api.md#7-client-guidelines)
> **Depends on:** none (first feature). It also builds the app's navigation shell.

**Design source.** The design is a Google Stitch project, not Figma or Sketch. Only one screen was exported (Stitch Export → .zip): a 390×1387 dark-mode frame, plus its HTML/Tailwind and `DESIGN.md`, which matches `docs/DESIGN.md`. Every "node" below refers to Stitch screen **`4b4780fd73dc42a19a12891c56942249`** in project **`5201581584167289517`**, and to the named HTML section inside it. The PNG renders the "Today's Broadcasts" banner blank, so its content comes from the HTML.

**How design and spec are reconciled.** `context.md` decides **behaviour and data**: what's listed, sort order, time format and navigation. The Stitch design decides **visual treatment**: layout, components, tokens and section titles. Anything in the design that the API can't supply is left out, never invented. Every disagreement is listed in [§3](#3-design-vs-spec).

---

## 1. Summary

Home is the app's start destination and its first bottom-navigation tab. It shows **today** in the device's time zone:

- **Today's Broadcasts banner:** the device's UTC offset, plus how many shows are still to air today and how many were released today.
- **Upcoming Today:** a horizontal carousel of shows scheduled for today that haven't aired yet. Each card has the cover, title, scheduled time and next episode number.
- **Latest Releases:** a list of everything released today. Each row has the cover, title, release time and episode label.

The user can pull to refresh. Home also reloads by itself when the app returns to the foreground on a new local day.

**Entry points:** the app's launch screen and the **Home** tab in the `NavigationSuiteScaffold`.

**In scope:**
- the Home feature (`feature/home/`, all four layers)
- the **app navigation shell**: `AppNavigation`, `TopLevelDestination`, `AppEntryProvider`, and `AppRoute` switching from the wordmark to the shell
- a `Clock` provider and a `TimeZoneProvider` in `common/`
- doc updates for the API findings in [§4](#4-data)

**Out of scope, with hooks left for later plans:**
- **Details screen.** Tapping a card does nothing until the Details plan passes `onOpenDetails` ([§10 Q5](#10-open-questions)).
- **Settings screen** and the *Time format* setting. The gear is hidden, and times use the spec's 12-hour default ([Q5](#10-open-questions), [Q9](#10-open-questions)).
- **Schedule, All Shows and Bookmarks tabs.** They're shown but disabled ([Q4](#10-open-questions)).
- **Things the design shows that the API can't supply:** bookmark toggles, ratings, genres, episode titles, countdowns, relative "ago" times, filters, broadcast alerts. See [§3](#3-design-vs-spec).

---

## 2. Design breakdown

### 2.0 Token mapping

Every design token maps onto the existing theme. **No new theme work is needed.**

| Design (Tailwind/HTML) | Compose |
|---|---|
| `bg-surface` | `colorScheme.surface` |
| `surface-container-low / container / container-high / container-highest` | `colorScheme.surfaceContainerLow / surfaceContainer / surfaceContainerHigh / surfaceContainerHighest` |
| `surface-dim/80`, `surface-container-highest/80` | `colorScheme.surfaceDim.copy(alpha = 0.8f)`, `surfaceContainerHighest.copy(alpha = 0.8f)` (alpha as named `private const`s) |
| `primary`, `primary/10`, `primary/20` | `colorScheme.primary`, `.copy(alpha = 0.1f / 0.2f)` |
| `secondary`, `secondary-container/20` | `colorScheme.secondary`, `colorScheme.secondaryContainer.copy(alpha = 0.2f)` |
| `tertiary`, `tertiary-container/30` | `colorScheme.tertiary`, `colorScheme.tertiaryContainer.copy(alpha = 0.3f)` |
| `on-surface`, `on-surface-variant`, `outline` | `colorScheme.onSurface`, `onSurfaceVariant`, `outline` |
| Glow blobs (`secondary-container/15`, `primary-container/20`, blurred) | `BakaMitaiTheme.extendedColors.glowSecondary` / `glowPrimary` as radial gradients |
| `headline-md` / `headline-sm` / `title-md` | `typography.headlineMedium` / `headlineSmall` / `titleMedium` |
| App-bar title (`headline-sm` + `font-bold`) | `typography.titleLarge` (Bold 18 sp, "App bar titles") |
| `body-sm` / `label-md` / `label-sm` | `typography.bodySmall` / `labelMedium` / `labelSmall` |
| `px-margin`, `gap-space-*`, `space-xl`, `gutter` | `BakaMitaiTheme.spacing.margin`, `.xs/.sm/.md/.lg/.xl`, `.gutter` (`.marginTablet` from medium width) |
| `rounded-lg` (32 px) on the banner | `MaterialTheme.shapes.extraLarge` ("hero spotlight banners") |
| `rounded-lg` (32 px) on cards | `MaterialTheme.shapes.large` (24 dp, DESIGN.md "poster cards"). See [§3](#3-design-vs-spec) row 20. |
| `rounded-DEFAULT` (16 px) thumbnails | `MaterialTheme.shapes.medium` |
| `rounded-full` pills | `CircleShape` |

### 2.1 App shell: top app bar and navigation suite

Nodes: HTML `<header>` and `<nav>` of screen `4b4780fd…`.

- **Layout:**
  - **Top app bar** (64 dp): title column on the left ("BakaMitai", with the subtitle "HOME" under it) and a trailing settings action.
  - **Navigation:** bottom bar with 4 destinations: Home, Schedule, All Shows, Bookmarks.
  - **Adaptive:**
    - **Compact:** bottom bar.
    - **Medium and expanded:** navigation rail. This is `NavigationSuiteScaffold`'s default `calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())`.
    - The top bar is the same on every size.
- **Components:**
  - `TopAppBar` (M3 small), in a private `HomeTopAppBar` inside `HomeScreen.kt`.
    - Title slot: a `Column` of two `Text`s.
    - Action slot: an `IconButton` with `Icons.Outlined.Settings`.
    - The bar is per screen because Details will need its own. When the Schedule plan adds a second top-level screen, move it to `common/ui/component/toplevelappbar/`.
  - `NavigationSuiteScaffold` in `app/navigation/AppNavigation.kt`, with one `item(...)` per `TopLevelDestination`.
- **Content:**
  - Title: `StringResource.app_name`.
  - Subtitle: `StringResource.home_app_bar_subtitle` ("HOME", stored uppercase).
  - Navigation labels: `nav_home`, `nav_schedule`, `nav_all_shows`, `nav_bookmarks`.
- **States:**
  - Home is selected.
  - Schedule, All Shows and Bookmarks are **disabled** (`enabled = false`) until their plans land ([Q4](#10-open-questions)).
  - The settings action is **hidden** until Settings exists ([Q5](#10-open-questions)).
- **Interactions:**
  - Tapping an enabled tab calls `navigator.navigate(key)`.
  - System back goes through `navigator.goBack()`. At Home's root, `NavDisplay` leaves the app.
  - The top bar uses `TopAppBarDefaults.pinnedScrollBehavior()`. It tints to `scrolledContainerColor` when content scrolls under it.
- **Theming:**
  - **Top bar:** `topAppBarColors(containerColor = surface, scrolledContainerColor = surfaceContainer)`. Title is `titleLarge` in `onSurface`. Subtitle is `labelSmall` in `primary`. Settings icon is `onSurfaceVariant`.
  - **Navigation suite:**
    - `NavigationSuiteDefaults.colors(navigationBarContainerColor = surfaceContainerHigh, navigationRailContainerColor = surfaceContainer)`.
    - Item colors (bar and rail): selected icon and text `primary`, indicator `primary.copy(alpha = NAV_INDICATOR_ALPHA = 0.2f)`, unselected icon and text `onSurfaceVariant`.
    - Labels use `labelMedium`.
  - **Icons** (filled when selected, outlined when not): `Icons.Filled/Outlined.Home`, `Icons.Filled/Outlined.CalendarMonth`, `Icons.AutoMirrored.Filled/Outlined.ViewList`, `Icons.Filled.Bookmark` / `Icons.Outlined.BookmarkBorder`. These need the icon library in [Q1](#10-open-questions).
  - **Proposed:**
    - Backdrop blur (`backdrop-blur-xl`) can't be done with M3, so the bars use solid tonal colors.
    - The design's glow shadow under the active indicator is dropped.
- **Accessibility:**
  - Navigation icons have `contentDescription = null`, because the label names the item. Disabled items are announced as disabled.
  - The settings button's description is `cd_settings`. `IconButton` gives a 48 dp target.

### 2.2 "Today's Broadcasts" banner

Node: HTML `<!-- Ambient Micro-Glance Banner -->`. It renders blank in the PNG.

- **Layout:** a full-width rounded container with `spacing.margin` side padding and `spacing.sm` top padding. Inside, with `spacing.md` padding:
  - row 1: the title "Today's Broadcasts" on the left; the zone label (clock icon + text) on the right
  - row 2: two count chips, `spacing.sm` apart
- **Components:** `component/todaysummary/TodaySummaryView.kt`.
  - The container is a `Surface(shape = shapes.extraLarge, color = surfaceContainer)`.
  - The two glows are drawn with `Modifier.drawBehind { drawCircle(Brush.radialGradient(...)) }`:
    - `glowSecondary`, centered at the top-right corner
    - `glowPrimary`, centered at the bottom-left corner
  - Each chip is a `Surface(shape = CircleShape)` holding a 6 dp status dot and a `Text`.
- **Content** (`TodaySummaryUIModel`):
  - `utcOffsetLabel`: the **device** offset, for example "UTC-5", "UTC+9", "UTC+5:30", or "UTC" at zero. It replaces "JST (UTC+9)" ([§3](#3-design-vs-spec) row 10).
  - `upcomingCount`: plural "%d Airing".
  - `releasedCount`: plural "%d Available".
- **States:** shown whenever content is loaded, including zero counts. Not shown during first load or the full-screen error.
- **Interactions:** none.
- **Theming:**
  - Title: `headlineMedium`, `onSurface`.
  - Zone label: `labelMedium` in `onSurfaceVariant`, with `Icons.Outlined.Schedule` at 16 dp in `primary`.
  - "Airing" chip: `primary` text on `primary@0.1`, with a `primary` dot that pulses. The pulse is a `rememberInfiniteTransition` alpha, read in `Modifier.graphicsLayer { alpha = … }` so it doesn't recompose (`best-practices` §1.3).
  - "Available" chip: `secondary` text on `secondaryContainer@0.2`, with a static `secondary` dot.
  - Chip text: `labelSmall`.
  - Chip padding: 10 dp horizontal and 4 dp vertical, as named `Dp` constants (DESIGN.md status badge "2px 10px").
- **Accessibility:**
  - `Modifier.semantics(mergeDescendants = true) {}` on the container, so TalkBack reads it as one item ("Today's Broadcasts, UTC-5, 4 Airing, 5 Available").
  - The title also has `semantics { heading() }`.
  - Dots and icon are decorative (`null` descriptions).

### 2.3 "Upcoming Today" section and card

Nodes: HTML `<!-- Section 1: Upcoming Broadcasts -->` and the `<article>` cards (Dandadan, Solo Leveling, …).

- **Layout:**
  - **Section header** (private `SectionHeader` in `HomeScreen.kt`, with `spacing.margin` side padding):
    - `Icons.Outlined.Schedule` (20 dp, `primary`)
    - the title "Upcoming Today" (`headlineSmall`)
    - a count pill "9 shows" (`labelSmall`, `primary` on `surfaceContainerHigh`)
    - The "Timeline >" link is **omitted** ([§3](#3-design-vs-spec) row 7).
  - **Carousel:**
    - A `LazyRow` with `contentPadding = PaddingValues(horizontal = margin)` and `horizontalArrangement = spacedBy(spacing.md)`.
    - Snap-to-start: `flingBehavior = rememberSnapFlingBehavior(listState, SnapPosition.Start)`.
    - Edge to edge: the carousel scrolls under the screen margins. See the breakout in [§5](#5-architecture).
  - **Card** (`UPCOMING_CARD_WIDTH = 200.dp` on every window size):
    - Poster: `aspectRatio(POSTER_ASPECT_RATIO = 3f / 4.2f)`, then a bottom gradient scrim.
    - Overlays at the poster's bottom, inset `spacing.sm`: the episode badge bottom-left; the time row bottom-right (`Icons.Outlined.Alarm` 14 dp + time).
    - Body below the poster (padding `spacing.sm`): the title.
  - **Adaptive:** cards stay 200 dp. Wider windows simply show more of them. Side margins are `spacing.margin` on compact and `spacing.marginTablet` from medium up.
- **Components:**
  - `component/upcomingcard/UpcomingCardView.kt`, built on M3 `Card`:
    - `Card(onClick = …)` when `isClickable`, otherwise the non-clickable `Card(...)` overload
    - `CardDefaults.cardColors(containerColor = surfaceContainerLow)`, zero elevation
  - `component/coverimage/CoverImageView.kt` for the poster (Coil `AsyncImage`).
  - `component/episodebadge/EpisodeBadgeView.kt` for the badge.
- **Content** (`UpcomingCardUIModel`):
  - `title`: the schedule `title`, `maxLines = 2`, `minLines = 2`, ellipsized. Two lines replace the genres line the design shows.
  - `coverUrl`: absolute, or `null` for the placeholder.
  - `timeLabel`: the schedule `time` in 12-hour format, for example "6:30 PM".
  - `episodeBadge`: "EP 08". Hidden when the next episode is unknown.
  - The image uses `ContentScale.Crop`. When the URL is null or loading fails, the poster is a `ColorPainter(surfaceContainerHighest)`.
- **States:**
  - The section header is always shown.
  - When there are no upcoming items, the carousel is replaced by the inline text `home_upcoming_empty`. **Not in design (proposed).**
- **Interactions:**
  - Tapping a card sends `HomeIntent.ShowClicked(slug)`, but only once `onOpenDetails` is wired ([Q5](#10-open-questions)).
  - The pressed state is M3's default ripple. The design's `active:scale-[0.98]` is dropped (proposed).
- **Theming:**
  - Card shape: `shapes.large`.
  - Scrim: `Brush.verticalGradient(0f to Color.Transparent, 0.5f to surfaceContainerLow.copy(alpha = 0.4f), 1f to surfaceContainerLow)`.
  - Time row: icon in `secondary`, text in `labelSmall` / `onSurfaceVariant`.
  - Title: `titleMedium`, `onSurface`.
- **Accessibility:**
  - The cover's `contentDescription` is `null`, because the title is adjacent text.
  - A clickable `Card` merges its descendants. When the card isn't clickable, add `Modifier.semantics(mergeDescendants = true) {}`.
  - The badge exposes `episodeBadge.contentDescription` ("Episode 08").
  - The section title has `heading()`.

### 2.4 "Latest Releases" section and row

Nodes: HTML `<!-- Section 2: Latest Releases (Aired Today) -->` and its `<article>` rows (Frieren, Demon Slayer, Chainsaw Man).

- **Layout:**
  - **Section header:**
    - `Icons.Outlined.Movie` (20 dp, `secondary`)
    - "Latest Releases" (`headlineSmall`)
    - a static pill "Aired Today" (`labelSmall`, `secondary` on `surfaceContainerHigh`)
    - The filter button is **omitted** ([§3](#3-design-vs-spec) row 8).
  - **Row:**
    - `Row` with padding `spacing.sm` and a `spacing.md` gap.
    - Left: thumbnail (`RELEASE_THUMBNAIL_WIDTH = 96.dp`, `POSTER_ASPECT_RATIO`, `shapes.medium`), with the time chip overlaid bottom-left at a 6 dp inset.
    - Right: a `Column`, vertically centered (proposed), holding the title and then the episode badge `spacing.xs` below it.
  - Rows are `spacing.md` apart.
  - **Adaptive:**
    - **Compact:** 1 column.
    - **Medium:** 2 columns.
    - **Expanded:** 3 columns.
    - These are grid cells with a `spacing.gutter` gap. Side margins are as in §2.3.
- **Components:** `component/releaserow/ReleaseRowView.kt`.
  - M3 `Card` (same click and color rules as the upcoming card), `shapes.large`.
  - `CoverImageView` for the thumbnail.
  - `EpisodeBadgeView` for the badge.
  - The time chip is a `Surface(shape = CircleShape, color = surfaceDim@0.8)`.
- **Content** (`ReleaseRowUIModel`):
  - `title`: the release `show`, `maxLines = 2`.
  - `timeLabel`: the release instant converted to the device zone, in 12-hour format, for example "9:33 AM". This value is 1 h early in winter ([§4](#4-data) gap G3).
  - `episodeBadge`: "EP 07" / "BATCH 01-24" / "MOVIE".
  - The same show can appear several times, once per release.
- **States:**
  - When nothing was released today, the inline text `home_latest_empty` replaces the rows. **Not in design (proposed).**
- **Interactions:** same as the upcoming card.
- **Theming:**
  - Row container: `surfaceContainerLow`.
  - Time chip text: `labelSmall` in `secondary`. The design's 10 px bold maps to `labelSmall`, so no hardcoded weight.
  - Title: `titleMedium`.
- **Accessibility:** same as the upcoming card. The time chip's text is read as part of the merged row.

### 2.5 Episode badge (shared by both cards)

Node: the `EP 08`, `EP 28 (Finale)` and `SP 01` pills.

- `component/episodebadge/EpisodeBadgeView.kt`: a `Surface(shape = CircleShape)` holding `Text(labelMedium)`, padded 8 dp horizontally and 2 dp vertically.
- **Regular** episode: `primary` text on `primary@0.2`.
- **Special** (batch, movie, anything non-numeric): `tertiary` text on `tertiaryContainer@0.3`. This follows the design's "SP 01" style.
- The label is stored uppercase in its string resource (DESIGN.md: "episodic data & badges uppercase").

### 2.6 Elements in the design that this plan omits

These have no data or no spec ([§3](#3-design-vs-spec) gives the reasons):
- rating stars ("9.8 / 10")
- genre line
- episode titles
- "(Finale)"
- bookmark toggles on cards
- countdown chips ("In 42m")
- "Aired • 2h 45m ago" chips
- "Timeline >"
- filter button
- the "Broadcast Alerts Active / Manage" bar

### 2.7 States not in the design (all proposed)

| State | Proposal |
|---|---|
| **First load** | A shimmering skeleton of the content fills the content area, under the top bar (`best-practices` §5.3): the same grid, margins and columns, with the banner title and both section titles as real text, and `TodaySummarySkeletonView`, 3 `UpcomingCardSkeletonView`s and `columns × 3` `ReleaseRowSkeletonView`s in one `SkeletonView`. |
| **Error, no content** | `ErrorView(model = state.error, onRetry = { onIntent(HomeIntent.Retry) })`. Pull-to-refresh is not active here. |
| **Refreshing** | `PullToRefreshBox` indicator over the existing content. Content stays interactive. |
| **Refresh failed, content present** | Keep the content and show a snackbar with the error message (`HomeEffect.ShowError`). |
| **Both sections empty** | Private `HomeEmptyView` in `HomeScreen.kt`: a centered `bodyLarge` text `home_empty` ("Nothing is on the schedule for today."). It's in a vertically scrollable `Column` inside `PullToRefreshBox`, so pull-to-refresh still works. The banner is still shown. |
| **One section empty** | That section's header plus its inline empty text (§2.3, §2.4). |
| **Light theme** | Same color roles. `LightColorScheme` / `LightExtendedColorScheme` resolve them. The glows keep `extendedColors.glow*`. |
| **Medium / expanded** | Rail navigation, 24 dp margins, Latest in 2 / 3 columns, carousel unchanged. |

---

## 3. Design vs. spec

| # | Item | Design | docs/context.md | Recommendation |
|---|---|---|---|---|
| 1 | Ratings | "★ 9.8 / 10" on Latest rows | Not listed. Not in the API. | **Omit.** No data source. |
| 2 | Genres | "Supernatural, Sci-Fi, …" under Upcoming titles | Not listed. Not in the API. | **Omit.** Give the title 2 lines (`minLines = 2`) to keep card height. |
| 3 | Episode titles | "Gyomei's Trial", "It Would Be Embarrassing" | Not listed. Not in the API. | **Omit.** |
| 4 | Finale marker | "EP 28 (Finale)" | Episode number only. The API has no episode total. | **Omit "(Finale)"**. Show "EP 28". |
| 5 | Special-episode style | "SP 01" in tertiary | Episode string "as-is: can be a batch range or Movie" | **Keep the style** for non-numeric episodes: "BATCH 01-24" and "MOVIE" use the tertiary badge. |
| 6 | Bookmark toggle on cards | `bookmark_add` / `bookmark` button on every card | §3.1 item fields don't include it. The Bookmarks feature doesn't exist yet. | **Omit in this plan.** Revisit after the Bookmarks plan ([Q7](#10-open-questions)). |
| 7 | "Timeline >" link | On the Upcoming header | Not in spec | **Omit.** The Schedule plan can turn it into "open Schedule tab" ([Q10](#10-open-questions)). |
| 8 | Filter button | On the Latest header | Not in spec | **Omit.** |
| 9 | "Broadcast Alerts Active / Manage" | Bar at the bottom | No notifications. The app is a read-only client with no background work (api.md §7). | **Omit.** |
| 10 | Time zone | Times as "18:30 JST", banner "JST (UTC+9)" | "today (in the device's time zone)" | **Device zone everywhere.** Times have no zone suffix. The banner shows the device offset ("UTC-5"). |
| 11 | Time format | 24-hour ("18:30") | 12-hour default (*Time format* setting, §3.6 / §4.5) | **12-hour** ("6:30 PM") until the Settings plan wires the setting ([Q9](#10-open-questions)). |
| 12 | Countdown chips | "In 42m", "In 2h 15m", with urgency colors | Item shows the scheduled time only | **Omit in v1.** It isn't in the spec and needs a ticking clock ([Q8](#10-open-questions)). |
| 13 | Relative release time | "Aired • Just Now", "2h 45m ago" | Item shows the release time | **Omit in v1.** Show the absolute time on the thumbnail. `release_date` is currently 1 h early ([§4](#4-data) G3), so "ago" values would be wrong ([Q8](#10-open-questions)). |
| 14 | "Today's Broadcasts" banner | Title, zone, "9 Airing" / "5 Available" | Not in spec ("No date is shown") | **Keep.** It shows no date, and the counts come straight from the two sections (`upcoming.size`, `released.size`). |
| 15 | Section titles | "Upcoming Today", "Latest Releases" | "Upcoming", "Latest" | **Use the design's titles.** This is visual only. |
| 16 | Latest sort order | Newest first (14:00 → 11:30 → 09:00) | "Sort both sections by time, ascending" | **Ascending (spec).** Behavior follows the spec ([Q3](#10-open-questions) offers newest-first). |
| 17 | Bottom navigation | 4 working tabs | 4 tabs, but only Home exists after this plan | **Show all 4; disable the 3 unbuilt ones** ([Q4](#10-open-questions)). |
| 18 | App-bar subtitle | "HOME" under "BakaMitai" | Title "BakaMitai" + gear | **Keep the subtitle.** It's visual only and fits the spec. |
| 19 | Settings gear | Always visible | Opens Settings, which isn't built | **Hide it until Settings exists** ([Q5](#10-open-questions)). |
| 20 | Card corner radius | `rounded-lg` = 32 px in the HTML | n/a. DESIGN.md says poster cards are 24 px. | **24 dp (`shapes.large`)** per DESIGN.md and `Shape.kt`. The banner keeps 32 dp (`shapes.extraLarge`). |
| 21 | Bottom bar shape | HTML: docked, full width. DESIGN.md: floating pill 16 px above the safe area. | `NavigationSuiteScaffold` | **Docked M3 default** (matches the exported screen). |
| 22 | Blur and drop shadows | `backdrop-blur`, `shadow-md/xl` | n/a | **Solid tonal layers, no shadows.** This follows DESIGN.md "Elevation & Depth". |
| 23 | Loading / error / empty / refresh / light states | Not in design | Required (§3) | **Proposed in [§2.7](#27-states-not-in-the-design-all-proposed).** |

---

## 4. Data

### 4.1 Endpoints

Every call goes through the shared `HttpClient` (`common/data/network/HttpClientFactory.kt`). That client already handles the `text/html` content type ([api.md §1](api.md#1-basics)), the User-Agent and `expectSuccess`. **Every call sends `tz=<TimeZone.id>`.**

| Call | Request | Fields used | Quirks to handle |
|---|---|---|---|
| Today's schedule, [api.md §3.1](api.md#31-todays-schedule) | `GET api/?f=schedule&h=true&tz=` | `title`, `page`, `image_url`, `time`, `aired` | `time` is `"HH:mm"` in the requested zone. Sorted by time, but sort anyway. Items missing `page`, `title` or a parseable `time` are dropped. |
| Latest releases, [api.md §3.3](api.md#33-latest-releases) | `GET api/?f=latest&tz=&p=1`, plus `p=2` per [§4.3](#43-derivation-domain) | `show`, `page`, `episode`, `release_date`, `image_url` | The root is an object keyed `"<show> - <ep>"`: use the values. `{"error":"limit_reached"}` means an empty page. The `time` label is never used. `downloads` and `xdcc` are ignored. |
| Search releases, [api.md §3.4](api.md#34-search-releases) | `GET api/?f=search&tz=&s=<query>`. `s` is a query parameter that Ktor encodes. **`s=` (empty)** is used for the newest releases (G2). | Same as latest | `[]` means no results. Results for one title can include other shows: filter by `page`. |

Quirks common to all three calls:

- `release_date` is RFC 1123 with **any** offset. Always parse it to `Instant`.
- `episode` is a string: `"07"`, `"01-24"`, `"Movie"`.
- `image_url` is either relative (prefix `https://subsplease.org`) or the placeholder `/wp-includes/images/image-coming-soon-placeholder.png`, which maps to `null`.

### 4.2 API gaps found live

These checks ran on 2026-11-18, 15:20–15:36 UTC, with `tz=America/New_York`, `Asia/Tokyo` and `Europe/London`. The last step of [§8](#8-implementation-steps) writes them into `api.md`.

- **G1. `f=latest` lags by about 3 days.**
  - At 15:21 UTC, page 1 started at *Sun 15 Nov 10:03 -0500*. At 15:36 it had advanced only to *Sun 15 Nov 10:22 -0500*.
  - Meanwhile, releases up to *Wed 18 Nov 09:33 -0500* existed.
  - The website's homepage uses the same call (`/includes/main.js`), so it's stale too. Page 3 still returns `limit_reached`.
  - **Impact:** following context.md §4.1 literally, Latest would be empty all day today.
  - **Re-check, ~15:40 UTC the same day:** `f=latest` page 1 and `f=search&s=` returned the same 20 items (newest *Wed 18 Nov 16:33 +0200*). So the lag is intermittent, not permanent. The merge in Q2 stays as a safety net.
- **G2. `f=search&s=` (empty query) returns the 20 newest releases, fresh.**
  - It has the same shape as `f=latest`. `p` is ignored.
  - The website calls it when the search box is cleared.
  - **Used as a second "latest" source** ([Q2](#10-open-questions)).
- **G3. `release_date` instants are 1 hour early, currently.**
  - The test show was *Tensei Shitara Ken Deshita S2*, checked against the nyaa.si upload time of its torrents:

    | Episode | API instant | nyaa upload |
    |---|---|---|
    | ep 08 (18 Nov) | 14:33:47Z | 15:33:48Z |
    | ep 07 (11 Nov) | 14:32:41Z | 15:32Z |
    | ep 04 (21 Oct) | 15:34:23Z | 15:34Z (correct) |

  - So the hour has been missing since European DST ended (25 Oct).
  - The schedule `time` (10:30 New York = 15:30Z) matches the real release. The real releases of the other 5 shows today also line up with their scheduled times.
  - `api.md` §3.5 claims "the instant is correct". That's wrong in winter.
  - **Plan:** show `release_date` as-is. The app can't tell when the server fixes it ([Q6](#10-open-questions)).
  - **Impact:**
    - Latest times look 1 h early.
    - A release in the first local hour after midnight is counted as yesterday.
- **G4. `release_date` offsets vary per endpoint and per item.**
  - Some use the requested zone (`-0500`). Others use the server zone, which changes: `+0100`, `+0200` and `-0300` were all seen today for the same show.
  - Parse to `Instant`. Never trust the offset.
- **G5. `time` labels differ.**
  - `f=latest` and `s=` use `"New"` / `"Nov 17th"`. Non-empty searches use `"New"` / `"MM/dd/yy"`.
  - Never parse them.
- **G6. Without `tz`, `f=latest` also returns `200` with an empty body**, like `f=show`. We always send `tz`. An empty body decodes to `DomainError.InvalidData`.
- **G7. What `aired` means is unverified.**
  - *Tensei S2* (scheduled 10:30 New York) was `aired=false` at 15:26Z.
  - It was `aired=true` at 15:33:39Z, when its release was also already listed.
  - So it could follow the release or the clock. That's why [§4.3](#43-derivation-domain) also removes already-released shows from Upcoming.
  - If it follows the clock, a **delayed** show drops out of Upcoming before it appears in Latest.
- **G8. `page` may be missing.** The website's script guards with `if(val.page)`. It wasn't seen today. Items without `page` are dropped, because the slug is the app's show ID.
- **G9. No HTML entities or `\u` escapes in JSON titles** (schedule, latest and search checked). No entity decoding is needed for the JSON endpoints.

### 4.3 Derivation (domain)

This lives in `GetTodayBroadcastsUseCase`. It replaces context.md §4.1 steps 1–4, pending [Q2](#10-open-questions) and [Q6](#10-open-questions).

1. `tz = timeZoneProvider.current()`, `today = clock.todayIn(tz)`, `utcOffset = tz.offsetAt(clock.now())`.
2. **In parallel** (`coroutineScope { async … }`):
   - `schedule = getTodaySchedule(tz)` (**required**: failure fails the whole result)
   - `page1 = getLatestReleases(page = 1, tz)` (**required**)
   - `newest = searchReleases(query = "", tz)` (**best effort**: failure counts as empty, G2)
3. If `page1` isn't empty **and** every `page1` item is dated `today` in `tz`:
   - fetch `page2 = getLatestReleases(page = 2, tz)` (best effort)
   - a `limit_reached` page counts as empty
4. `known = (page1 + newest + page2)`, sorted by `releasedAt` descending, `distinctBy { slug to episode }`.
5. **Released** = `known` items whose `releasedAt.toLocalDateTime(tz).date == today`, sorted by `releasedAt` **ascending** (spec; [Q3](#10-open-questions)).
   - Each release is listed separately, so the same show can appear twice.
6. **Upcoming** = `schedule` items with `aired != true` **whose slug isn't in Released** (safety net for G7, [Q6](#10-open-questions)).
   - Sorted by `time` ascending, then by `title`.
7. **Next episode** for each Upcoming item, run in parallel through `FindLatestEpisodeNumberUseCase`:
   1. Take the highest `episode.toIntOrNull()` among `known` items with the same slug. `"01-24"`, `"Movie"` and `"12.5"` count as non-numeric.
   2. If there's none, call `searchReleases(show.title, tz)`, keep results whose slug matches, and take the highest numeric episode.
   3. A search failure gives `null`.
   - `episodeNumber = latest + 1`. If no number was found, it's `null` and the badge is hidden. Never guess.
8. Return `TodayBroadcasts(date = today, timeZone = tz, utcOffset, upcoming, released)`.

### 4.4 Local data

None: no DataStore. The *Time format* setting doesn't exist yet, so times use the 12-hour pattern from [context.md §4.5](context.md#45-formatting-rules-all-screens).

### 4.5 Caching and refresh

[api.md §7](api.md#7-client-guidelines) lists no in-memory cache for Home, and none is added.

- **When Home fetches:**
  - once per `HomeViewModel` (`init { onIntent(Load) }`)
  - on pull-to-refresh
  - on `ScreenStarted` when the local date has changed since the last successful load
- **When it doesn't fetch:**
  - Switching tabs doesn't refetch. `NavigationState` keeps each entry's ViewModel store.
  - Rotation doesn't refetch.
  - No polling, no timers.
- Process death refetches.

---

## 5. Architecture

Root: `app/src/main/java/com/zc/bakamitai/`. Test root: `app/src/test/java/com/zc/bakamitai/`.

### common/ (shared pieces this feature needs)

| Path | | Responsibility |
|---|---|---|
| `common/di/CommonModule.kt` | modify | Add `@Single fun clock(): Clock = Clock.System`, using **`kotlin.time.Clock`**. kotlinx-datetime 0.8 deprecated `kotlinx.datetime.Clock`. Opt in to `kotlin.time.ExperimentalTime` on the smallest declaration only if the compiler asks. |
| `common/domain/util/TimeZoneProvider.kt` | new | `interface TimeZoneProvider { fun current(): TimeZone }` + `@Single class SystemTimeZoneProvider` (`TimeZone.currentSystemDefault()`). It's injected like `DispatcherProvider`, so tests can fix the zone and a zone change is picked up on the next load. It needs a `structure` §2 row (step 1). |
| `common/ui/resource/ResourceTypes.kt` | modify | Add `typealias PluralResource = com.zc.bakamitai.R.plurals` (the first `<plurals>` arrives with this feature), and update the file comment. |
| `res/values/strings.xml` | modify | Strings and plurals listed in step 11. |

### feature/home/data

| Path | | Responsibility |
|---|---|---|
| `feature/home/data/remote/BroadcastService.kt` | new | `@Single`. Holds `suspend fun getTodaySchedule(timeZoneId: String): TodayScheduleDTO`, `getLatestReleases(timeZoneId: String, page: Int): ReleaseListDTO` and `searchReleases(timeZoneId: String, query: String): ReleaseListDTO`. Each is `client.get("api/") { parameter("f", …); parameter("tz", …); … }.body()`. No mapping. |
| `feature/home/data/dto/TodayScheduleDTO.kt` | new | `@SerialName("schedule") val schedule: List<ScheduleItemDTO>?` |
| `feature/home/data/dto/ScheduleItemDTO.kt` | new | `title`, `page`, `image_url`, `time` (all `String?`), and `aired: Boolean?` |
| `feature/home/data/dto/ReleaseDTO.kt` | new | `time`, `release_date`, `show`, `episode`, `image_url`, `page`, all `String?`. `downloads` and `xdcc` aren't declared, because Home doesn't use them. |
| `feature/home/data/dto/ReleaseListDTO.kt` | new | `@Serializable(with = ReleaseListDTOSerializer::class) data class ReleaseListDTO(val releases: List<ReleaseDTO>, val isLimitReached: Boolean)`. A non-public, decode-only serializer object in the same file handles the polymorphic root (snippet below). |
| `feature/home/data/mapper/ScheduleItemDTOMapper.kt` | new | `fun ScheduleItemDTO.toDomain(): ScheduledShow?` and `fun TodayScheduleDTO.toDomain(): List<ScheduledShow>`. Uses `LocalTime.Formats.ISO.parseOrNull(time)`. |
| `feature/home/data/mapper/ReleaseDTOMapper.kt` | new | `fun ReleaseDTO.toDomain(): Release?` and `fun ReleaseListDTO.toDomain(): List<Release>` (`mapNotNull`). `releasedAt` comes from `DateTimeComponents.Formats.RFC_1123.parseOrNull(raw)?.takeIf { it.offsetHours != null }?.toInstantUsingOffset()`. The built-in format accepts `"07 Jul"`, because its day field is `Padding.NONE`, and `+HHMM` offsets. |
| `feature/home/data/mapper/ImageUrlDTOMapper.kt` | new | `fun String?.toCoverUrl(): String?`. Null or blank → `null`. The placeholder path → `null`. A value starting with `http` is returned as-is. Otherwise `SUBSPLEASE_BASE_URL.trimEnd('/') + path`. |
| `feature/home/data/repository/BroadcastRepositoryImpl.kt` | new | `@Single`, implements `BroadcastRepository`. Each method is `safeApiCall { service.x(timeZone.id, …).toDomain() }`. |

Decode-only root serializer. This is the one tricky detail:

```kotlin
// in ReleaseListDTO.kt
override fun deserialize(decoder: Decoder): ReleaseListDTO {
    val input = decoder as? JsonDecoder ?: throw SerializationException("ReleaseListDTO is JSON-only")
    return when (val root = input.decodeJsonElement()) {
        is JsonArray -> ReleaseListDTO(emptyList(), isLimitReached = false)               // f=search, no results
        is JsonObject -> if ("error" in root) ReleaseListDTO(emptyList(), isLimitReached = true) // {"error":"limit_reached"}
            else ReleaseListDTO(root.values.map { input.json.decodeFromJsonElement(ReleaseDTO.serializer(), it) }, false)
        else -> throw SerializationException("Unexpected root: $root")
    }
}
```

### feature/home/domain (pure Kotlin + kotlinx-datetime)

| Path | | Responsibility |
|---|---|---|
| `domain/model/ShowSlug.kt` | new | `@JvmInline value class ShowSlug(val value: String)`: the show ID (`page`) |
| `domain/model/Show.kt` | new | `data class Show(val slug: ShowSlug, val title: String, val coverUrl: String?)` |
| `domain/model/ScheduledShow.kt` | new | `data class ScheduledShow(val show: Show, val time: LocalTime, val aired: Boolean?)`. This is context.md §6's shape, so it can move to `common/` unchanged. |
| `domain/model/Release.kt` | new | `data class Release(val show: Show, val episode: String, val releasedAt: Instant)`, with `kotlin.time.Instant`. `downloads` is left out, because Home doesn't use it. |
| `domain/model/UpcomingBroadcast.kt` | new | `data class UpcomingBroadcast(val show: Show, val time: LocalTime, val episodeNumber: Int?)` |
| `domain/model/TodayBroadcasts.kt` | new | `data class TodayBroadcasts(val date: LocalDate, val timeZone: TimeZone, val utcOffset: UtcOffset, val upcoming: List<UpcomingBroadcast>, val released: List<Release>)` |
| `domain/repository/BroadcastRepository.kt` | new | `suspend fun getTodaySchedule(timeZone: TimeZone): Result<List<ScheduledShow>>`, `getLatestReleases(page: Int, timeZone: TimeZone): Result<List<Release>>` (a `limit_reached` page is an empty list), `searchReleases(query: String, timeZone: TimeZone): Result<List<Release>>` |
| `domain/usecase/GetTodayBroadcastsUseCase.kt` | new | `@Factory`. `suspend operator fun invoke(): Result<TodayBroadcasts>` implements [§4.3](#43-derivation-domain). Dependencies: `BroadcastRepository`, `FindLatestEpisodeNumberUseCase`, `Clock`, `TimeZoneProvider`. |
| `domain/usecase/FindLatestEpisodeNumberUseCase.kt` | new | `@Factory`. `suspend operator fun invoke(show: Show, knownReleases: List<Release>, timeZone: TimeZone): Int?` implements step 7 of §4.3. |
| `domain/usecase/GetTodayDateUseCase.kt` | new | `@Factory`. `operator fun invoke(): LocalDate = clock.todayIn(timeZoneProvider.current())`. It's pure and can't fail, so it returns the value directly. The ViewModel uses it for the new-day check, because a ViewModel may only depend on use cases. |

### feature/home/presentation

The episode badge and the cover image are shared sub-components used by two sibling components (`upcomingcard`, `releaserow`). Per `structure` §1.1 they sit at the nearest shared level, `presentation/component/`. Their model is in `presentation/model/`, because it's rendered inside two components.

| Path | | Responsibility |
|---|---|---|
| `presentation/model/EpisodeBadgeUIModel.kt` | new | `@Immutable data class EpisodeBadgeUIModel(val label: UiText, val contentDescription: UiText, val isSpecial: Boolean)` |
| `presentation/mapper/EpisodeBadgeUIModelMapper.kt` | new | `fun Release.toEpisodeBadgeUIModel(): EpisodeBadgeUIModel`, mapping all-digits / contains `-` / equals `Movie` (ignoring case) / other to `home_episode` / `home_episode_batch` / `home_episode_movie` / raw text. `fun UpcomingBroadcast.toEpisodeBadgeUIModel(): EpisodeBadgeUIModel?` gives `null` when `episodeNumber == null`, otherwise "EP " + `episodeNumber.toString().padStart(2, '0')`. |
| `presentation/mapper/TimeUIModelMapper.kt` | new | `fun LocalTime.toTimeLabel(): UiText` and `fun Instant.toTimeLabel(timeZone: TimeZone): UiText`, both as `UiText.StringValue`, using a `private val TwelveHourFormat = LocalTime.Format { amPmHour(Padding.NONE); char(':'); minute(); char(' '); amPmMarker("AM", "PM") }` (context.md §4.5). This moves to `common/ui/mapper/` with the Schedule plan. |
| `presentation/mapper/TodayBroadcastsUIModelMapper.kt` | new | `fun TodayBroadcasts.toUpcomingCountLabel(): UiText` (plural `home_section_upcoming_count`) |
| `presentation/component/todaysummary/model/TodaySummaryUIModel.kt` | new | `@Immutable data class TodaySummaryUIModel(val utcOffsetLabel: UiText, val upcomingCount: UiText, val releasedCount: UiText)` |
| `presentation/component/todaysummary/mapper/TodaySummaryUIModelMapper.kt` | new | `fun TodayBroadcasts.toTodaySummaryUIModel(): TodaySummaryUIModel`. The offset suffix is `""` at 0, otherwise `+H`, `-H` or `+H:MM` (`UiText.StringResource(home_utc_offset, listOf(suffix))`). |
| `presentation/component/todaysummary/TodaySummaryView.kt` | new | The banner (§2.2), plus `@AdaptivePreviews` |
| `presentation/component/upcomingcard/model/UpcomingCardUIModel.kt` | new | `@Immutable data class UpcomingCardUIModel(val key: String, val slug: String, val title: UiText, val coverUrl: String?, val timeLabel: UiText, val episodeBadge: EpisodeBadgeUIModel?)`. `key = "${slug}@${time}"`. |
| `presentation/component/upcomingcard/mapper/UpcomingCardUIModelMapper.kt` | new | `fun UpcomingBroadcast.toUIModel(): UpcomingCardUIModel` |
| `presentation/component/upcomingcard/UpcomingCardView.kt` | new | `UpcomingCardView(model, onClick: (() -> Unit)?, modifier)`. `null` means not clickable. With `@AdaptivePreviews`. |
| `presentation/component/releaserow/model/ReleaseRowUIModel.kt` | new | `@Immutable data class ReleaseRowUIModel(val key: String, val slug: String, val title: UiText, val coverUrl: String?, val timeLabel: UiText, val episodeBadge: EpisodeBadgeUIModel)`. `key = "${slug}#${episode}"`. |
| `presentation/component/releaserow/mapper/ReleaseRowUIModelMapper.kt` | new | `fun Release.toUIModel(timeZone: TimeZone): ReleaseRowUIModel` |
| `presentation/component/releaserow/ReleaseRowView.kt` | new | `ReleaseRowView(model, onClick: (() -> Unit)?, modifier)`, with `@AdaptivePreviews` |
| `presentation/component/episodebadge/EpisodeBadgeView.kt` | new | `EpisodeBadgeView(model: EpisodeBadgeUIModel, modifier)` (§2.5), with `@AdaptivePreviews` |
| `presentation/component/coverimage/CoverImageView.kt` | new | `CoverImageView(url: String?, modifier)`. `AsyncImage` with `ContentScale.Crop`, `contentDescription = null`, and a `ColorPainter(surfaceContainerHighest)` for `placeholder`, `error` and `fallback`. The caller sets the size and shape. With `@AdaptivePreviews`. It moves to `common/ui/component/coverimage/` with the Schedule plan (`best-practices` §1.6). |
| `presentation/HomeState.kt` | new | MVI state (below) |
| `presentation/HomeIntent.kt` | new | Intents (below) |
| `presentation/HomeEffect.kt` | new | Effects (below) |
| `presentation/HomeViewModel.kt` | new | `@KoinViewModel class HomeViewModel(getTodayBroadcasts: GetTodayBroadcastsUseCase, getTodayDate: GetTodayDateUseCase)`. It owns `loadJob` (concurrent loads are ignored), keeps `loadedDate: LocalDate?` private, maps domain to UI models, and reduces with `_state.update`. |
| `presentation/HomeRoute.kt` | new | `HomeRoute(onOpenDetails: ((slug: String) -> Unit)?, onOpenSettings: (() -> Unit)?, modifier, viewModel = koinViewModel())`. Collects state with `collectAsStateWithLifecycle()` and effects with `ObserveAsEvents`. Runs `LifecycleStartEffect(viewModel) { viewModel.onIntent(HomeIntent.ScreenStarted); onStopOrDispose {} }`. Owns the `SnackbarHostState`. Effects: `OpenDetails` → `onOpenDetails?.invoke(slug)`; `OpenSettings` → `onOpenSettings?.invoke()`; `ShowError` → `snackbarHostState.showSnackbar(effect.error.message.asString(context))`. Passes `canOpenShow = onOpenDetails != null` and `showSettingsAction = onOpenSettings != null` to the Screen. |
| `presentation/HomeScreen.kt` | new | `HomeScreen(state, onIntent, modifier, canOpenShow: Boolean = true, showSettingsAction: Boolean = true, snackbarHostState = remember { SnackbarHostState() })`. Private: `HomeTopAppBar`, `HomeContent`, `SectionHeader`, `UpcomingCarousel`, `HomeEmptyView`. Has `@AdaptivePreviews` with a `PreviewParameterProvider<HomeState>`. |

`HomeScreen` layout: the content is **one `LazyVerticalGrid`** inside `PullToRefreshBox(isRefreshing = state.isRefreshing, onRefresh = { onIntent(HomeIntent.Refresh) })`, which is inside a `Scaffold` that has `Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)` and `snackbarHost = { SnackbarHost(snackbarHostState) }`. The skeleton (`HomeSkeleton`) and `ErrorView` replace the box entirely.

- `columns = GridCells.Fixed(latestColumns)`. `latestColumns` is 1 / 2 / 3, read from `currentWindowAdaptiveInfo().windowSizeClass` with `isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND / WIDTH_DP_EXPANDED_LOWER_BOUND)`.
- `contentPadding`: horizontal `margin` (or `marginTablet`), vertical `spacing.sm`.
- `verticalArrangement = spacedBy(spacing.md)` and `horizontalArrangement = spacedBy(spacing.gutter)`.
- Full-span items (`span = { GridItemSpan(maxLineSpan) }`), in order: `summary`, `upcoming-header`, `upcoming-row` (or `upcoming-empty`), `latest-header`, then `latest-empty` when needed.
  - Both headers get an extra `spacing.md` top padding. Added to the `spacedBy(spacing.md)` gap, that puts major sections `spacing.xl` (32 dp) apart, per DESIGN.md's vertical rhythm.
- Release cells: `items(state.latest, key = { it.key }, contentType = { "release" })`.
- Every item passes a `contentType` (`"summary"`, `"header"`, `"carousel"`, `"empty"`, `"release"`).

The carousel item has to escape the grid's horizontal padding. That's done with a private breakout modifier:

```kotlin
private fun Modifier.breakOutOfHorizontalPadding(padding: Dp) = layout { measurable, constraints ->
    val extra = (padding * 2).roundToPx()
    val placeable = measurable.measure(constraints.copy(minWidth = constraints.maxWidth + extra, maxWidth = constraints.maxWidth + extra))
    layout(constraints.maxWidth, placeable.height) { placeable.place(-extra / 2, 0) }
}
```

Click lambdas are adapted once: `val onShowClick = remember(onIntent) { { slug: String -> onIntent(HomeIntent.ShowClicked(slug)) } }`. Cards get `onClick = if (canOpenShow) { { onShowClick(model.slug) } } else null`.

Insets: `NavigationSuiteScaffold` already pads for the navigation bar. If the last row ends up padded twice, pass `contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)` to the Home `Scaffold`. Check this on an emulator.

### Navigation

| Path | | Responsibility |
|---|---|---|
| `feature/home/presentation/navigation/HomeNavKey.kt` | new | `@Serializable data object HomeNavKey : NavKey`. It's a plain top-level page, not `FullScreenNavKey` or `OverlayNavKey`. |
| `feature/home/presentation/navigation/HomeNavigation.kt` | new | `fun EntryProviderScope<NavKey>.homeEntry(onOpenDetails: ((slug: String) -> Unit)?, onOpenSettings: (() -> Unit)?)` → `entry<HomeNavKey> { HomeRoute(onOpenDetails, onOpenSettings) }`. There's no `Navigator.navigateToHome()`, because tabs are selected through `TopLevelDestination` and no feature opens Home. |
| `app/navigation/TopLevelDestination.kt` | new | `enum class TopLevelDestination(val key: NavKey?, val selectedIcon: ImageVector, val unselectedIcon: ImageVector, val label: UiText)` with `HOME(HomeNavKey, …)`, `SCHEDULE(null, …)`, `ALL_SHOWS(null, …)`, `BOOKMARKS(null, …)`. `key == null` means the destination isn't built yet and is shown disabled. Each later plan fills in its key. |
| `app/navigation/AppEntryProvider.kt` | new | `fun appEntryProvider(): (NavKey) -> NavEntry<NavKey> = entryProvider { homeEntry(onOpenDetails = null, onOpenSettings = null) }`. Later plans add a `navigator` parameter and pass `navigator::navigateToDetails` and the like. |
| `app/navigation/AppNavigation.kt` | new | `@Composable fun AppNavigation(modifier: Modifier = Modifier)`. Builds `rememberNavigationState(startRoute = HomeNavKey, topLevelRoutes = TopLevelDestination.entries.mapNotNull { it.key }.toSet())` and `val navigator = remember(state) { Navigator(state) }`. Renders `NavigationSuiteScaffold` (items: `enabled = key != null`, `selected = key == state.topLevelRoute`, `onClick = { key?.let(navigator::navigate) }`, colors per §2.1), then `NavDisplay(entries = state.toDecoratedEntries(appEntryProvider()), onBack = navigator::goBack)`. Remember the entry provider. |
| `app/AppRoute.kt` | modify | Replace the wordmark `Box` with `AppNavigation()` inside `BakaMitaiTheme`, and update the KDoc. |

### di

| Path | | Responsibility |
|---|---|---|
| `feature/home/di/HomeModule.kt` | new | `@Module @ComponentScan("com.zc.bakamitai.feature.home") class HomeModule` |
| `app/di/AppModule.kt` | modify | `@Module(includes = [CommonModule::class, HomeModule::class])` |

### MVI contract

```kotlin
@Immutable
data class HomeState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: ErrorUIModel? = null,                       // full-screen error (no content yet)
    val summary: TodaySummaryUIModel? = null,              // non-null once a load succeeded
    val upcomingCountLabel: UiText? = null,                // "9 shows"
    val upcoming: ImmutableList<UpcomingCardUIModel> = persistentListOf(),
    val latest: ImmutableList<ReleaseRowUIModel> = persistentListOf(),
) {
    val hasContent: Boolean get() = summary != null
    val isEmpty: Boolean get() = hasContent && upcoming.isEmpty() && latest.isEmpty()
}

sealed interface HomeIntent {
    data object Load : HomeIntent                          // from init
    data object Retry : HomeIntent                         // ErrorView button
    data object Refresh : HomeIntent                       // pull-to-refresh
    data object ScreenStarted : HomeIntent                 // lifecycle START: reload if the local date changed
    data class ShowClicked(val slug: String) : HomeIntent
    data object SettingsClicked : HomeIntent
}

sealed interface HomeEffect {
    data class OpenDetails(val slug: String) : HomeEffect
    data object OpenSettings : HomeEffect
    data class ShowError(val error: ErrorUIModel) : HomeEffect   // refresh failed while content is shown
}
```

**Reducer rules:**

- **`Load` / `Retry`:**
  - Does nothing if `loadJob?.isActive`.
  - Otherwise `isLoading = !hasContent`, `isRefreshing = hasContent`, `error = null`.
- **Success:**
  - All flags off.
  - `summary`, `upcomingCountLabel`, `upcoming` and `latest` are mapped, the latter two with `.toImmutableList()`.
  - `loadedDate = broadcasts.date`.
- **Failure without content:** `error = throwable.toErrorUIModel()`.
- **Failure with content:** flags off, then the `ShowError` effect.
- **`Refresh`:** same as `Load`, with `isRefreshing = true`.
- **`ScreenStarted`:** if `loadedDate != null`, no load is running, and `getTodayDate() != loadedDate`, run `Refresh`.
- **`ShowClicked` / `SettingsClicked`:** send the matching effect.

---

## 6. Dependencies

### Libraries

Everything is already in `libs.versions.toml` and wired into `:app`, **except icons**:

- **Icons.** `material3` 1.4.0 no longer brings `material-icons-core`, and `debugRuntimeClasspath` has no `material-icons-*` at all. So `Icons.*` doesn't compile today.
  - The design needs Home, CalendarMonth, ViewList, Bookmark/BookmarkBorder, Settings, Schedule, Alarm and Movie, which come from **`androidx.compose.material:material-icons-extended`**.
  - That artifact is managed by Compose BOM 2026.09.00 at **1.7.8**, so it needs no version entry.
  - It isn't in `tech-stack` §1 or the `library-management` artifact map yet. **Approved by the user on 2026-11-18** ([Q1](#10-open-questions)), so it is decided.
  - Step 11 adds:
    - catalog entry: `androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }`
    - `implementation(libs.androidx.compose.material.icons.extended)`
    - the `tech-stack` §1 row and the `library-management` §2 row
- **Opt-ins:**
  - `PullToRefreshBox` and `TopAppBar` may need `@OptIn(ExperimentalMaterial3Api::class)`.
  - `kotlin.time.Clock` and `Instant` may need `ExperimentalTime`.
  - Opt in on the smallest declaration. That's not a suppression.
- **Already present:** `LifecycleStartEffect` (lifecycle-runtime-compose 2.11), `NavigationSuiteScaffold` (material3-adaptive-navigation-suite 1.4.0), Navigation 3 1.2.1 and `lifecycle-viewmodel-navigation3`.

### Earlier plans and shared code

- **Needs:** nothing beyond what already exists in `common/`: `HttpClient`, `safeApiCall`, `DomainError`, `ErrorView`, `SkeletonView`, `UiText`, `ObserveAsEvents`, `@AdaptivePreviews`, `Navigator` and `NavigationState`.
- **Creates for everyone:** the `Clock` provider, `TimeZoneProvider`, `PluralResource`, and the app navigation shell (`AppNavigation`, `TopLevelDestination`, `AppEntryProvider`).

### What it unblocks, and what later plans must move

- **Schedule plan (second user of the shared pieces):** under the `structure` §3 rule, it moves these to `common/`:
  - `Show`, `ShowSlug`, `ScheduledShow`, `ScheduleItemDTO` (+ mapper), `toCoverUrl`
  - `TimeUIModelMapper`, which becomes the setting-driven time formatter
  - `CoverImageView`
  - `HomeTopAppBar`, which becomes `common/ui/component/toplevelappbar/`
  
  It also sets `TopLevelDestination.SCHEDULE.key`.
- **Details plan:**
  - Passes `onOpenDetails = navigator::navigateToDetails` in `AppEntryProvider`.
  - Context.md §3.5 says callers may pass the title and cover, but `conventions` §5.1 says NavKeys carry ids only. Home passes only the slug.
  - `Release` gains `downloads` when Details or another feature needs them.
- **Settings plan:** passes `onOpenSettings` and replaces the 12-hour constant with the setting.
- **All Shows and Bookmarks plans:** set their `TopLevelDestination` keys. The Bookmarks plan may add card bookmark toggles ([Q7](#10-open-questions)).

---

## 7. Testing

Unit tests mirror the main package under `app/src/test/java/com/zc/bakamitai/`. Tools: JUnit4, `kotlin.test`, `runTest`, Turbine, Ktor `MockEngine` through `createHttpClient(appJson, "test", engine)`. Fakes are hand-written.

### Fixtures

Fixtures are trimmed real responses in `app/src/test/resources/subsplease/`, captured with a few `curl` calls that include `tz=America/New_York`:

| File | Source | Must contain |
|---|---|---|
| `schedule_today.json` | `f=schedule&h=true` | at least 2 `aired:true` and 2 `aired:false` items |
| `latest_page1.json` | `f=latest&p=1` | about 4 entries, including a batch (`"25-48"`) and one with the placeholder `image_url` |
| `latest_limit_reached.json` | | `{"error":"limit_reached"}` |
| `search_newest.json` | `f=search&s=` | about 4 entries spanning two local dates |
| `search_show.json` | `s=Sasaki to Pii-chan` | `sasaki-to-pii-chan-s2` episodes **plus** the `sasaki-to-pii-chan` batch `"01-12"`, plus one `"Movie"` entry taken from `s=Movie` |
| `search_no_results.json` | | `[]` |

### Test classes

| Class | Covers |
|---|---|
| `common/domain/util/FixedTimeZoneProvider.kt`, `common/util/FixedClock.kt` | **Fakes (test sources).** A fixed zone, and a settable `Clock` for the new-day test. |
| `feature/home/domain/repository/FakeBroadcastRepository.kt` | **Fake.** Per-call `Result`s, a log of calls (to assert which pages and queries were requested), and an optional failure per method. |
| `feature/home/data/remote/BroadcastServiceTest` | Each method sends `GET /api/` with the exact `f`, `h`, `tz`, `p` and `s` query parameters (`s` decoded equals the title; the empty query is sent as `s=`). Each method decodes its fixture. |
| `feature/home/data/dto/ReleaseListDTOTest` | Object root becomes a list of values. `[]` becomes an empty list with `isLimitReached = false`. `{"error":"limit_reached"}` becomes empty with `isLimitReached = true`. Unknown keys (`downloads`, `xdcc`, `time`) are ignored. A number root throws `SerializationException`. |
| `feature/home/data/mapper/ScheduleItemDTOMapperTest` | `"08:00"` → `LocalTime(8, 0)`. `"8pm"` and a null time are dropped. A null `page` is dropped (G8). `aired` is carried. Covers go through `toCoverUrl`. |
| `feature/home/data/mapper/ReleaseDTOMapperTest` | `"Sun, 15 Nov 2026 10:03:53 -0500"`, `"Tue, 07 Jul 2026 19:41:51 +0200"` and `"Wed, 18 Nov 2026 11:33:47 -0300"` all map to the right `Instant` (G4). An unparseable or missing date is dropped. `"Movie"` and `"01-24"` are kept as-is. A missing `page` or `show` is dropped. |
| `feature/home/data/mapper/ImageUrlDTOMapperTest` | Relative → `https://subsplease.org/wp-content/…` (no double slash). The placeholder → `null`. Blank → `null`. Absolute stays unchanged. |
| `feature/home/data/repository/BroadcastRepositoryImplTest` | Through `MockEngine`: 500 → `DomainError.Server`. HTML body → `InvalidData`. **Empty body (G6) → `InvalidData`.** `limit_reached` → `Result.success(emptyList())`. `tz` is `timeZone.id`. |
| `feature/home/domain/usecase/GetTodayBroadcastsUseCaseTest` | See the cases below. |
| `feature/home/domain/usecase/FindLatestEpisodeNumberUseCaseTest` | Picks the highest numeric episode among known releases of the same slug. Ignores `"01-24"`, `"Movie"` and other slugs. Falls back to search when there's no numeric match, and filters by slug (the `sasaki-to-pii-chan` S1 batch is ignored). A search failure → `null`. Nothing numeric → `null`. |
| `feature/home/domain/usecase/GetTodayDateUseCaseTest` | The date depends on the provider's zone for the same instant. |
| `feature/home/presentation/mapper/TimeUIModelMapperTest` | 00:00 → "12:00 AM", 08:05 → "8:05 AM", 12:15 → "12:15 PM", 23:59 → "11:59 PM". `Instant` + zone → local time. |
| `feature/home/presentation/mapper/EpisodeBadgeUIModelMapperTest` | `"07"` → `home_episode` + "07", regular. `"01-24"` → batch, special. `"movie"` → movie, special. `"OVA"` → raw, special. Upcoming 8 → "08", 1221 → "1221", `null` → no badge. Content descriptions included. |
| `feature/home/presentation/component/todaysummary/mapper/TodaySummaryUIModelMapperTest` | Offsets +9h → "+9", −5h → "-5", +5:30 → "+5:30", 0 → "". Plural counts use the list sizes. |
| `…/upcomingcard/mapper/UpcomingCardUIModelMapperTest`, `…/releaserow/mapper/ReleaseRowUIModelMapperTest` | Keys are unique for the same show with two episodes. A null cover passes through. The time label uses the given zone. |
| `feature/home/presentation/HomeViewModelTest` | `MainDispatcherRule`, Turbine, real use cases over `FakeBroadcastRepository` with `FixedClock` / `FixedTimeZoneProvider`. See the cases below. |
| `app/navigation/NavKeySerializationTest` | **New.** Round-trips every `NavKey` (only `HomeNavKey` today) through `appJson` with `NavKeySerializer()`. If that serializer needs a SavedState encoder, use each key's own `serializer()` instead. Later plans add their keys to its sample list. |
| `app/di/KoinModulesTest` | **Modify.** `verify()` still passes with `HomeModule` included. `common bindings are created` also resolves `Clock`, `TimeZoneProvider` and `HomeViewModel`'s use cases. |

`GetTodayBroadcastsUseCaseTest` cases:
- Upcoming = `aired == false` only.
- An upcoming show already in Released is removed (G7).
- Released is filtered by **local** date. Use an instant of 02:30Z on the 19th: it's "today" (the 18th) in `America/New_York` but tomorrow in `Asia/Tokyo`.
- Released is ascending. The same show with two episodes appears twice.
- `page1` and `newest` are merged and deduped (G1, G2).
- When `page1` is stale and `newest` holds today's releases, Released comes from `newest`.
- `p=2` is requested only when `page1` is non-empty and all of it is today. A `limit_reached` p2 is ignored.
- A schedule failure → failure. A `page1` failure → failure. A `newest` failure → still a success.
- Episode numbers come from known releases, else from search. A search failure → `null` episode.
- The upcoming episode lookups run concurrently: the fake suspends, and the test asserts all are started before any completes.

`HomeViewModelTest` cases:
- `init` → `isLoading` → content state, with the mapped counts.
- Failure → `error` set. `Retry` → content.
- `Refresh` with content → `isRefreshing` → updated content.
- `Refresh` failure with content → content kept, flags off, a `ShowError` effect.
- `ScreenStarted` on the same day → no repository call. After `FixedClock` moves to the next day → one reload.
- `ShowClicked("x")` → `OpenDetails("x")`. `SettingsClicked` → `OpenSettings`.
- A second `Refresh` while loading → ignored (one call).

**Every quirk in [§4](#4-data) has a test above:**
- `[]` root: ReleaseListDTOTest
- `limit_reached`: ReleaseListDTOTest, RepositoryImplTest
- `text/html` content type: covered by the existing `HttpClientFactoryTest`. Fixtures are served as `text/html` in `BroadcastServiceTest`.
- empty body: RepositoryImplTest
- placeholder and relative images: ImageUrlDTOMapperTest
- mixed offsets: ReleaseDTOMapperTest
- non-numeric episodes: ReleaseDTOMapperTest, FindLatest…, EpisodeBadge…
- missing `page`: both mapper tests
- stale latest: GetTodayBroadcasts…
- early-release dedupe: GetTodayBroadcasts…
- search filter by slug: FindLatest…

### Compose previews

Each preview uses `@AdaptivePreviews` (light/dark × compact/expanded) inside `BakaMitaiTheme`, with private sample data:

- `HomeScreen`: `PreviewParameterProvider<HomeState>` with loading, content (4 upcoming, 3 latest, one batch, one null cover), only-latest (upcoming empty), both empty, refreshing, and error.
- `TodaySummaryView`: zero and non-zero counts.
- `UpcomingCardView`: with and without a badge, a long 2-line title, a null cover, clickable and non-clickable.
- `ReleaseRowView`: EP, BATCH and MOVIE.
- `EpisodeBadgeView`: regular and special.
- `CoverImageView`: null URL.

`AppNavigation` gets no preview. It needs Koin, and it's app wiring rather than a Screen or View.

---

## 8. Implementation steps

### Phase 1: non-UI

1. [x] **Skill and doc prerequisite.** In `structure` §2, add the row "`TimeZoneProvider` / `SystemTimeZoneProvider` → `common/domain/util/TimeZoneProvider.kt`" next to `DispatcherProvider`.
   **Done when:** the skill lists it, and `util/` placement stays rule-compliant.
2. [x] **Common time providers.**
   - Add the `Clock` provider to `CommonModule`.
   - Add `common/domain/util/TimeZoneProvider.kt`.
   - Add the test fakes `FixedClock` and `FixedTimeZoneProvider`.
   - Extend `KoinModulesTest` to resolve `Clock` and `TimeZoneProvider`.

   **Done when:** `./gradlew testDebugUnitTest --tests "*KoinModulesTest"` passes.
3. [x] **Domain models and the repository interface** (`ShowSlug`, `Show`, `ScheduledShow`, `Release`, `UpcomingBroadcast`, `TodayBroadcasts`, `BroadcastRepository`).
   **Done when:** it compiles, and the `architecture` §1 domain-purity grep prints nothing.
4. [x] **Fixtures and DTOs:**
   - Capture and trim the fixtures in [§7](#7-testing).
   - Add `TodayScheduleDTO`, `ScheduleItemDTO`, `ReleaseDTO` and `ReleaseListDTO` with its root serializer.
   - Add `ReleaseListDTOTest`.

   **Done when:** `ReleaseListDTOTest` passes.
5. [x] **Data mappers:** `ImageUrlDTOMapper`, `ScheduleItemDTOMapper`, `ReleaseDTOMapper`, and their tests.
   **Done when:** the three mapper test classes pass.
6. [x] **Service:** `BroadcastService` + `BroadcastServiceTest`.
   **Done when:** all three methods assert the request and decode the fixtures.
7. [x] **Repository:** `BroadcastRepositoryImpl` + `BroadcastRepositoryImplTest`.
   **Done when:** the error-mapping, empty-body and `limit_reached` cases pass.
8. [x] **Use cases:** `FindLatestEpisodeNumberUseCase`, `GetTodayBroadcastsUseCase`, `GetTodayDateUseCase`, `FakeBroadcastRepository`, and their tests.
   **Done when:** every case listed in §7 for these three classes passes.
9. [x] **DI:** add `HomeModule` and include it in `AppModule`.
   **Done when:** `KoinModulesTest` passes with the home graph.
10. [x] **Phase 1 gate:**
    ```bash
    ./gradlew testDebugUnitTest
    ```
    ```bash
    ./gradlew detekt
    ```
    ```bash
    ./gradlew assembleDebug
    ```
    Also run the `tech-stack` verification greps and the `architecture` §1 greps.

    **Done when:** all of them pass, with zero Detekt findings.

### Phase 2: UI. [Q1](#10-open-questions) is decided: icons come from `material-icons-extended`.

11. [x] **Icons.** Q1 was approved:
    - Add `material-icons-extended` per [§6](#6-dependencies): catalog, `app/build.gradle.kts`, `tech-stack` §1 and the layer table row, and the `library-management` §2 artifact-map row.

    **Done when:** `./gradlew -q :app:dependencies --configuration debugRuntimeClasspath` lists `material-icons-extended:1.7.8`, `./gradlew assembleDebug` passes, and both skills have their new rows.
12. [x] **Resources:** add `PluralResource` to `ResourceTypes.kt`, and these strings to `strings.xml`:
    - `nav_home` "Home", `nav_schedule` "Schedule", `nav_all_shows` "All Shows", `nav_bookmarks` "Bookmarks"
    - `home_app_bar_subtitle` "HOME", `cd_settings` "Settings"
    - `home_summary_title` "Today's Broadcasts", `home_utc_offset` "UTC%1$s"
    - `home_section_upcoming` "Upcoming Today", `home_section_latest` "Latest Releases", `home_section_latest_badge` "Aired Today"
    - `home_episode` "EP %1$s", `home_episode_batch` "BATCH %1$s", `home_episode_movie` "MOVIE"
    - `cd_episode` "Episode %1$s", `cd_episode_batch` "Batch, episodes %1$s", `cd_episode_movie` "Movie"
    - `home_upcoming_empty` "Nothing else is scheduled for today."
    - `home_latest_empty` "Nothing has been released yet today."
    - `home_empty` "Nothing is on the schedule for today."

    Plurals: `home_summary_airing` ("%d Airing"), `home_summary_available` ("%d Available"), `home_section_upcoming_count` ("%d show" / "%d shows").

    **Done when:** it builds, and the `conventions` §2.5 grep prints nothing.
13. [x] **Presentation mappers and UI models:**
    - `EpisodeBadgeUIModel` + mapper
    - `TimeUIModelMapper`, `TodayBroadcastsUIModelMapper`
    - the `todaysummary`, `upcomingcard` and `releaserow` models and mappers
    - their tests

    **Done when:** the mapper tests in §7 pass.
14. [x] **MVI and ViewModel:** `HomeState`, `HomeIntent`, `HomeEffect`, `HomeViewModel`, `HomeViewModelTest`.
    **Done when:** every ViewModel case in §7 passes.
15. **Components:** `EpisodeBadgeView`, `CoverImageView`, `TodaySummaryView`, `UpcomingCardView`, `ReleaseRowView`, each with `@AdaptivePreviews`.
    **Done when:** the previews render in Android Studio, and `./gradlew detekt` is clean (Compose rules: modifier param, no magic numbers).
16. **Screen and Route:** `HomeScreen` (private top bar, header, carousel with breakout, empty view, `PullToRefreshBox`, snackbar host) with the preview provider, and `HomeRoute`.
    **Done when:** all six preview states render in light and dark, compact and expanded.
17. [x] **Navigation shell:**
    - `HomeNavKey`, `HomeNavigation`
    - `TopLevelDestination`, `AppEntryProvider`, `AppNavigation`
    - switch `AppRoute` to `AppNavigation()`
    - `NavKeySerializationTest`

    **Done when:** `android run --apks=app/build/outputs/apk/debug/app-debug.apk` opens on Home, with 4 navigation items and 3 of them disabled.
18. [x] **Docs.** Implementation-only doc edits (done 2026-11-18):
    - **`docs/api.md`:**
      - G1, G2 → §3.3 and §3.4
      - G3 → §3.5, correcting "The instant is correct", and §3.3
      - G4 and G5 → §3.3 and §3.4
      - G6 → §1
      - G7 → §3.1
      - G8 → §3.1 and §3.3
    - **`docs/context.md`:**
      - §4.1: the derivation in [§4.3](#43-derivation-domain), once Q2 and Q6 are confirmed; `kotlin.time.Clock` instead of `kotlinx.datetime.Clock`; the note that `CommonModule` now provides it
      - §3.1: sort order, if Q3 changes it
    - **`CLAUDE.md`** "Current state" and the `structure` §7 "Current repo state": `app/navigation/` exists and Home is the first feature.

    **Done when:** both docs and `CLAUDE.md` reflect the shipped behaviour.
19. **Final verification**, in order:
    ```bash
    ./gradlew testDebugUnitTest
    ```
    ```bash
    ./gradlew detekt
    ```
    ```bash
    ./gradlew assembleDebug
    ```
    ```bash
    ./gradlew assembleRelease && grep -rn "unstable" app/build/compose_compiler/*-composables.txt
    ```
    Then run the `architecture` §1 greps, the `structure` §7 checks, the `tech-stack` §1 verification greps and the `conventions` §2.5 grep, and go through [§9](#9-acceptance-criteria) on an emulator.

    **Done when:** tests and Detekt pass with zero findings, the `unstable` grep prints nothing for Home's Screen and Views, every grep prints nothing, and every acceptance criterion is checked.

---

## 9. Acceptance criteria

**On launch:**
- [x] The app opens on **Home**. The navigation bar shows Home (selected), Schedule, All Shows and Bookmarks. The last three are visibly disabled and do nothing when tapped.
- [x] The top bar shows "BakaMitai" with "HOME" under it. There's no settings icon.
- [x] A first-load skeleton (shimmering, in the content's layout) appears, then the content.

**Banner:**
- [x] It shows "Today's Broadcasts" and the device's UTC offset (for example "UTC-5" on an America/New_York emulator).
- [x] Its "N Airing" count equals the number of Upcoming cards, and "M Available" equals the number of Latest rows.

**Upcoming:**
- [x] It lists today's shows (device zone) that haven't aired, sorted by time ascending. It matches the website's schedule widget for the same zone, minus shows already listed under Latest.
- [x] Each card shows a cover (or the neutral placeholder), a title of up to 2 lines, and a 12-hour time ("6:30 PM").
- [ ] The badge is "EP NN", where NN is the previous numeric episode + 1 (check one show against `f=search`). It's hidden when unknown.
- [x] The carousel scrolls edge to edge and snaps to the start.

**Latest:**
- [x] It lists only releases whose time falls on today's local date, in ascending order.
- [x] Rows still appear while `f=latest` lags (G1).
- [ ] Each row shows the cover, title, 12-hour release time on the thumbnail, and a badge: "EP 07", or a tertiary "BATCH 01-24" / "MOVIE".

**Behaviour:**
- [x] Pull-to-refresh shows the indicator and updates both sections.
- [x] Airplane mode + cold start → error message with **Retry**. After reconnecting, Retry loads the content.
- [x] Airplane mode + pull-to-refresh with content showing → the content stays, and a snackbar shows the error.
- [x] Rotating keeps the content without a new request (Logcat `Ktor` lines in a debug build).
- [x] Backgrounding the app, moving the device date to the next day, then returning → Home reloads by itself. Returning on the same day → no reload.
- [x] Changing the device time zone, then pulling to refresh → times, the offset label and the "today" filter follow the new zone.
- [x] Tapping a card does nothing and shows no ripple (Details isn't built yet).

**Appearance:**
- [x] A tablet or foldable (medium/expanded) shows a navigation rail, 24 dp margins, and Latest in 2 or 3 columns.
- [x] Light and dark themes both render with readable badges and chips.
- [ ] TalkBack:
  - announces the section titles as headings
  - reads the banner as one item
  - reads each card as one item including "Episode 08"
  - announces the disabled tabs as disabled

---

## 10. Open questions

**Blocking:** none. Q1 was decided on 2026-11-18 (see its row). Every other question is non-blocking: the implementor uses the recommendation unless a human overrides it.

| # | Blocks? | Question | Recommendation |
|---|---|---|---|
| Q1 | **Decided 2026-11-18:** the user approved `material-icons-extended`. Step 11 adds it. | Which icon source? No `material-icons-*` artifact is on the classpath (material3 1.4 dropped it). | **Approve `androidx.compose.material:material-icons-extended`** (BOM-managed 1.7.8). It matches the skills' `Icons.*` / `ImageVector` usage. R8 strips unused icons in release. **Alternative:** Material Symbols vector drawables in `res/drawable/` with a `DrawableResource` typealias. That adds no library, but needs a `conventions` §2.5 change, because it passes raw drawable ids. |
| Q2 | No (spec change) | `f=latest` lags by about 3 days (G1). Should Home also read `f=search&s=` (G2)? | **Yes.** Merge `p=1` + `s=` (+ `p=2`), as in [§4.3](#43-derivation-domain). When `f=latest` is fresh, the result equals the spec's behaviour. Update context.md §4.1 in step 18. |
| Q3 | No | Latest order: spec ascending, design newest-first. | **Ascending (spec)**, because the spec decides behaviour. Newest-first is a one-line change if preferred. |
| Q4 | No | What do the three unbuilt tabs do now? | **Show them, disabled** (`enabled = false`, `TopLevelDestination.key = null`). This needs no placeholder screens or fake features, and doesn't break the rule that NavKeys live in features. Each later plan just fills in its key. |
| Q5 | No | What do the gear and card taps do before Settings and Details exist? | **Hide the gear and make cards non-clickable.** `homeEntry` takes nullable `onOpenSettings` / `onOpenDetails`. `null` hides the gear and drops the card click (no dead ripples). The full intent → effect → lambda path is built and tested now, so later plans only edit `AppEntryProvider`. |
| Q6 | No | Data corrections: (a) remove from Upcoming any show already released today (G7)? (b) correct the 1 h `release_date` skew (G3)? | **(a) Yes**, as a cheap safety net. **(b) No.** Show `release_date` as-is, because the app can't detect when the server is fixed. Document it in api.md. Revisit if users notice. |
| Q7 | No | Bookmark toggles on cards (design)? | **Not in v1.** The Bookmarks plan can add them through its `domain` use cases. A feature may import another feature's `domain`. |
| Q8 | No | Countdowns ("In 42m") and "aired … ago" chips? | **Not in v1.** They aren't in the spec, they need a minute ticker, and "ago" times would be wrong under G3. Revisit after Settings. The schedule `time` is accurate (G3), so countdowns would be feasible later. |
| Q9 | No | Time format before the Settings feature exists? | **12-hour** ("8:00 AM"), the spec default. The design's 24-hour times only become available through the setting. |
| Q10 | No | "Timeline >" link? | **Omit.** The Schedule plan can add it as "select the Schedule tab". |
| Q11 | No | `TimeZoneProvider` in `common/domain/util/` needs a `structure` §2 row. OK to add? | **Yes**, in step 1. It's the same kind of injectable system abstraction as `DispatcherProvider`, and Schedule, Details and Bookmarks all need the device zone. |

---

## 11. Implementation notes

- **Decisions applied (2026-11-18).** Q1 was decided by the user: `material-icons-extended` (BOM 1.7.8) is in the catalog and `app/build.gradle.kts`, with rows in `tech-stack` §1, the `tech-stack` layer table and the `library-management` §2 artifact map. Q2–Q11 use their recommendations: Latest merges `f=latest` p=1 (+p=2) with `f=search&s=` and removes duplicates; ascending order; unbuilt tabs disabled; gear hidden and cards not clickable; already-released shows removed from Upcoming and no `release_date` correction; no bookmark toggles, countdowns or "Timeline"; 12-hour times; `TimeZoneProvider` row added to `structure` §2.
- **G1 reproduced.** At 20:28 UTC on 18 Nov, `f=latest` p=1 still started at *Mon 16 Nov 08:32 -0500* while `f=search&s=` had today's releases, so the merge is doing real work. `s=Sasaki to Pii-chan` returned `+0000` offsets (G4 again).
- **Fixtures** (`app/src/test/resources/subsplease/`, captured with `tz=America/New_York`):
  - `schedule_today.json`: captured at 15:28 New York, when every show had already aired. The last two items' `aired` were set to `false` so the fixture has both values.
  - `latest_page1.json`: live page 1 had no batch and no placeholder. The real batch (`Vinland Saga S2 - 01-24`) and placeholder cover (`Kyouran Reijou Nia Liston`) are in `search_newest.json`, where they actually appeared.
  - `latest_limit_reached.json` and `search_no_results.json` were written from the documented bodies (`{"error":"limit_reached"}`, `[]`), to save requests.
  - `downloads` were trimmed to the 1080p entry, and magnets cut after the info hash.
- **Additions within scope:**
  - `ReleaseDTOMapper` also drops a `release_date` that parses but isn't a real date (31 Nov, or a weekday that doesn't match).
  - A non-numeric, non-batch, non-movie episode ("OVA") is shown uppercase, per DESIGN.md's uppercase badges. Its content description is `cd_episode`.
  - `POSTER_ASPECT_RATIO` (`coverimage/CoverImageView.kt`) and `UpcomingCardWidth` (`upcomingcard/UpcomingCardView.kt`) are public, because the cards and the screen's carousel use them.
- **Empty body (G6)** reaches `safeApiCall` as a content-conversion error, so it maps to `DomainError.InvalidData` without extra handling (`BroadcastRepositoryImplTest`).
- **Detekt** (no rule changes): in `HomeScreen.kt`, the window-size helpers became one private `HomeLayout` / `homeLayout()` and the Upcoming section became `LazyGridScope.upcomingSection()` (LongMethod, TooManyFunctions). The mappers use single-return expressions (ReturnCount).
- **`HomeEmptyView`** centers its text horizontally, `spacing.xl` below the banner, rather than vertically in the viewport, because a scrollable column can't weight-fill.
- **Insets:** the Home `Scaffold` uses `contentWindowInsets = …only(Top + Horizontal)` as §5 suggests. On the emulator, the list ends right above the navigation bar, with no doubled padding.
- **Compose report:** every Home Screen and View is `restartable skippable`, and `HomeState` and every UI model are `stable`. The one `unstable` line is the existing `ObserveAsEvents(vararg keys)` in `common/ui/util/`, which is neither a Screen nor a View.
- **Step 15 / 16 previews:** `ReleaseRowView`, `EpisodeBadgeView` and `CoverImageView` were rendered with `android studio render-compose-preview`. The open Studio project hasn't synced Gradle since the icons library was added, so `HomeScreen`, `TodaySummaryView` and `UpcomingCardView` (which use `Icons`) don't compile in the IDE yet. Gradle builds pass. Re-render after a Gradle sync, then tick 15 and 16. One preview bug was fixed: `Surface` passes its minimum size on, so preview content is wrapped in a `Column`.
- **Step 18 (docs), done afterwards in the same session:** the implementor edits only the plan file, so these were done separately: `docs/api.md` (G1–G8, including the G3 correction to §3.5), `docs/context.md` §4.1 (the §4.3 derivation, `kotlin.time.Clock`, `CommonModule` provides it), `CLAUDE.md` "Current state" and `structure` §7 "Current repo state" (`app/navigation/` exists; Home is the first feature).
- **Step 19:** all gates and greps pass. The `conventions` §2.5 grep matches only the existing prose comment in `ResourceTypes.kt`. Not ticked, because the §9 acceptance criteria are for the tester. A launch check on `emulator-5554` (Asia/Beirut) passed: the app opens on Home with 4 navigation items (3 disabled), no gear, a "UTC+2" banner, an empty Upcoming (every show had aired) and 9 Latest rows in ascending order.

---

- **Loading is a skeleton, not a spinner (2026-11-18, after QA).** By the user's request, every loading state in the app is now a shimmering skeleton, a rule recorded in `best-practices` §5.3 and the other skills.
  - `common/ui/component/loading/LoadingView.kt` is gone. `common/ui/component/skeleton/SkeletonView.kt` has `SkeletonView` (one band swept over everything inside it, drawn with `BlendMode.SrcAtop` in an offscreen layer and read in the draw phase only; TalkBack reads it as one "Loading" item), plus `SkeletonBlock`, `SkeletonTextLine` and `textLineHeight`. A new theme token, `extendedColors.shimmerHighlight`, colors the band; blocks use `surfaceContainerHighest`.
  - `TodaySummarySkeletonView`, `UpcomingCardSkeletonView`, `ReleaseRowSkeletonView` and `EpisodeBadgeSkeletonView` sit next to their components and reuse their private dimensions.
  - `HomeScreen` shows a private `HomeSkeleton` that shares the grid helper (`HomeGrid`) with the content. To stay within Detekt's 11-functions-per-file limit, the section header moved to `component/sectionheader/SectionHeaderView.kt` and `homeLayout()` became the `HomeLayout(windowSizeClass)` constructor.
  - Checked on emulator-5554 in dark and light themes with rapid `screencap` captures during a cold start: the skeleton matches the content layout and the band enters from the left as soon as it appears.

## 12. QA

**Run:** 2026-11-18, `emulator-5554`, Android 17 (API 37), 1080×2400 at 420 dpi, device zone **Asia/Beirut (UTC+2)**. The zone was switched to Asia/Tokyo, Europe/Moscow and America/New_York to get Upcoming items and to test zone and day changes, then restored. Live data was compared with `f=schedule&h=true`, `f=latest&p=1`, `f=search&s=` and two title searches for the same zones.

### Journeys

| # | Journey | Result |
|---|---|---|
| J1 | Launch: Home selected, 4 tabs with Schedule / All Shows / Bookmarks disabled (`enabled=false`, taps do nothing), "BakaMitai" / "HOME", no gear | PASSED |
| J2 | First load: spinner, then content; banner "UTC+2", "0 Airing" / "9 Available" | PASSED |
| J3 | Latest (Beirut): the 9 releases dated 18 Nov in UTC+2, ascending 2:03 PM → 6:17 PM, "EP NN" badges, long titles ellipsized at 2 lines; matches the API. `f=latest` p=1 still ended on 16 Nov (G1), so every row came from `s=` | PASSED |
| J4 | Upcoming (Tokyo, Moscow): matches the zone's schedule minus aired/released shows, 12-hour times; badges checked against search (FX Senshi Kurumi-chan 07 → "EP 08", Tsuihou sareta… 20 → "EP 21"); carousel scrolls edge to edge, snaps to start and ends on the margin | PASSED |
| J5 | Pull-to-refresh: indicator while pulling, one load (3 requests + episode searches), both sections updated | PASSED |
| J6 | Airplane mode + cold start → "Can't reach SubsPlease…" + Retry; Retry offline → error again; online → spinner → content | PASSED |
| J7 | Airplane mode + pull-to-refresh with content → content kept, error snackbar | PASSED |
| J8 | Rotation (portrait ↔ landscape), dark ↔ light, font scale and window-size changes → content kept, 0 requests | PASSED |
| J9 | Back to foreground on the same day → 0 requests; on a different local date (zone moved across midnight while in the background, both directions) → automatic reload | PASSED |
| J10 | Zone change + pull-to-refresh: Beirut → Tokyo → Moscow → New York; offset ("UTC+9", "UTC+3", "UTC-5"), times and the "today" filter follow (Tokyo Latest = the 3 releases after 00:00 JST) | PASSED |
| J11 | Tapping a card or row: no ripple, no navigation | PASSED |
| J12 | Medium (1600×2560) and expanded (2560×1600): rail, 24 dp margins, Latest in 2 / 3 columns | FAILED, then PASSED after fix 1 |
| J13 | Light theme: badges, chips and time chips readable | PASSED |
| J14 | Font scale 1.3: no clipping or overlap | PASSED |
| J15 | Accessibility: merged semantics checked with a temporary instrumented probe (deleted afterwards): banner is one heading node "Today's Broadcasts, UTC+9, 1 Airing, 1 Available"; "Upcoming Today" and "Latest Releases" are headings; each card / row is one node with content description "Episode 08"; disabled tabs report `enabled=false` | PASSED (semantics only; see below) |
| J16 | Back at Home's root leaves the app | PASSED |

No crash, ANR or app error in Logcat during the run.

### Issues

| # | Type | Issue | Status |
|---|---|---|---|
| 1 | UI | On medium and expanded windows (navigation rail), the end of the list and the snackbar sat under the bottom system bar (taskbar): the last Latest row couldn't be scrolled fully into view. Root cause: `HomeScreen`'s `Scaffold` limited `contentWindowInsets` to Top + Horizontal, assuming the navigation suite pads for the bottom bar. `NavigationSuiteScaffold` consumes the bottom inset only for its bar; its rail consumes only Start. | **Fixed** in `feature/home/presentation/HomeScreen.kt`: the `Scaffold` uses its default content insets, which already leave out what the suite consumed. Re-checked on compact portrait and landscape (no doubled padding), medium and expanded. UI-only, so no unit test can catch it. |
| 2 | UI (beyond the 1.3 criterion) | At font scale 2.0, the release-time chip on the 96 dp thumbnail clips its "AM" / "PM" ("12:02"), and the navigation labels break mid-word ("Schedul / e", "Bookma / rks"). | Open: needs a design decision (wrap the chip, move the time, or ellipsize the labels). |
| 3 | Spec question | A zone change that keeps the same local date (e.g. Beirut → New York while in the background) doesn't reload: the offset, times and Upcoming stay in the old zone until pull-to-refresh. §9 only asks for pull-to-refresh. | Open: should `ScreenStarted` also reload when the zone changed? |
| 4 | Note | Back at Home's root finishes `MainActivity` (`wm_finish_activity … app-request`) instead of moving the task to the back, so reopening the app refetches. It meets "leaves the app"; reported for awareness. | Open (platform/back behaviour, not Home logic). |
| 5 | Note | Phone landscape keeps the bottom bar (the suite's default for compact heights) and leaves little room for content. | As designed (library default). |

### Not covered

- **TalkBack speech:** TalkBack ignored injected gestures and keyboard shortcuts on this emulator, so its spoken output wasn't heard. The semantics it reads were verified instead (J15). The app-bar title and subtitle are two separate stops.
- **Live data that didn't occur today:** BATCH / MOVIE badges, the placeholder cover, a hidden Upcoming badge, and both sections empty. They're covered by unit tests and previews only.
- **The device date itself** wasn't changed; the new-day reload was triggered by moving the zone across midnight.
- The refresh spinner after release: responses took about 120 ms, so only the pull indicator was captured.
