# 2. Schedule

> **Status:** Implemented · **Design:** [Google Stitch project `5201581584167289517`, screen `1df2cd7a8e8c4b9cbcb901d12956e68c` ("Weekly Schedule")](https://stitch.google.com/projects/5201581584167289517?node-id=1df2cd7a8e8c4b9cbcb901d12956e68c) · **Planned:** 2026-11-19
> **Spec:** [context.md §2](context.md#2-navigation-shell), [§3.2](context.md#32-schedule), [§3.6](context.md#36-settings), [§4.2](context.md#42-schedule), [§4.5](context.md#45-formatting-rules-all-screens), [§6](context.md#6-suggested-feature-breakdown) · **API:** [api.md §1](api.md#1-basics), [§3.1](api.md#31-todays-schedule), [§3.2](api.md#32-weekly-schedule), [§7](api.md#7-client-guidelines)
> **Depends on:** [1-home.md](1-home.md) (implemented). Schedule is the second user of Home's shared pieces, so this plan moves them to `common/` first ([§5.1](#51-shared-code-moved-from-featurehome)).

**Design source.** The design is a Google Stitch project, not Figma or Sketch. Only one screen exists: **`1df2cd7a8e8c4b9cbcb901d12956e68c`** in project **`5201581584167289517`**, a 390×1400 dark-mode frame. It was captured as a render (`screen.jpg`, whose fonts are a serif fallback; the real fonts are DESIGN.md's Plus Jakarta Sans and Space Grotesk) and a condensed copy of its HTML/Tailwind. Every "node" below names that screen plus the HTML section inside it (`<header>`, `#day-tabs-container`, `#current-day-heading`, `.filter-pill`, `#anime-grid` `<article>`, `#empty-state`, `<nav>`). Loading, day-empty, error, light-mode and tablet states aren't in the design; they're proposed in [§2.8](#28-states-not-in-the-design-all-proposed).

**How design and spec are reconciled** (same rule as [1-home.md](1-home.md)): `context.md` decides **behaviour and data**, the Stitch design decides **visual treatment**. Anything the API can't supply is left out, never invented. Every disagreement is in [§3](#3-design-vs-spec).

---

## 1. Summary

Schedule shows SubsPlease's **weekly release schedule** in the device's time zone. A sticky strip of seven day chips ("MON 16" … "SUN 22", the dates of the current week) sits under the top app bar. Today's chip is selected when the screen opens and is marked with a dot. Below it, the selected day has a header ("Thursday", the device's UTC offset, "6 Shows Airing") and a grid of poster cards, each with the cover, title and release time.

On **today's** tab only, each card also carries an **Aired** / **Upcoming** badge taken from today's schedule `aired` flag, and filter chips "All (6) / Aired (5) / Upcoming (1)" narrow the grid. Other days have no status: the weekly endpoint has none, and anything derived from the day order would be a guess ([§4.3](#43-aired-and-upcoming), [Q2](#10-open-questions)).

The user can pull to refresh. The screen reloads by itself when the app comes back on a new local day, and then selects the new today.

**Entry point:** the **Schedule** tab of the `NavigationSuiteScaffold`, enabled by this plan.

**In scope:**
- the Schedule feature (`feature/schedule/`, all four layers)
- **moving Home's shared pieces to `common/`**, with Home's imports, previews and tests updated ([§5.1](#51-shared-code-moved-from-featurehome)): `Show`, `ShowSlug`, `ScheduledShow`, `GetTodayDateUseCase`, `ScheduleItemDTO`, `TodayScheduleDTO`, their mapper, `toCoverUrl`, the time formatter, the UTC-offset label, `CoverImageView` and the top app bar
- the in-memory weekly-schedule cache from [api.md §7](api.md#7-client-guidelines) (about 1 h, bypassed by pull-to-refresh)
- enabling the Schedule tab (`TopLevelDestination`, `AppEntryProvider`)
- doc updates for the API findings in [§4.2](#42-api-gaps-found-live)

**Out of scope, with hooks left for later plans:**
- **Settings.** No settings data layer yet. The week starts on **Monday** and times use **12-hour** format, the spec defaults ([Q1](#10-open-questions)). The use case already takes `weekStart`, and the time formatter is the one shared mapper, so the Settings plan only changes the source. The gear stays hidden.
- **Details.** Cards aren't clickable until the Details plan passes `onOpenDetails` ([Q5](#10-open-questions)).
- **Things the design shows that the API can't supply:** episode labels, ratings, studio and genre lines, bookmark toggles, the sort button, JST times. See [§3](#3-design-vs-spec).
- **Home's "Timeline >" link** ([1-home.md Q10](1-home.md#10-open-questions)): a follow-up ([Q12](#10-open-questions)).

---

## 2. Design breakdown

### 2.0 Token mapping

The mapping in [1-home.md §2.0](1-home.md#20-token-mapping) applies unchanged. **No new theme work is needed.** The extra tokens this screen uses:

| Design (Tailwind/HTML) | Compose |
|---|---|
| `bg-primary` / `text-on-primary` (selected day chip, selected filter chip) | `colorScheme.primary` / `colorScheme.onPrimary` |
| `surface-container-lowest/80` (time chip) | `colorScheme.surfaceContainerLowest.copy(alpha = 0.8f)` |
| `surface-dim/40` (middle stop of the poster scrim) | `colorScheme.surfaceDim.copy(alpha = 0.4f)` |
| `rounded-DEFAULT` (16 px) day chips | `MaterialTheme.shapes.medium` |
| `headline-md` day title / `label-md` subtitle | `typography.headlineMedium` / `typography.labelMedium` |
| `min-w-[52px] h-[64px]` day chip | `DayChipMinWidth = 52.dp`, `DayChipHeight = 64.dp` (private `Dp` constants) |
| Material Symbols `calendar_today`, `broadcast_on_home`, `schedule`, `alarm`, `calendar_view_day` | `Icons.Outlined.CalendarToday`, `.BroadcastOnHome`, `.Schedule`, `.Alarm`, `.CalendarViewDay` (all present in `material-icons-extended` 1.7.8, checked in the Gradle cache) |

Alpha values are named `private const`s, as in Home.

### 2.1 Top app bar and navigation suite

Nodes: HTML `<header>` and `<nav>`.

- **Layout:** the same 64 dp bar as Home: "BakaMitai" with the subtitle "SCHEDULE" under it, and a trailing settings action. The **day strip (§2.2) is part of the top bar slot**, directly under the bar, so it stays pinned while the grid scrolls (the design's `sticky top-16` subheader).
- **Components:**
  - `common/ui/component/toplevelappbar/TopLevelAppBarView.kt` (moved out of `HomeScreen.kt`, [§5.1](#51-shared-code-moved-from-featurehome)). It gains an optional `bottomContent` slot. The bar and the slot share one background that follows the scroll state (below), so the strip and the bar always have the same color.
  - Navigation: unchanged `NavigationSuiteScaffold`. The **Schedule** item becomes enabled and selectable.
- **Content:** title `app_name`; subtitle `schedule_app_bar_subtitle` ("SCHEDULE", stored uppercase).
- **States:** the settings action is hidden until Settings exists ([Q5](#10-open-questions)). The strip is shown only when content is loaded; during the first load its skeleton is drawn at the same place in the content area (§2.8).
- **Interactions:** `TopAppBarDefaults.pinnedScrollBehavior()`, as on Home. Tapping the Schedule tab selects it; system back from Schedule returns to Home ("exit through home", `Navigator.goBack()`).
- **Theming:** container `surface`, `surfaceContainer` once content scrolls under it (Home's colors). The wrapper paints the color, and `TopAppBar` gets transparent container colors:
  ```kotlin
  val isScrolled by remember(scrollBehavior) { derivedStateOf { scrollBehavior.state.overlappedFraction > SCROLLED_FRACTION } } // 0.01f, as M3
  val containerColor by animateColorAsState(if (isScrolled) colors.surfaceContainer else colors.surface)
  ```
  The design's `backdrop-blur` and the shadow under the strip are dropped ([§3](#3-design-vs-spec) row 18).
- **Accessibility:** unchanged from Home (`cd_settings`, labels name the navigation items).

### 2.2 Day strip

Node: `#day-tabs-container` (the `.day-tab` buttons).

- **Layout:**
  - A `LazyRow` with `contentPadding = PaddingValues(horizontal = margin, vertical = spacing.sm)` and `horizontalArrangement = spacedBy(spacing.xs)`, private `DayStrip` in `ScheduleScreen.kt`.
  - Seven chips in **tab order**: Monday → Sunday (the default week start; Sunday → Saturday once the Settings plan passes *First day of week*).
  - **Compact:** 7 × 52 dp + gaps don't fit in 328 dp, so the row scrolls horizontally. When the selected index changes, `LaunchedEffect(selectedIndex) { listState.animateScrollToItem((selectedIndex - 1).coerceAtLeast(0)) }` keeps the selected chip and its left neighbour in view. This is purely visual scroll state.
  - **Medium and expanded:** all seven fit; the row starts at the content margin (`marginTablet`).
- **Components:** `component/daychip/DayChipView.kt`: `Surface(selected = …, onClick = …, shape = shapes.medium)`, `widthIn(min = DayChipMinWidth)`, `height(DayChipHeight)`, holding a centered `Column`:
  - weekday: `labelSmall`, stored uppercase ("THU"), at 75 % alpha when not selected
  - date: `headlineSmall` ("19")
  - a 6 dp dot `spacing.xs` below the date, **drawn only on today's chip**. Its space is reserved on every chip, so all labels line up.
- **Content** (`DayChipUIModel`): `dayOfWeek`, `weekdayLabel` (`day_short_*`), `dateLabel` (day of month of this week's date, unpadded), `contentDescription`, `isToday`.
- **States:**
  - selected: `primary` container, `onPrimary` content, `onPrimary` dot
  - not selected: `surfaceContainerLow` container, `onSurfaceVariant` content, `primary` dot
  - Same size in both states. The design's 52×64 → 56×68 growth and glow on selection are dropped, so nothing shifts when the selection changes (proposed, [§3](#3-design-vs-spec) row 3).
- **Interactions:** tapping a chip sends `ScheduleIntent.DaySelected(dayOfWeek)`. The design's vibration and `active:scale-95` are dropped; M3's ripple remains.
- **Accessibility:**
  - The `LazyRow` has `Modifier.selectableGroup()`. Each chip has `role = Role.Tab` and merges its children, with `contentDescription` = "Thursday 19" or "Thursday 19, today" (`cd_schedule_day` / `cd_schedule_day_today`). TalkBack then adds "selected" and "tab".
  - 52×64 dp is above the 48 dp minimum.

### 2.3 Day header

Nodes: `#current-day-heading`, `#current-day-subtitle`, `#total-count-badge`.

- **Layout:** a full-span grid item (it scrolls with the cards), `spacing.md` above the first row of cards:
  - Row: a `Column(Modifier.weight(1f))` with the title row (`Icons.Outlined.CalendarToday` 20 dp **before** the title, `spacing.sm` apart; moved to the left at the user's request, 2026-11-19) and the subtitle under it; then the count pill.
  - Under it, `spacing.sm` lower, the filter chips (§2.4) when the day has any.
- **Components:** `component/dayheader/DayHeaderView.kt` (+ `DayHeaderSkeletonView`).
  - Count pill: `Surface(shape = CircleShape, color = surfaceContainerHigh, contentColor = primary)`, padding 12 dp × 6 dp, holding `Icons.Outlined.BroadcastOnHome` (16 dp, `secondary`) and `Text(labelSmall)`.
- **Content** (`DayHeaderUIModel`):
  - `title`: the full day name alone ("Thursday"), from `DayOfWeek.toDayName()`. Changed after implementation at the user's request (2026-11-19): it was "%1$s Schedule" (`schedule_day_title`, now removed).
  - `subtitle`: `schedule_time_zone` "Times in %1$s" with the device offset label ("Times in UTC+2"). It replaces "JST Simulcast Feed" ([§3](#3-design-vs-spec) row 5).
  - `countLabel`: plural `schedule_show_count` "%d Show Airing" / "%d Shows Airing", the day's show count before filtering.
  - `filters`: the filter chips, empty when the day has no statuses.
- **Theming:** title `headlineMedium` / `onSurface`, icon `secondary`; subtitle `labelMedium` / `onSurfaceVariant`.
- **Accessibility:** the title has `semantics { heading() }`. The pill merges into one item ("6 Shows Airing"); its icons are decorative.

### 2.4 Status filter chips (today only)

Node: the `.filter-pill` buttons. The sort button next to them is omitted ([§3](#3-design-vs-spec) row 11).

- **Layout:** a horizontally scrollable `Row`, `spacedBy(spacing.xs)`, inside `DayHeaderView`.
- **Components:** M3 `FilterChip` (32 dp high, 48 dp touch target), `shape = CircleShape`, `border = null`, no leading icon, label `labelMedium`.
- **Content** (`StatusFilterChipUIModel(filter, label)`): "All (6)", "Aired (5)", "Upcoming (1)" (`schedule_filter_all/aired/upcoming`, the count as `%1$d`). All counts are the day's counts before filtering.
- **States:**
  - Shown **only when the selected day has statuses**: today's tab, when today's schedule loaded ([§4.3](#43-aired-and-upcoming)). Other days show no row.
  - Selected: `primary` / `onPrimary`. Not selected: `surfaceContainerHigh` / `onSurfaceVariant` (`FilterChipDefaults.filterChipColors`). The selection glow is dropped.
- **Interactions:** tapping a chip sends `ScheduleIntent.FilterSelected(filter)`. The selection is kept while the user visits other days and applies again on today.
- **Accessibility:** `FilterChip` already announces its selected state.

### 2.5 Schedule card

Node: `#anime-grid` `<article class="anime-card">` (Bleach, Delicious in Dungeon, Bocchi the Rock!).

- **Layout:** poster (`fillMaxWidth().aspectRatio(POSTER_ASPECT_RATIO)`, 3 : 4.2), then the title with `spacing.sm` padding. On the poster, inset `spacing.sm`:
  - status badge top-start
  - time chip bottom-start
  - The bookmark toggle (top-end) and episode label (bottom-end) are omitted ([§3](#3-design-vs-spec) rows 12 and 15).
- **Grid:** `LazyVerticalGrid(GridCells.Fixed(columns))`, columns **2 / 3 / 4** on compact / medium / expanded (DESIGN.md: 2 on mobile, 3–4 on tablet), `contentPadding` horizontal `margin` (`marginTablet` from medium) and vertical `spacing.md`, both arrangements `spacedBy(spacing.gutter)`.
- **Components:** `component/schedulecard/ScheduleCardView.kt` (+ `ScheduleCardSkeletonView`), built like Home's `UpcomingCardView`:
  - `Card(onClick = …)` when clickable, otherwise the non-clickable overload with `Modifier.semantics(mergeDescendants = true) {}`
  - `CardDefaults.cardColors(containerColor = surfaceContainerLow)`, zero elevation, `shape = shapes.large`
  - `CoverImageView` (now in `common/ui/component/coverimage/`)
  - scrim: `Brush.verticalGradient(0f to Color.Transparent, 0.5f to surfaceDim@0.4, 1f to surfaceContainerLow)`
  - status badge: `component/schedulecard/component/statusbadge/StatusBadgeView.kt` (§2.6)
  - time chip: private `TimeChip` in `ScheduleCardView.kt`, a `Surface(shape = CircleShape, color = surfaceContainerLowest@0.8)` with padding 8 dp × 2 dp, an icon (14 dp) and `Text(labelSmall)`
- **Content** (`ScheduleCardUIModel`):
  - `key = "${slug}@${time}"` (unique within a day, like Home's upcoming keys)
  - `title`: the schedule `title`, `maxLines = 2`, `minLines = 2`, ellipsized (two lines replace the studio/genre/rating line, keeping the card height)
  - `coverUrl`: absolute or `null` (placeholder fill)
  - `timeLabel`: the schedule `time` in 12-hour format ("5:00 PM"), no zone suffix
  - `status`: `StatusBadgeUIModel?`, `null` on every day but today (and on today when its schedule didn't load)
- **States:** time chip style follows the status:
  - **Upcoming:** `Icons.Outlined.Alarm`, icon and text `tertiary`
  - **Aired or no status:** `Icons.Outlined.Schedule` in `primary`, text `onSurface`
- **Interactions:** none until Details exists. Then a tap sends `ScheduleIntent.ShowClicked(slug)` ([Q5](#10-open-questions)). The design's hover lift and image zoom are dropped.
- **Theming:** title `titleMedium` / `onSurface`.
- **Accessibility:** cover `contentDescription = null` (the title is adjacent). The merged card reads "Aired, 5:00 PM, Bleach: Thousand-Year Blood War".

### 2.6 Status badge

Node: the "● Aired" / "● Upcoming" pills on the posters.

- `StatusBadgeView(model: StatusBadgeUIModel, modifier)`: `Surface(shape = CircleShape)`, padding 10 dp × 2 dp (DESIGN.md status badge "2px 10px"), a 6 dp dot and `Text(labelSmall)`, `spacing.xs` apart.
- **Aired:** `secondary` text and dot on `surfaceContainerHighest@0.8`. Static dot.
- **Upcoming:** `tertiary` text and dot on `tertiaryContainer@0.3`. The dot pulses: a `rememberInfiniteTransition` alpha read in `Modifier.graphicsLayer { alpha = … }`, so it never recomposes (`best-practices` §1.3), as Home's "Airing" chip does.
- Labels `schedule_status_aired` "Aired" / `schedule_status_upcoming` "Upcoming", in the design's mixed case.
- `StatusBadgeSkeletonView` isn't needed: the card skeleton has no badge, because the status isn't known before loading.

### 2.7 Empty views

Node: `#empty-state` (hidden by default).

- **Filter matches nothing** (today, e.g. "Upcoming" late in the evening). This is the design's state:
  - a 64 dp `surfaceContainerHighest` circle with `Icons.Outlined.CalendarViewDay` (32 dp, `secondary`)
  - title `schedule_filter_empty_title` "No simulcasts found" (`headlineSmall`)
  - message `schedule_filter_empty_message` "There are no shows matching this filter for the selected broadcast day." (`bodyMedium`, `onSurfaceVariant`, centered, `widthIn(max = 320.dp)`)
  - a filled `Button` `schedule_filter_reset` "Reset Filters" → `ScheduleIntent.ResetFilterClicked`
- **Day has no shows** (not in design, proposed): the same view without the button, title `schedule_day_empty_title` "Nothing scheduled", message `schedule_day_empty_message` "No shows are scheduled for %1$s." with the day name.
- Both are a private `ScheduleEmptyView` in `ScheduleScreen.kt`, rendered as a full-span grid item under the header, so the header stays and pull-to-refresh still works. Model: `ScheduleEmptyUIModel(title, message, showResetAction)`.
- The title has `heading()`; the icon is decorative.

### 2.8 States not in the design (all proposed)

| State | Proposal |
|---|---|
| **First load** | A private `ScheduleSkeleton` fills the content area under the bar (`best-practices` §5.3), in **one** `SkeletonView`: a non-scrolling row of 7 `DayChipSkeletonView`s with the strip's exact padding and spacing (at the strip's future position), then the shared grid helper with `userScrollEnabled = false`: a full-span `DayHeaderSkeletonView` (title and subtitle lines, the pill block, and 3 filter-chip pill blocks, because today opens first) and `columns × 3` `ScheduleCardSkeletonView`s. The top app bar is real. |
| **Error, no content** | `ErrorView(model = state.error, onRetry = { onIntent(ScheduleIntent.Retry) })` replaces strip and grid. No pull-to-refresh. |
| **Refreshing** | `PullToRefreshBox` indicator over the grid, under the strip. The content stays interactive. |
| **Refresh failed, content present** | Keep the content; snackbar with the error message (`ScheduleEffect.ShowError`). |
| **Today's schedule failed, weekly loaded** | The week shows normally; today has no badges and no filter row. No error message (best effort). |
| **Day without shows** | §2.7. |
| **Light theme** | Same color roles; `LightColorScheme` resolves them. |
| **Medium / expanded** | Rail navigation, 24 dp margins, 3 / 4 grid columns, the strip fits without scrolling. |
| **Day switch** | The grid sits in `key(state.selectedDay) { … rememberLazyGridState() … }`, so each day opens at the top and keeps its scroll position across rotation. |

---

## 3. Design vs. spec

| # | Item | Design | docs/context.md | Recommendation |
|---|---|---|---|---|
| 1 | Day tab labels | "FRI 25": weekday plus date | "Tabs for each day of the week" (no dates) | **Keep the dates.** They're the current week's dates (the week containing today, from the week start), computed from the clock and the device zone. Nothing invented ([Q6](#10-open-questions)). |
| 2 | Tab order | Mon → Sun | Follows *First day of week* (Monday default) | **Monday → Sunday** until the Settings plan passes the setting ([Q1](#10-open-questions)). |
| 3 | Today / selection marker | The tapped chip turns violet, grows to 56×68 and gets a dot (the dot follows the selection) | Today's tab is selected on open | **Fill marks the selection, the dot marks today**, so today stays findable while another day is selected. Same chip size in both states (no layout shift). |
| 4 | Times | "17:00 JST", 24-hour | Device zone; 12-hour default (§3.6, §4.5) | **Device zone, 12-hour, no suffix** ("5:00 PM"), as on Home. |
| 5 | Subtitle | "JST Simulcast Feed" | Device zone | **"Times in UTC+2"**: the device offset, with Home's offset format (moved to `common/ui/mapper/`). |
| 6 | Day title | "Friday Schedule" + calendar icon | Not specified | **Keep** the title and icon, but with the day name alone and the icon on the left ("[icon] Friday"), per the user (2026-11-19). |
| 7 | Count badge | "18 Shows Airing" | Not specified | **Keep**: the selected day's show count. It's derived, not invented. |
| 8 | Aired / Upcoming badges | On every card | Item shows cover, title, release time; weekly API has no `aired` (api.md §3.2) | **Today only**, from today's schedule `aired` (api.md §3.1), joined by slug. **No badge on other days** ([§4.3](#43-aired-and-upcoming), [Q2](#10-open-questions)). |
| 9 | Filter chips "All / Aired / Upcoming" | Always shown | Not in spec | **Shown only when the selected day has statuses** (today). Counts are real. |
| 10 | Empty state "No simulcasts found" + "Reset Filters" | Filter matches nothing | Empty states required (§3) | **Keep** for the filter case. A day with no shows gets a proposed variant without the button. |
| 11 | Sort button (clock icon) | Next to the filters | Not in spec | **Omit.** The grid is sorted by time; no other order is defined. |
| 12 | Episode labels | "EP 26", "SPEC" | Not listed for Schedule; weekly API has none | **Omit** ([Q3](#10-open-questions)). Home's search-per-show approach would cost ~57 extra requests per week view. |
| 13 | Ratings | "★ 8.9" | Not listed; not in the API | **Omit.** |
| 14 | Studio • genre line | "Pierrot • Action" | Not listed; not in the API | **Omit.** The title gets 2 lines (`minLines = 2`) to keep the card height. |
| 15 | Bookmark toggle on cards | Top-right of every poster | Bookmarks feature not built | **Omit** until the Bookmarks plan ([Q4](#10-open-questions)). |
| 16 | Grid columns | "2-column or 3-column responsive" | "a grid" | **2 / 3 / 4** for compact / medium / expanded (DESIGN.md: 3–4 on tablets). |
| 17 | Card corner radius | `rounded-lg` = 32 px | n/a. DESIGN.md: poster cards 24 px | **24 dp (`shapes.large`)**, as Home ([1-home.md §3](1-home.md#3-design-vs-spec) row 20). |
| 18 | Blur, shadows, glows, ambient backlight, hover/scale/vibration effects | Throughout | n/a | **Dropped.** Solid tonal layers, per DESIGN.md "Elevation & Depth" and Home's precedent. |
| 19 | Settings gear | Visible | Opens Settings (not built) | **Hidden** until Settings exists ([Q5](#10-open-questions)). |
| 20 | Pull-to-refresh | Not shown | Not noted in §3.2; api.md §7 says the weekly cache is "invalidated by pull-to-refresh" | **Add it** (it's the only way to bypass the 1 h cache). |
| 21 | Day strip | Sticky under the bar | n/a | **Sticky**, inside the top-bar slot. |
| 22 | Swipe between days | Not in design | Not in spec | **No pager in v1** ([Q8](#10-open-questions)). |
| 23 | Bottom navigation | Schedule active | 4 tabs | **Enable the Schedule tab.** All Shows and Bookmarks stay disabled. |
| 24 | Loading / error / day-empty / refresh / light / tablet | Not in design | Required (§3) | **Proposed in [§2.8](#28-states-not-in-the-design-all-proposed).** |

---

## 4. Data

### 4.1 Endpoints

Every call goes through the shared `HttpClient` (`text/html` JSON negotiation, User-Agent, `expectSuccess`) and sends `tz=<TimeZone.id>`.

| Call | Request | Fields used | Quirks to handle |
|---|---|---|---|
| Weekly schedule, [api.md §3.2](api.md#32-weekly-schedule) | `GET api/?f=schedule&tz=` (no `h`) | `schedule.<Weekday>[].title`, `page`, `image_url`, `time` | `schedule` is an object keyed by English weekday name: map the key to `DayOfWeek` ignoring case, drop unknown keys, never rely on key order. A missing day is an empty day. Items without `page`, `title` or a parseable `"HH:mm"` `time` are dropped. Placeholder `image_url` → `null`. Sort by time, then title. Decode defensively (S6). |
| Today's schedule, [api.md §3.1](api.md#31-todays-schedule) | `GET api/?f=schedule&h=true&tz=` | `page`, `aired` | **Best effort**: only the `aired` flag of each slug is used, to label today's cards. A failure means "no statuses". |

### 4.2 API gaps found live

Checked on **2026-11-18, 22:08–22:12 UTC** with `curl` (default User-Agent) for `tz=America/New_York`, `Asia/Tokyo`, `Asia/Kolkata` and an invalid zone. The docs step ([§8](#8-implementation-steps) step 19) writes these into `api.md` §3.2.

- **S1. Without `tz`, the weekly call returns `200` with an empty body**, like `f=latest` and `f=show` (Home's G6). We always send `tz`; an empty body decodes to `DomainError.InvalidData`.
- **S2. An unknown `tz` silently falls back to GMT.** `tz=Not/AZone` returned `200` with `"tz":"Etc\/GMT"` and GMT times. The response always echoes the zone it used in `tz`. The app ignores this in v1 ([Q10](#10-open-questions)); Android reports IANA ids, which the server accepts (Kolkata's +5:30 worked: Monday 07:30 New York = 18:00 Kolkata).
- **S3. Shape and volume.** All seven keys `Monday` … `Sunday` were present, in that order, with 3–14 items per day and **57 shows per week**, the same set in every zone. Each show appeared **once** per week. Items have exactly `title`, `page`, `image_url`, `time`. No `aired`, no HTML entities. The placeholder cover also appears here (*Kyouran Reijou Nia Liston*).
- **S4. Days are bucketed in the requested zone.** *Doumo, Suki na Hito ni…* is Monday 07:30 in New York and Monday 21:30 in Tokyo; Friday has 8 shows in New York and 3 in Tokyo.
- **S5. Today's bucket equals today's schedule.** The New York `Wednesday` bucket and `f=schedule&h=true&tz=America/New_York` had the same 9 slugs and times; Tokyo's `Thursday` bucket and its `h=true` call had the same 6 (5 `aired:true`, 1 `aired:false`). So today's `aired` can be joined onto the weekly bucket by slug.
- **S6. Items are sorted by `time` within a day**, with ties (two shows at 08:00) in no defined order. Sort by time, then title. Defensive decoding, not observed: PHP encodes an empty associative array as `[]`, so an empty `schedule` would arrive as `[]` rather than `{}`. `schedule` = `[]`, `null` or missing decodes to "no shows", and a day value that isn't an array decodes to an empty day.
- Headers: `content-type: text/html; charset=UTF-8`, `cf-cache-status: DYNAMIC`, as documented.

### 4.3 Aired and Upcoming

What the data supports, per day:

| Day | Data we have | Status shown |
|---|---|---|
| **Today** | Today's schedule `aired` per slug (api.md §3.1), the same signal Home uses for Upcoming | `aired == true` → **Aired**; `aired == false` → **Upcoming**; slug missing from today's schedule, or the call failed → none |
| **Earlier this week** | Only the weekday and time. The weekly schedule is a template, so it can't tell a delayed, skipped or finished episode from one that aired | **None** |
| **Later this week** | Same | **None** (every card would say "Upcoming", which tells the user nothing the date doesn't) |

Notes:
- Home additionally drops a show from Upcoming once its release is listed (G7 in [1-home.md §4.2](1-home.md#42-api-gaps-found-live)). Schedule doesn't fetch releases, so if `aired` lags the release, Schedule can briefly show "Upcoming" for a show Home lists under Latest. Accepted; it fixes itself on the next load.
- Statuses are as of the last load. They don't tick; pull-to-refresh or a new-day reload updates them.
- The rejected alternative, clock-based status (`time <= now` → Aired), is a guess for delayed shows; dropping the feature entirely is the other option in [Q2](#10-open-questions).

### 4.4 Derivation (domain)

`GetWeeklyScheduleUseCase(weekStart: DayOfWeek = MONDAY, forceRefresh: Boolean = false)`:

1. `timeZone = timeZoneProvider.current()`, `now = clock.now()`, `today = now.toLocalDateTime(timeZone).date`, `utcOffset = timeZone.offsetAt(now)`.
2. **In parallel** (`coroutineScope { async … }`):
   - `weekly = repository.getWeeklySchedule(timeZone, forceRefresh)` (**required**: failure fails the result with its `DomainError`)
   - `todaySchedule = repository.getTodaySchedule(timeZone)` (**best effort**: failure counts as empty)
3. First date of the week:
   ```kotlin
   val daysSinceStart = (today.dayOfWeek.isoDayNumber - weekStart.isoDayNumber).mod(DAYS_PER_WEEK)
   val firstDate = today.minus(daysSinceStart, DateTimeUnit.DAY)
   ```
4. `days = (0 until DAYS_PER_WEEK).map { firstDate.plus(it, DateTimeUnit.DAY) }`, each a `ScheduleDay(date, shows)`:
   - `shows = weekly[date.dayOfWeek].orEmpty().sortedWith(compareBy({ it.time }, { it.show.title }))`
   - on `date == today`: `shows.map { it.copy(aired = airedBySlug[it.show.slug]) }`, with `airedBySlug = todaySchedule.associate { it.show.slug to it.aired }`
   - on every other date, `aired` stays `null` (the weekly DTO has none)
5. Return `WeeklySchedule(today, timeZone, utcOffset, days)`.

### 4.5 Local data

None. No DataStore: the week start and time format use the spec defaults ([Q1](#10-open-questions)).

### 4.6 Caching and refresh

[api.md §7](api.md#7-client-guidelines): weekly schedule cached in memory for about 1 h, invalidated by pull-to-refresh.

- `ScheduleRepositoryImpl` (`@Single`, so it lives for the process) keeps the last successful weekly result with its zone id and fetch instant, behind a `Mutex`:
  ```kotlin
  override suspend fun getWeeklySchedule(timeZone: TimeZone, forceRefresh: Boolean) = mutex.withLock {
      val cached = cache?.takeIf { !forceRefresh && it.zoneId == timeZone.id && clock.now() - it.fetchedAt < WEEKLY_CACHE_TTL } // 1.hours
      if (cached != null) Result.success(cached.days)
      else safeApiCall { service.getWeeklySchedule(timeZone.id).toDomain() }
          .onSuccess { cache = CachedWeek(timeZone.id, clock.now(), it) }
  }
  ```
  Failures aren't cached. A zone change misses the cache. Concurrent callers wait for the lock, so they share one request.
- **Today's schedule is never cached:** `aired` changes during the day.
- **When Schedule fetches:**
  - once per `ScheduleViewModel` (`init { onIntent(Load) }`), using the cache
  - `Retry`, using the cache (it holds only successes)
  - pull-to-refresh, with `forceRefresh = true`
  - `ScreenStarted` when the local date changed since the last successful load, using the cache (the template doesn't change with the date; today's `aired` is refetched)
- **When it doesn't:** tab switches and rotation keep the ViewModel (`NavigationState` keeps each entry's ViewModel store). No polling, no timers.

---

## 5. Architecture

Root: `app/src/main/java/com/zc/bakamitai/`. Test root: `app/src/test/java/com/zc/bakamitai/`.

### 5.1 Shared code moved from `feature/home/`

`structure` §3: code moves to `common/` when a second feature needs it, and a feature never imports another feature's code. Every move keeps the declaration unchanged except its package (and the noted renames). Use `git mv` so history follows the file. After each group, `./gradlew compileDebugUnitTestKotlin` lists any import still pointing at the old package.

**Domain**

| From `feature/home/…` | To | Notes |
|---|---|---|
| `domain/model/ShowSlug.kt` | `common/domain/model/ShowSlug.kt` | |
| `domain/model/Show.kt` | `common/domain/model/Show.kt` | |
| `domain/model/ScheduledShow.kt` | `common/domain/model/ScheduledShow.kt` | KDoc: `aired` is set for today's schedule and, by Schedule, for today's weekly bucket |
| `domain/usecase/GetTodayDateUseCase.kt` | `common/domain/usecase/GetTodayDateUseCase.kt` | Picked up by `CommonModule`'s `@ComponentScan` |

**Data**

| From `feature/home/…` | To | Notes |
|---|---|---|
| `data/dto/ScheduleItemDTO.kt` | `common/data/dto/ScheduleItemDTO.kt` | |
| `data/dto/TodayScheduleDTO.kt` | `common/data/dto/TodayScheduleDTO.kt` | Schedule calls `h=true` too |
| `data/mapper/ScheduleItemDTOMapper.kt` | `common/data/mapper/ScheduleItemDTOMapper.kt` | Both functions (`ScheduleItemDTO.toDomain()`, `TodayScheduleDTO.toDomain()`) |
| `data/mapper/ImageUrlDTOMapper.kt` | `common/data/mapper/ImageUrlDTOMapper.kt` | `toCoverUrl()`. Home's `ReleaseDTOMapper` imports it from here |

**Presentation**

| From `feature/home/…` | To | Notes |
|---|---|---|
| `presentation/mapper/TimeUIModelMapper.kt` | `common/ui/mapper/TimeUIModelMapper.kt` | Still 12-hour. The Settings plan adds the format |
| private `UtcOffset.toSuffix()` in `component/todaysummary/mapper/TodaySummaryUIModelMapper.kt` | **new** `common/ui/mapper/UtcOffsetUIModelMapper.kt`: `fun UtcOffset.toUtcOffsetLabel(): UiText` | Rename the string `home_utc_offset` → `utc_offset` ("UTC%1$s"). `toTodaySummaryUIModel()` calls `utcOffset.toUtcOffsetLabel()` |
| `presentation/component/coverimage/CoverImageView.kt` (with `POSTER_ASPECT_RATIO`) | `common/ui/component/coverimage/CoverImageView.kt` | Preview moves too |
| private `HomeTopAppBar` in `presentation/HomeScreen.kt` | **new** `common/ui/component/toplevelappbar/TopLevelAppBarView.kt` | See signature below. `HomeScreen` calls it with `subtitle = home_app_bar_subtitle` and no `bottomContent`. Its behaviour doesn't change |

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopLevelAppBarView(
    subtitle: UiText,
    showSettingsAction: Boolean,
    onSettingsClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    bottomContent: (@Composable () -> Unit)? = null,   // e.g. Schedule's day strip; shares the bar's background
)
```

**Home files whose imports change** (main): `data/mapper/ReleaseDTOMapper.kt`, `data/remote/BroadcastService.kt`, `data/repository/BroadcastRepositoryImpl.kt`, `domain/model/Release.kt` and `UpcomingBroadcast.kt` (they used `Show` from the same package, so they **gain** an import), `domain/repository/BroadcastRepository.kt`, `domain/usecase/GetTodayBroadcastsUseCase.kt`, `domain/usecase/FindLatestEpisodeNumberUseCase.kt`, `presentation/HomeViewModel.kt`, `presentation/HomeScreen.kt` (top bar + `utc_offset` in the preview), `component/upcomingcard/UpcomingCardView.kt` and its mapper, `component/releaserow/ReleaseRowView.kt` and its mapper, `component/todaysummary/TodaySummaryView.kt` (preview) and its mapper.

**Tests**

| From `app/src/test/…/feature/home/…` | To |
|---|---|
| `data/mapper/ScheduleItemDTOMapperTest.kt` | `common/data/mapper/ScheduleItemDTOMapperTest.kt` |
| `data/mapper/ImageUrlDTOMapperTest.kt` | `common/data/mapper/ImageUrlDTOMapperTest.kt` |
| `domain/usecase/GetTodayDateUseCaseTest.kt` | `common/domain/usecase/GetTodayDateUseCaseTest.kt` |
| `presentation/mapper/TimeUIModelMapperTest.kt` | `common/ui/mapper/TimeUIModelMapperTest.kt` |
| the two offset cases of `component/todaysummary/mapper/TodaySummaryUIModelMapperTest.kt` | **new** `common/ui/mapper/UtcOffsetUIModelMapperTest.kt` (the counts case stays) |
| `show()` and `scheduled()` in `domain/HomeTestData.kt` | **new** `common/domain/model/ShowTestData.kt` (`object ShowTestData`). `HomeTestData.release()` uses it |

Tests whose imports change: `ReleaseDTOMapperTest`, `FakeBroadcastRepository`, `HomeViewModelTest` (`GetTodayDateUseCase`, `utc_offset`), `TodaySummaryUIModelMapperTest`, `KoinModulesTest`, plus any the compiler reports.

### 5.2 feature/schedule/data

| Path | | Responsibility |
|---|---|---|
| `feature/schedule/data/dto/WeeklyScheduleDTO.kt` | new | `@Serializable(with = WeeklyScheduleDTOSerializer::class) data class WeeklyScheduleDTO(val days: Map<String, List<ScheduleItemDTO>>)`, with a non-public decode-only serializer object in the same file (Home's `ReleaseListDTO` pattern; snippet below). Unknown root fields (`tz`) are ignored. |
| `feature/schedule/data/mapper/WeeklyScheduleDTOMapper.kt` | new | `fun WeeklyScheduleDTO.toDomain(): Map<DayOfWeek, List<ScheduledShow>>`. Keys → `DayOfWeek.entries.firstOrNull { it.name.equals(key.trim(), ignoreCase = true) }`, unknown keys dropped. Items through the common `ScheduleItemDTO.toDomain()` (`mapNotNull`). No sorting here. |
| `feature/schedule/data/remote/ScheduleService.kt` | new | `@Single`. `suspend fun getWeeklySchedule(timeZoneId: String): WeeklyScheduleDTO` (`f=schedule`, `tz`) and `suspend fun getTodaySchedule(timeZoneId: String): TodayScheduleDTO` (`f=schedule`, `h=true`, `tz`). Same constants and style as `BroadcastService`. No mapping. |
| `feature/schedule/data/repository/ScheduleRepositoryImpl.kt` | new | `@Single`, implements `ScheduleRepository`. Depends on `ScheduleService` and `kotlin.time.Clock`. The weekly cache in [§4.6](#46-caching-and-refresh); `getTodaySchedule` is `safeApiCall { service.getTodaySchedule(timeZone.id).toDomain() }` (common mapper). |

The one tricky detail, the defensive root decode:

```kotlin
override fun deserialize(decoder: Decoder): WeeklyScheduleDTO {
    val input = decoder as? JsonDecoder ?: throw SerializationException("WeeklyScheduleDTO is JSON-only")
    val root = input.decodeJsonElement() as? JsonObject ?: throw SerializationException("Unexpected weekly schedule root")
    val days = (root[SCHEDULE_KEY] as? JsonObject).orEmpty()          // `[]`, null or missing: no shows (S6)
        .mapValues { (_, items) -> (items as? JsonArray).orEmpty().map { input.json.decodeFromJsonElement(ScheduleItemDTO.serializer(), it) } }
    return WeeklyScheduleDTO(days)
}
```

### 5.3 feature/schedule/domain (pure Kotlin + kotlinx-datetime)

| Path | | Responsibility |
|---|---|---|
| `domain/model/ScheduleDay.kt` | new | `data class ScheduleDay(val date: LocalDate, val shows: List<ScheduledShow>)` with `val dayOfWeek: DayOfWeek get() = date.dayOfWeek` and `val hasStatuses: Boolean get() = shows.any { it.aired != null }`. |
| `domain/model/WeeklySchedule.kt` | new | `data class WeeklySchedule(val today: LocalDate, val timeZone: TimeZone, val utcOffset: UtcOffset, val days: List<ScheduleDay>)`. `days` holds 7 entries in tab order. |
| `domain/repository/ScheduleRepository.kt` | new | `suspend fun getWeeklySchedule(timeZone: TimeZone, forceRefresh: Boolean): Result<Map<DayOfWeek, List<ScheduledShow>>>` (cached; a missing day is absent) and `suspend fun getTodaySchedule(timeZone: TimeZone): Result<List<ScheduledShow>>`. Failures are `DomainError`. |
| `domain/usecase/GetWeeklyScheduleUseCase.kt` | new | `@Factory`. `suspend operator fun invoke(weekStart: DayOfWeek = DEFAULT_WEEK_START, forceRefresh: Boolean = false): Result<WeeklySchedule>` implements [§4.4](#44-derivation-domain). Dependencies: `ScheduleRepository`, `Clock`, `TimeZoneProvider`. `DEFAULT_WEEK_START = DayOfWeek.MONDAY` (context.md §3.6 default; the Settings plan passes the setting). |

`GetTodayDateUseCase` comes from `common/domain/usecase/` ([§5.1](#51-shared-code-moved-from-featurehome)).

### 5.4 feature/schedule/presentation

Component placement follows `structure` §1.1. `DayChipView`, `DayHeaderView` and `ScheduleCardView` are each rendered by the Screen only; the status badge is rendered only inside the card, so it nests under it.

| Path | | Responsibility |
|---|---|---|
| `presentation/model/StatusFilter.kt` | new | `enum class StatusFilter { ALL, AIRED, UPCOMING }`. Read by the State, the Intent and `DayHeaderView`, so it sits at screen level. A plain value enum, so no `UIModel` suffix. |
| `presentation/model/ScheduleEmptyUIModel.kt` | new | `@Immutable data class ScheduleEmptyUIModel(val title: UiText, val message: UiText, val showResetAction: Boolean)` |
| `presentation/model/ScheduleDayUIModel.kt` | new | `@Immutable data class ScheduleDayUIModel(val header: DayHeaderUIModel, val cards: ImmutableList<ScheduleCardUIModel>, val empty: ScheduleEmptyUIModel?)`: the selected day, filtered. `empty != null` exactly when `cards` is empty. |
| `presentation/mapper/DayOfWeekUIModelMapper.kt` | new | `fun DayOfWeek.toDayName(): UiText` (`day_monday`…) and `fun DayOfWeek.toShortDayName(): UiText` (`day_short_monday`…), each a `when` over the 7 days returning `UiText.StringResource`. |
| `presentation/mapper/ScheduleDayUIModelMapper.kt` | new | `fun ScheduleDay.toUIModel(utcOffset: UtcOffset, filter: StatusFilter): ScheduleDayUIModel`. The filter applies **only when `hasStatuses`**: `ALL` → all; `AIRED` → `aired == true`; `UPCOMING` → `aired == false`. `empty`: no shows at all → day-empty model (no reset); shows exist but none match → filter-empty model (reset); otherwise `null`. Uses the header and card mappers. |
| `component/daychip/model/DayChipUIModel.kt` | new | `@Immutable data class DayChipUIModel(val dayOfWeek: DayOfWeek, val weekdayLabel: UiText, val dateLabel: UiText, val contentDescription: UiText, val isToday: Boolean)` |
| `component/daychip/mapper/DayChipUIModelMapper.kt` | new | `fun ScheduleDay.toDayChipUIModel(today: LocalDate): DayChipUIModel` and `fun WeeklySchedule.toDayChipUIModels(): ImmutableList<DayChipUIModel>` |
| `component/daychip/DayChipView.kt` | new | `DayChipView(model, selected: Boolean, onClick: () -> Unit, modifier)` (§2.2) + `DayChipSkeletonView(modifier)` (same size and shape, one `SkeletonBlock`), with `@AdaptivePreviews` |
| `component/dayheader/model/DayHeaderUIModel.kt` | new | `@Immutable data class DayHeaderUIModel(val title: UiText, val subtitle: UiText, val countLabel: UiText, val filters: ImmutableList<StatusFilterChipUIModel>)` |
| `component/dayheader/model/StatusFilterChipUIModel.kt` | new | `@Immutable data class StatusFilterChipUIModel(val filter: StatusFilter, val label: UiText)` |
| `component/dayheader/mapper/DayHeaderUIModelMapper.kt` | new | `fun ScheduleDay.toDayHeaderUIModel(utcOffset: UtcOffset): DayHeaderUIModel`. `filters` is empty unless `hasStatuses`. `subtitle = UiText.StringResource(schedule_time_zone, listOf(utcOffset.toUtcOffsetLabel()))` (a `UiText` argument, which `UiText` resolves). |
| `component/dayheader/DayHeaderView.kt` | new | `DayHeaderView(model, selectedFilter: StatusFilter, onFilterClick: (StatusFilter) -> Unit, modifier)` (§2.3, §2.4) + `DayHeaderSkeletonView(modifier)`, with `@AdaptivePreviews` |
| `component/schedulecard/model/ScheduleCardUIModel.kt` | new | `@Immutable data class ScheduleCardUIModel(val key: String, val slug: String, val title: UiText, val coverUrl: String?, val timeLabel: UiText, val status: StatusBadgeUIModel?)` |
| `component/schedulecard/mapper/ScheduleCardUIModelMapper.kt` | new | `fun ScheduledShow.toScheduleCardUIModel(): ScheduleCardUIModel`. `key = "${show.slug.value}@$time"`, `timeLabel = time.toTimeLabel()` (common) |
| `component/schedulecard/ScheduleCardView.kt` | new | `ScheduleCardView(model, onClick: (() -> Unit)?, modifier)` (§2.5) + `ScheduleCardSkeletonView(modifier)` (poster block + two `titleMedium` lines), with `@AdaptivePreviews` |
| `component/schedulecard/component/statusbadge/model/StatusBadgeUIModel.kt` | new | `@Immutable data class StatusBadgeUIModel(val label: UiText, val isUpcoming: Boolean)` |
| `component/schedulecard/component/statusbadge/mapper/StatusBadgeUIModelMapper.kt` | new | `fun ScheduledShow.toStatusBadgeUIModel(): StatusBadgeUIModel?`: `true` → Aired, `false` → Upcoming, `null` → `null` |
| `component/schedulecard/component/statusbadge/StatusBadgeView.kt` | new | `StatusBadgeView(model, modifier)` (§2.6), with `@AdaptivePreviews` |
| `presentation/ScheduleState.kt` | new | MVI state (below) |
| `presentation/ScheduleIntent.kt` | new | Intents (below) |
| `presentation/ScheduleEffect.kt` | new | Effects (below) |
| `presentation/ScheduleViewModel.kt` | new | `@KoinViewModel class ScheduleViewModel(getWeeklySchedule: GetWeeklyScheduleUseCase, getTodayDate: GetTodayDateUseCase)`. Keeps `loadJob` and the last `WeeklySchedule` private (re-reduced on day and filter changes, never exposed). Maps with the mappers above and reduces with `_state.update`. |
| `presentation/ScheduleRoute.kt` | new | `ScheduleRoute(onOpenDetails: ((slug: String) -> Unit)?, onOpenSettings: (() -> Unit)?, modifier, viewModel = koinViewModel())`. Same shape as `HomeRoute`: `collectAsStateWithLifecycle()`, `LifecycleStartEffect(viewModel) { viewModel.onIntent(ScheduleIntent.ScreenStarted); onStopOrDispose {} }`, `ObserveAsEvents` (`OpenDetails` → `onOpenDetails?.invoke(slug)`, `OpenSettings` → `onOpenSettings?.invoke()`, `ShowError` → snackbar), owns the `SnackbarHostState`, passes `canOpenShow` and `showSettingsAction`. |
| `presentation/ScheduleScreen.kt` | new | `ScheduleScreen(state, onIntent, modifier, canOpenShow: Boolean = true, showSettingsAction: Boolean = true, snackbarHostState = remember { SnackbarHostState() })`. Private: `ScheduleLayout` (window-size values), `DayStrip`, `ScheduleGrid` (shared by content and skeleton), `ScheduleContent`, `ScheduleSkeleton`, `ScheduleEmptyView`. `@AdaptivePreviews` with a `PreviewParameterProvider<ScheduleState>`. |

`ScheduleScreen` layout:

- `Scaffold(modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = { TopLevelAppBarView(subtitle = schedule_app_bar_subtitle, …, bottomContent = if (state.hasContent) { { DayStrip(…) } } else null) }, snackbarHost = …)`. Keep Scaffold's **default** content insets (Home QA issue 1: the rail doesn't consume the bottom inset).
- Body `when`:
  - `state.day != null` → `PullToRefreshBox(isRefreshing, onRefresh = { onIntent(Refresh) })` → `key(state.selectedDay) { ScheduleContent(…) }`
  - `state.error != null` → `ErrorView`
  - else → `ScheduleSkeleton`
- `ScheduleContent` = `ScheduleGrid(layout, gridState = rememberLazyGridState())` with: `item(key = "header", span = full, contentType = "header") { DayHeaderView(…) }`, then either `item(key = "empty", span = full, contentType = "empty") { ScheduleEmptyView(…) }` or `items(day.cards, key = { it.key }, contentType = { "card" }) { ScheduleCardView(…) }`.
- `ScheduleLayout(windowSizeClass)`: `margin` (`margin` / `marginTablet` from medium) and `columns` (2 / 3 / 4), read with `isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND / WIDTH_DP_EXPANDED_LOWER_BOUND)`, as `HomeLayout` does. Never stored in state.
- Click lambdas are adapted once: `remember(onIntent) { { slug: String -> onIntent(ScheduleIntent.ShowClicked(slug)) } }`, likewise for days and filters. Cards get `onClick = if (canOpenShow) { { onShowClick(card.slug) } } else null`.

### 5.5 Navigation

| Path | | Responsibility |
|---|---|---|
| `feature/schedule/presentation/navigation/ScheduleNavKey.kt` | new | `@Serializable data object ScheduleNavKey : NavKey`. A plain top-level page. |
| `feature/schedule/presentation/navigation/ScheduleNavigation.kt` | new | `fun EntryProviderScope<NavKey>.scheduleEntry(onOpenDetails: ((slug: String) -> Unit)?, onOpenSettings: (() -> Unit)?)` → `entry<ScheduleNavKey> { ScheduleRoute(onOpenDetails, onOpenSettings) }`. No `Navigator.navigateToSchedule()` yet: no feature opens Schedule (add it with Home's "Timeline" follow-up, [Q12](#10-open-questions)). |
| `app/navigation/TopLevelDestination.kt` | modify | `SCHEDULE(key = ScheduleNavKey, …)`. Icons and label unchanged. |
| `app/navigation/AppEntryProvider.kt` | modify | Add `scheduleEntry(onOpenDetails = null, onOpenSettings = null)`; update the KDoc. |

`AppNavigation` needs no change: `topLevelRoutes` is built from the destinations' keys, and `NavigationState` gives the new key its own back stack and ViewModel store.

### 5.6 di

| Path | | Responsibility |
|---|---|---|
| `feature/schedule/di/ScheduleModule.kt` | new | `@Module @ComponentScan("com.zc.bakamitai.feature.schedule") class ScheduleModule` |
| `app/di/AppModule.kt` | modify | `@Module(includes = [CommonModule::class, HomeModule::class, ScheduleModule::class])` |

### 5.7 Resources

`res/values/strings.xml` (modify): rename `home_utc_offset` → `utc_offset`, and add (a new `<!-- Schedule (feature/schedule) -->` and `<!-- Days -->` section):

- `schedule_app_bar_subtitle` "SCHEDULE"
- `schedule_time_zone` "Times in %1$s" (the title is the `day_*` string itself)
- `schedule_filter_all` "All (%1$d)", `schedule_filter_aired` "Aired (%1$d)", `schedule_filter_upcoming` "Upcoming (%1$d)"
- `schedule_status_aired` "Aired", `schedule_status_upcoming` "Upcoming"
- `schedule_filter_empty_title` "No simulcasts found", `schedule_filter_empty_message` "There are no shows matching this filter for the selected broadcast day.", `schedule_filter_reset` "Reset Filters"
- `schedule_day_empty_title` "Nothing scheduled", `schedule_day_empty_message` "No shows are scheduled for %1$s."
- `cd_schedule_day` "%1$s %2$s", `cd_schedule_day_today` "%1$s %2$s, today"
- `day_monday` … `day_sunday` ("Monday" …), `day_short_monday` … `day_short_sunday` ("MON" …, stored uppercase). Neutral names, so Settings can reuse them for *First day of week*.
- Plural `schedule_show_count`: one "%d Show Airing", other "%d Shows Airing".

### 5.8 MVI contract

```kotlin
@Immutable
data class ScheduleState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: ErrorUIModel? = null,                                  // full-screen error (no content yet)
    val days: ImmutableList<DayChipUIModel> = persistentListOf(),     // tab order; empty until a load succeeds
    val selectedDay: DayOfWeek? = null,
    val statusFilter: StatusFilter = StatusFilter.ALL,                // applies only to a day with statuses
    val day: ScheduleDayUIModel? = null,                              // the selected day, filtered
) {
    val hasContent: Boolean get() = day != null
}

sealed interface ScheduleIntent {
    data object Load : ScheduleIntent                                 // from init
    data object Retry : ScheduleIntent                                // ErrorView button
    data object Refresh : ScheduleIntent                              // pull-to-refresh (bypasses the cache)
    data object ScreenStarted : ScheduleIntent                        // lifecycle START: reload on a new local day
    data class DaySelected(val day: DayOfWeek) : ScheduleIntent
    data class FilterSelected(val filter: StatusFilter) : ScheduleIntent
    data object ResetFilterClicked : ScheduleIntent
    data class ShowClicked(val slug: String) : ScheduleIntent
    data object SettingsClicked : ScheduleIntent
}

sealed interface ScheduleEffect {
    data class OpenDetails(val slug: String) : ScheduleEffect
    data object OpenSettings : ScheduleEffect
    data class ShowError(val error: ErrorUIModel) : ScheduleEffect   // refresh failed while content is shown
}
```

**Reducer rules:**

- **`Load` / `Retry` / `Refresh`:** ignored while `loadJob?.isActive`. Otherwise `isLoading = !hasContent`, `isRefreshing = hasContent`, `error = null`, then `getWeeklySchedule(forceRefresh = intent is Refresh)`.
- **Success:**
  - flags off; the `WeeklySchedule` is kept privately
  - `selectedDay` = today's `dayOfWeek` on the first success and whenever `weekly.today` differs from the previous load's `today`; otherwise the current selection
  - `statusFilter` unchanged
  - `days = weekly.toDayChipUIModels()`, `day = <selected ScheduleDay>.toUIModel(weekly.utcOffset, statusFilter)` (`firstOrNull`, no `!!`)
- **Failure without content:** `error = throwable.toErrorUIModel()`. **With content:** flags off, then `ShowError`.
- **`ScreenStarted`:** if a load succeeded before, no load is running, and `getTodayDate()` differs from the last `weekly.today`, run `Load` (cached weekly, fresh today's schedule).
- **`DaySelected(day)`:** ignored without content. Otherwise `selectedDay = day` and re-reduce `day`.
- **`FilterSelected(filter)`:** `statusFilter = filter` and re-reduce `day`. **`ResetFilterClicked`:** the same with `ALL`.
- **`ShowClicked` / `SettingsClicked`:** send the matching effect.

---

## 6. Dependencies

### Libraries

**No new library.** Everything is in `libs.versions.toml` and wired into `:app`: kotlinx-datetime, kotlinx-coroutines (`Mutex`), kotlinx-collections-immutable, Ktor, kotlinx.serialization, Compose Material 3 (`FilterChip`, `PullToRefreshBox`), Material 3 Adaptive, `material-icons-extended` (BOM 1.7.8, every icon above checked present), Coil, Koin, Navigation 3. Opt-ins (`ExperimentalMaterial3Api` for `TopAppBar` / `PullToRefreshBox`, `ExperimentalTime` if asked) go on the smallest declaration.

### Earlier plans and shared code

- **Needs (exists):** `HttpClient`, `safeApiCall`, `DomainError`, `Clock`, `TimeZoneProvider`, `ErrorView`, `SkeletonView` and friends, `UiText`, `PluralResource`, `ObserveAsEvents`, `@AdaptivePreviews`, `Navigator` / `NavigationState`, the navigation shell.
- **Moves from Home** ([§5.1](#51-shared-code-moved-from-featurehome)): everything listed in [1-home.md §6](1-home.md#what-it-unblocks-and-what-later-plans-must-move), plus `TodayScheduleDTO`, `GetTodayDateUseCase` and the UTC-offset label, which Schedule also needs.

### What it unblocks

- **Settings plan:** passes `weekStart` from *First day of week* to `GetWeeklyScheduleUseCase` (via an observed setting in `ScheduleViewModel`), adds the *Time format* parameter to `common/ui/mapper/TimeUIModelMapper.kt` (one place for Home and Schedule), wires `onOpenSettings` for both screens, and may reuse `day_*` strings.
- **Details plan:** passes `onOpenDetails = navigator::navigateToDetails` to `scheduleEntry` too.
- **Bookmarks plan:** may add card bookmark toggles on Schedule and Home ([Q4](#10-open-questions)).
- **All Shows plan:** reuses `TopLevelAppBarView`.
- **Home follow-up:** "Timeline >" can select the Schedule tab ([Q12](#10-open-questions)).

---

## 7. Testing

Unit tests mirror the main package under `app/src/test/java/com/zc/bakamitai/`. Tools: JUnit4, `kotlin.test`, `runTest`, Turbine, Ktor `MockEngine` through `createHttpClient(appJson, "test", engine)`, hand-written fakes.

### Fixtures

| File | Source | Must contain |
|---|---|---|
| `schedule_week.json` (new) | `f=schedule&tz=America/New_York` | All 7 day keys with 1–3 items each, trimmed from a live response: the placeholder-cover item (*Kyouran Reijou Nia Liston*, Tuesday) and two items at the same time (Wednesday 08:00). Add `SubsPleaseFixtures.SCHEDULE_WEEK`. |
| `schedule_today.json` (existing) | `f=schedule&h=true` | Reused for `ScheduleServiceTest.getTodaySchedule`. |

Edge cases (`[]` schedule, missing `schedule`, a non-array day, unknown day keys, missing `page`) are inline JSON strings in the tests, not fixtures.

### Test classes

| Class | Covers |
|---|---|
| Moved tests ([§5.1](#51-shared-code-moved-from-featurehome)) | Unchanged cases, new packages. `UtcOffsetUIModelMapperTest`: +9h → "+9", −5h → "-5", +5:30 → "+5:30", 0 → "" with `utc_offset`. |
| `common/domain/model/ShowTestData.kt` | **Test helper.** `show(slug, title, coverUrl)`, `scheduled(slug, time, aired, title)`. |
| `feature/schedule/data/dto/WeeklyScheduleDTOTest` | The fixture decodes 7 keys with their items. `"schedule":[]`, `"schedule":null` and a missing `schedule` → empty map (S6). A day value `{}` or `"x"` → empty list. Unknown root fields (`tz`) and item fields are ignored. A `[]` root throws `SerializationException`. |
| `feature/schedule/data/mapper/WeeklyScheduleDTOMapperTest` | `"Monday"`…`"Sunday"` and `"monday"` → `DayOfWeek`; `"Funday"` dropped. Items without `page`, `title` or a valid `time` dropped. `aired` is `null`. Placeholder → `null` cover, relative → absolute. A day missing from the DTO is absent from the map. |
| `feature/schedule/data/remote/ScheduleServiceTest` | `getWeeklySchedule` sends `GET /api/` with `f=schedule` and `tz`, and **no `h`**; `getTodaySchedule` sends `h=true`. Fixtures served as `text/html` decode. |
| `feature/schedule/data/repository/ScheduleRepositoryImplTest` | `MockEngine` counting requests, `FixedClock`. Second call within 1 h → no request. After 1 h → one request. `forceRefresh` → request. Another zone → request. A failure isn't cached (next call requests). Two concurrent calls → one request. 500 → `Server`; empty body (S1) → `InvalidData`; `tz` is `timeZone.id`. `getTodaySchedule` is never cached. |
| `feature/schedule/domain/repository/FakeScheduleRepository.kt` | **Fake.** Settable `weekly` / `today` results, a log of calls with `forceRefresh` and zone, an optional `gate: CompletableDeferred<Unit>`. |
| `feature/schedule/domain/usecase/GetWeeklyScheduleUseCaseTest` | See below. |
| `feature/schedule/presentation/mapper/DayOfWeekUIModelMapperTest` | Each `DayOfWeek` → its `day_*` and `day_short_*` resource. |
| `feature/schedule/presentation/mapper/ScheduleDayUIModelMapperTest` | Filters: `ALL` / `AIRED` / `UPCOMING` on a day with statuses; any filter on a day without statuses shows every card. `empty`: no shows → day-empty (no reset, day name argument); filter matches nothing → filter-empty with reset; otherwise `null`. |
| `…/component/daychip/mapper/DayChipUIModelMapperTest` | `dateLabel` "16" and "9" (unpadded); `isToday`; content description with and without ", today"; 7 chips in `days` order. |
| `…/component/dayheader/mapper/DayHeaderUIModelMapperTest` | Title with the day name; plural count before filtering; subtitle wraps the offset label; filters present only with statuses, with counts (All 6 / Aired 5 / Upcoming 1); a show with `aired == null` on today counts in All only. |
| `…/component/schedulecard/mapper/ScheduleCardUIModelMapperTest` | Key `slug@time`; 12-hour time label; null cover passes through; status from `aired`. |
| `…/component/schedulecard/component/statusbadge/mapper/StatusBadgeUIModelMapperTest` | `true` → Aired, `false` → Upcoming (`isUpcoming`), `null` → `null`. |
| `feature/schedule/presentation/ScheduleViewModelTest` | See below. |
| `app/navigation/NavKeySerializationTest` | **Modify:** add `ScheduleNavKey` to the sample list (the top-level-key test requires it). |
| `app/di/KoinModulesTest` | **Modify:** `verify()` passes with `ScheduleModule`; resolves `ScheduleRepository` as `ScheduleRepositoryImpl`, `GetWeeklyScheduleUseCase`, and `GetTodayDateUseCase` from its new package. |

`GetWeeklyScheduleUseCaseTest` (`FixedClock`, `FixedTimeZoneProvider`, `FakeScheduleRepository`):
- `weekStart = MONDAY`, now Thu 2026-11-19 12:00 in New York → 7 days Mon 16 … Sun 22 in that order. `weekStart = SUNDAY` → Sun 15 … Sat 21.
- Today on the first day (Mon, MONDAY start) → today first; today on the last day (Sun, MONDAY start) → Mon 16 … Sun 22 with today last.
- Device zone decides today: `2026-11-19T02:30Z` is Wed 18 in `America/New_York` and Thu 19 in `Asia/Tokyo`; `today`, the week and `utcOffset` follow the zone.
- Shows sorted by time then title; days missing from the map are empty.
- Today's shows get `aired` from today's schedule by slug; a slug missing there → `null`; other days stay `null`.
- Today's schedule failure → success with all `aired == null`. Weekly failure → that `DomainError`.
- `forceRefresh` and the zone are passed to the repository; both calls start before either completes (gate).

`ScheduleViewModelTest` (`MainDispatcherRule`, Turbine, real use cases over `FakeScheduleRepository`):
- `init` → `isLoading` → content: 7 chips, `selectedDay` = today, header for today, `forceRefresh = false`.
- Failure → `error`; `Retry` → content.
- `DaySelected(FRIDAY)` → Friday's cards, no filters; `FilterSelected(UPCOMING)` there changes `statusFilter` but not the cards; back to today → only upcoming cards.
- `FilterSelected(UPCOMING)` with no upcoming show → `empty` with reset; `ResetFilterClicked` → `ALL`, all cards.
- `Refresh` → `isRefreshing` → content, selection kept, `forceRefresh = true` logged.
- `Refresh` failure with content → content kept, flags off, `ShowError`.
- `ScreenStarted` on the same day → no repository call. After the clock moves to the next day → one reload with `forceRefresh = false`, and `selectedDay` becomes the new today even if another day was selected.
- A second `Refresh` while loading → ignored (one call).
- `ShowClicked("x")` → `OpenDetails("x")`; `SettingsClicked` → `OpenSettings`.

**Every quirk in [§4](#4-data) has a test:**
- no `tz` / empty body (S1): RepositoryImplTest
- `[]` / missing `schedule`, non-array day (S6): WeeklyScheduleDTOTest
- weekday keys, unknown keys, key order (S3): WeeklyScheduleDTOMapperTest, DTO test with shuffled keys
- zone-dependent buckets and "today" (S4): GetWeeklyScheduleUseCaseTest
- today join by slug (S5), missing slug, failed today call: GetWeeklyScheduleUseCaseTest
- unsorted ties (S6): GetWeeklyScheduleUseCaseTest
- placeholder cover, missing `page`: WeeklyScheduleDTOMapperTest (and the moved common tests)
- 1 h cache, forced refresh, zone miss: RepositoryImplTest
- S2 (unknown zone → GMT) is documented only ([Q10](#10-open-questions)).

### Compose previews

`@AdaptivePreviews` (light/dark × compact/expanded) inside `BakaMitaiTheme`, private sample data:

- `ScheduleScreen` provider: loading; today with statuses (filter All); today filtered to Upcoming; another day (no statuses); filter-empty; day-empty; refreshing; error.
- `DayChipView`: plain, today, selected, selected today; plus `DayChipSkeletonView`.
- `DayHeaderView`: with filters (each selected once), without filters, a long day name; plus `DayHeaderSkeletonView`.
- `ScheduleCardView`: Aired, Upcoming, no status, long 2-line title, null cover, clickable and not; plus `ScheduleCardSkeletonView`.
- `StatusBadgeView`: both.
- `TopLevelAppBarView`: with and without the settings action, with a `bottomContent` sample.
- `CoverImageView`: the moved preview.

---

## 8. Implementation steps

### Phase 0: shared moves (Home must behave exactly as before)

1. [x] **Domain moves:** `ShowSlug`, `Show`, `ScheduledShow` → `common/domain/model/`; `GetTodayDateUseCase` → `common/domain/usecase/`; `ShowTestData`; move `GetTodayDateUseCaseTest`; fix Home's imports.
   **Done when:** `./gradlew testDebugUnitTest` passes and the `architecture` §1 domain-purity grep prints nothing.
2. [x] **Data moves:** `ScheduleItemDTO`, `TodayScheduleDTO` → `common/data/dto/`; `ScheduleItemDTOMapper`, `ImageUrlDTOMapper` → `common/data/mapper/`, with their tests.
   **Done when:** `./gradlew testDebugUnitTest` passes and `grep -rn "feature.home.data.dto.ScheduleItemDTO\|feature.home.data.mapper.toCoverUrl" app/src` prints nothing.
3. [x] **Presentation moves:** `TimeUIModelMapper` → `common/ui/mapper/`; new `UtcOffsetUIModelMapper` (rename `home_utc_offset` → `utc_offset`, update `TodaySummaryUIModelMapper`, both previews and the tests); `CoverImageView` → `common/ui/component/coverimage/`; `HomeTopAppBar` → `TopLevelAppBarView` with the `bottomContent` slot and the shared animated background (§2.1).
   **Done when:** tests pass, `./gradlew detekt` is clean, and on an emulator Home looks and scrolls as before (the bar tints to `surfaceContainer` when the list scrolls under it).
4. [x] **Phase 0 gate:**
   ```bash
   ./gradlew testDebugUnitTest
   ```
   ```bash
   ./gradlew detekt
   ```
   ```bash
   ./gradlew assembleDebug
   ```
   Plus the `structure` §7 checks (`common/` imports no `feature`).
   **Done when:** all pass, and this prints nothing:
   ```bash
   find app/src -path "*feature/home*" \( -name "Show*.kt" -o -name "ScheduledShow.kt" -o -name "ScheduleItemDTO*.kt" -o -name "TodayScheduleDTO.kt" -o -name "ImageUrlDTO*.kt" -o -name "TimeUIModelMapper*.kt" -o -name "CoverImageView.kt" -o -name "GetTodayDateUseCase*.kt" \)
   ```

### Phase 1: non-UI

5. [x] **Fixture and DTO:** capture and trim `schedule_week.json`; `WeeklyScheduleDTO` with its serializer; `WeeklyScheduleDTOTest`.
   **Done when:** `WeeklyScheduleDTOTest` passes.
6. [x] **Mapper:** `WeeklyScheduleDTOMapper` + test.
   **Done when:** `WeeklyScheduleDTOMapperTest` passes.
7. [x] **Service:** `ScheduleService` + `ScheduleServiceTest`.
   **Done when:** both methods assert their query parameters and decode the fixtures.
8. [x] **Domain:** `ScheduleDay`, `WeeklySchedule`, `ScheduleRepository`.
   **Done when:** it compiles and the domain-purity grep prints nothing.
9. [x] **Repository:** `ScheduleRepositoryImpl` with the cache + `ScheduleRepositoryImplTest`.
   **Done when:** every repository case in §7 passes.
10. [x] **Use case:** `GetWeeklyScheduleUseCase`, `FakeScheduleRepository`, `GetWeeklyScheduleUseCaseTest`.
    **Done when:** every use-case case in §7 passes.
11. [x] **DI:** `ScheduleModule`, `AppModule`, `KoinModulesTest`.
    **Done when:** `./gradlew testDebugUnitTest --tests "*KoinModulesTest"` passes.
12. [x] **Phase 1 gate:** `./gradlew testDebugUnitTest`, `./gradlew detekt`, `./gradlew assembleDebug`, the `tech-stack` verification greps and the `architecture` §1 greps.
    **Done when:** all pass with zero Detekt findings.

### Phase 2: UI

13. [x] **Resources:** the strings and plural in [§5.7](#57-resources).
    **Done when:** it builds and the `conventions` §2.5 grep prints nothing new.
14. [x] **Presentation models and mappers:** `StatusFilter`, `ScheduleEmptyUIModel`, `ScheduleDayUIModel`, the component models, all mappers, and their tests.
    **Done when:** the mapper tests in §7 pass.
15. [x] **MVI and ViewModel:** `ScheduleState`, `ScheduleIntent`, `ScheduleEffect`, `ScheduleViewModel`, `ScheduleViewModelTest`.
    **Done when:** every ViewModel case in §7 passes.
16. [x] **Components:** `StatusBadgeView`, `ScheduleCardView`, `DayChipView`, `DayHeaderView`, each with its skeleton (where listed) and `@AdaptivePreviews`.
    **Done when:** the previews render and `./gradlew detekt` is clean.
17. **Screen and Route:** `ScheduleScreen` (strip in the top-bar slot, grid helper, `key(selectedDay)`, skeleton, empty view, `PullToRefreshBox`, snackbar host) with its preview provider, and `ScheduleRoute`.
    **Done when:** all eight preview states render in light and dark, compact and expanded.
18. [x] **Navigation:** `ScheduleNavKey`, `ScheduleNavigation`, `TopLevelDestination.SCHEDULE.key`, `AppEntryProvider`, `NavKeySerializationTest`.
    **Done when:** `android run --apks=app/build/outputs/apk/debug/app-debug.apk` shows an enabled Schedule tab that opens on today.
19. [x] **Docs** (done 2026-11-19, separately from the implementor):
    - **`docs/api.md`:** §3.2 gains S1–S6 ([§4.2](#42-api-gaps-found-live)); §2's table lists Schedule as a user of today's schedule; §1 notes the weekly call's empty body without `tz`.
    - **`docs/context.md`:** §3.2 (dates under the day tabs, today-only Aired/Upcoming with filters, pull-to-refresh); §4.2 (the today's-schedule join, the week's dates, Monday/12-hour until Settings); §6 (the shared pieces now in `common/`).
    - **`CLAUDE.md`** "Current state" and **`structure` §7** "Current repo state": Schedule is the second feature and enabled; the shared pieces moved to `common/`; only All Shows and Bookmarks stay disabled.

    **Done when:** the docs describe the shipped behaviour.
20. **Final verification**, in order:
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
    Then the `architecture` §1 greps, the `structure` §7 checks, the `tech-stack` §1 verification greps and the `conventions` §2.5 grep, and [§9](#9-acceptance-criteria) on an emulator.

    **Done when:** tests and Detekt pass with zero findings, the `unstable` grep shows nothing for Schedule's and Home's Screens and Views (the known `ObserveAsEvents` line excepted), every grep prints nothing, and every acceptance criterion is checked.

---

## 9. Acceptance criteria

**Navigation:**
- [ ] The Schedule tab is enabled; All Shows and Bookmarks stay disabled. Tapping Schedule shows "BakaMitai" / "SCHEDULE" and no settings icon.
- [ ] Back from Schedule returns to Home; back on Home leaves the app.
- [ ] Switching Home ↔ Schedule doesn't refetch (Logcat `Ktor` lines in a debug build) and keeps the selected day.

**Loading:**
- [ ] First open shows a shimmering skeleton (chip strip, header, card grid) in the content's layout, then the content. No spinner.

**Day strip:**
- [ ] Seven chips Monday → Sunday with this week's dates, matching the device calendar.
- [ ] Today's chip is selected on open, scrolled into view on a phone (also on a Sunday), and keeps its dot when another day is selected.
- [ ] The strip stays pinned while the grid scrolls; bar and strip tint together.

**Content:**
- [ ] Each day's cards match `f=schedule&tz=<device zone>` for that weekday (same shows, sorted by time), with the cover (or the neutral placeholder), a title of up to 2 lines and a 12-hour time ("5:00 PM").
- [ ] The header shows "<Day>", "Times in UTC±N" for the device zone, and "N Shows Airing" equal to the day's card count.
- [ ] Only today's cards have Aired / Upcoming badges, matching `aired` in `f=schedule&h=true` for the same zone; Upcoming badges have a pulsing dot and a tertiary alarm time chip.
- [ ] Only today has the filter chips; their counts are right; "Upcoming" shows only upcoming cards; when nothing matches, "No simulcasts found" with **Reset Filters** appears and the button restores All.
- [ ] Other days show no badges and no filter chips.

**Behaviour:**
- [ ] Pull-to-refresh shows the indicator and sends both requests (`f=schedule` and `f=schedule&h=true`).
- [ ] A new-day reload within an hour of the last weekly fetch sends only the `h=true` request (weekly cache).
- [ ] Airplane mode + cold start → error with **Retry**; after reconnecting, Retry loads.
- [ ] Airplane mode + pull-to-refresh with content → content kept, error snackbar.
- [ ] Rotation keeps the selected day, filter and scroll position, with no request.
- [ ] Returning to the app on a new local day reloads and selects the new today; on the same day, nothing is requested.
- [ ] Changing the device zone, then pulling to refresh → shows move to the zone's weekdays, times and the "UTC" label follow.
- [ ] Tapping a card does nothing and shows no ripple.
- [ ] Home still looks and behaves as before (bar tint, cover images, times, "UTC±N" banner).

**Appearance:**
- [ ] Medium and expanded windows: rail navigation, 24 dp margins, 3 / 4 columns, all chips visible without scrolling.
- [ ] Light and dark themes: chips, badges and time chips are readable on posters.
- [ ] Font scale 1.3: no clipped chip, badge or header text.
- [ ] TalkBack: the day title is a heading; each chip reads "Thursday 19, today, selected, tab"; each card is one item including its status and time; filter chips announce selection.

---

## 10. Open questions

**Blocking:** none. Every question below can use its recommendation. Q2 is the one product call worth a human glance; if the answer is "drop", delete the today join (§4.4 step 2's second call, `TodayScheduleDTO` move, `getTodaySchedule`) and the filter/badge UI before Phase 2.

| # | Blocks? | Question | Recommendation |
|---|---|---|---|
| Q1 | No | Build the settings data layer (DataStore, `AppSettings`, `SettingsRepository`) now, or use the defaults until Settings exists? | **Defaults now: Monday, 12-hour.** Without the Settings UI, a settings store can't be changed or verified in the app, and the Settings plan owns it (it also carries the theme for `AppRoute`). The hooks are ready: `GetWeeklyScheduleUseCase(weekStart)` and the single shared time formatter. |
| Q2 | No (product call; recommendation implemented) | What do Aired / Upcoming mean without an `aired` field on the weekly endpoint? | **Today only, from today's schedule `aired`, joined by slug** ([§4.3](#43-aired-and-upcoming)); no status on other days; the filter row only on today. It's real data, the same signal Home uses, for one extra ~1.5 KB request. **Alternatives:** drop badges and filters entirely (simplest, matches the spec's item fields); or clock-based statuses for every day (rejected: a guess for delayed or skipped episodes). |
| Q3 | No | Episode labels ("EP 26", "SPEC")? | **Omit.** The weekly schedule has none, and Home's search-per-show lookup would cost up to ~57 requests per week view (or 3–14 per day switch) on a volunteer-run site. Details will show episodes. |
| Q4 | No | Bookmark toggles on cards? | **Not in v1.** The Bookmarks plan can add them to Schedule and Home through its `domain` use cases. |
| Q5 | No | Card taps and the gear before Details and Settings exist? | **Non-clickable cards, hidden gear**, as Home ([1-home.md Q5](1-home.md#10-open-questions)). The intent → effect → nullable-lambda path is built and tested now. |
| Q6 | No | Which dates go under the weekdays? | **The current week** (the week containing today, from the week start), so earlier days show past dates. Alternative: the next occurrence of each weekday (no past dates), which breaks the fixed Monday → Sunday reading. |
| Q7 | No | Today marker vs. selection marker? | **Fill = selection, dot = today**, constant chip size. The design's script moves the dot with the selection, which loses today once another day is picked. |
| Q8 | No | Swipe between days (`HorizontalPager`)? | **Not in v1.** Neither design nor spec has it, and syncing a pager with the ViewModel's selection adds state bugs. Revisit with user feedback. |
| Q9 | No | Implement api.md §7's ~1 h weekly cache here? | **Yes**, in `ScheduleRepositoryImpl`, bypassed by pull-to-refresh. It makes new-day reloads cost one small request. |
| Q10 | No | An unknown `tz` silently returns GMT data (S2). Detect it? | **Ignore in v1, document in api.md.** Android reports IANA ids the server accepts. A later fix could compare the echoed `tz` with the request and convert. |
| Q11 | No | Reload on `ScreenStarted` when the **zone** changed on the same date (Home QA issue 3)? | **Not here; fix both screens together in a follow-up** (`ScreenStarted` would compare zone ids too). Matters more for Schedule (whole shows change day), but parity with Home keeps one rule. Pull-to-refresh already follows a new zone. |
| Q12 | No | Home's "Timeline >" link ([1-home.md Q10](1-home.md#10-open-questions))? | **Follow-up, out of scope:** add `Navigator.navigateToSchedule()` and `onOpenSchedule` on `homeEntry`. |
| Q13 | No | Both `BroadcastService` and `ScheduleService` call `f=schedule&h=true`. Share it? | **Keep one method per feature service** (4 lines each) with the shared `TodayScheduleDTO` and mapper. A shared service would need a `common/data/remote/` folder the `structure` skill doesn't have. |
| Q14 | No | "Shows Airing" on past days reads oddly. | **Keep the design's copy** for all days; it reads as the weekly slot. Change the plural if reviewers prefer "%d Shows". |

---

## 11. Implementation notes

- **Open questions:** Q1–Q14 all use their recommendations, as the requester decided. Week starts Monday, times are 12-hour, Aired/Upcoming and the filter row appear on today only (from today's `aired`), there are no episode labels or bookmark toggles, cards aren't clickable, and the gear is hidden.
- **Step 3:** Home was checked on the emulator. The bar, covers, times and the "UTC+2" banner are unchanged. Home's content didn't overflow the screen that day, so its scroll tint couldn't be triggered there. The same `TopLevelAppBarView` tints correctly when Schedule's grid scrolls.
- **Steps 16–17:** previews were rendered through Android Studio for `ScheduleScreenPreview` (the loading skeleton), `DayChipViewPreview`, `TopLevelAppBarViewPreview` and `HomeScreenPreview`. The tool returns only the first variant, so the other seven Screen states × 4 configurations haven't been viewed. Step 17 stays unticked.
- **Steps 19–20 are not done:** the docs step (api.md, context.md, CLAUDE.md, `structure` skill) is left to the requester, because the implementor edits only the plan. §9 acceptance QA is a separate tester step.
- **Day chip height (§2.2):** the chip uses `max(64 dp, content)` instead of a fixed `height(DayChipHeight)`, so labels aren't clipped at font scale 1.3 (§9). `DayChipSkeletonView` uses the same computation, so nothing shifts. At 1.0 it's 64 dp, as designed.
- **Upcoming badge container (§2.6):** `tertiaryContainer@0.3` is now composited over `surfaceContainerLowest@0.8`, the time chip's base. On the emulator, the plain 30 % tint made "Upcoming" unreadable on bright covers, because the design's backdrop blur was dropped (§3 row 18). Aired is unchanged.
- **Day-strip skeleton (§2.8):** it's a `LazyRow` with `userScrollEnabled = false` that shares `DayStripRow` (padding, spacing) with the real strip, not a plain `Row`. On compact widths the 7 chips overflow, and a `Row` would squeeze the last ones.
- **Filter-chip skeleton:** each pill block (`FilterChipDefaults.Height`) sits in a `minimumInteractiveComponentSize()` box, so it takes the real `FilterChip`'s 48 dp row height.
- **Bar tint per day:** `ScheduleScreen` keys `rememberTopAppBarState()` on `selectedDay`. A newly selected day opens at the top, so the bar tint starts over too, instead of staying tinted from the previous day's scroll. Rotation keeps it, because the state is saveable.
- **`GetWeeklyScheduleUseCase`:** on days other than today it sets `aired = null` explicitly. If the weekly feed ever sends `aired`, other days still get no badge (Q2). `DEFAULT_WEEK_START` is a public companion constant.
- **Defensive decoding beyond §5.2:** `WeeklyScheduleDTOSerializer` skips day items that aren't JSON objects. `WeeklyScheduleDTOMapper` merges keys that differ only in case or spaces ("Monday" / " monday ") instead of letting one overwrite the other.
- **Fixture:** `schedule_week.json` was captured on 2026-11-18 for `tz=America/New_York` with one request and trimmed to 1–3 items per day. It includes Tuesday's placeholder cover (*Kyouran Reijou Nia Liston*) and Wednesday's two 08:00 shows. The live shape matched §4.2 (S3–S6).
- **Pulsing dot:** `StatusBadgeView` has its own private pulsing dot, the same code as `TodaySummaryView`'s. Extracting a shared component was outside this plan's scope.
