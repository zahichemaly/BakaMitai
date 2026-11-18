# BakaMitai

Android app, being rewritten from scratch. The repo holds a fresh Jetpack Compose scaffold; the previous codebase is in git history and on the `master`, `clean-architecture` and `compose-migration` branches.

## Project

- Single Gradle module `:app`, Kotlin only, Jetpack Compose + Material 3.
- Root package `com.zc.bakamitai` (`app/src/main/java/com/zc/bakamitai/`).
- `minSdk` 27, `compileSdk` / `targetSdk` 37. AGP 9.5.1 (built-in Kotlin), Gradle 9.6.0, Kotlin 2.4.20, Compose BOM 2026.09.00.
- Every dependency and plugin is declared in `gradle/libs.versions.toml`.
- Work happens on `develop`; `master` is the default branch for PRs.

## Commands

```bash
./gradlew assembleDebug
```

```bash
./gradlew testDebugUnitTest
```

```bash
./gradlew connectedDebugAndroidTest
```

```bash
android run --apks=app/build/outputs/apk/debug/app-debug.apk
```

- `connectedDebugAndroidTest` needs a running emulator or device, and uninstalls the app when it finishes.
- `android run` installs and launches the debug APK on the connected device.
- `./gradlew detekt` must pass with zero findings; `./gradlew detekt --auto-correct` fixes ktlint formatting.

## Skills

Project rules live in `.claude/skills/`. Load the relevant ones before writing code, and all six when generating a feature.

| Skill | Covers |
|---|---|
| `structure` | Package tree and where every file goes |
| `architecture` | Clean Architecture layers, dependency rule, MVI data flow |
| `conventions` | Naming, immutability, mappers, MVI contract, Route / Screen / View |
| `tech-stack` | Approved libraries and which layer may use each |
| `library-management` | Version catalog, `build-logic/` convention plugins, custom Gradle tasks |
| `best-practices` | Compose performance, Detekt, coroutines, error handling, previews, testing |

Agents that don't load Claude Code skills should read `.claude/skills/<name>/SKILL.md` directly.

Rules marked **MUST** / **NEVER** are non-negotiable. When code and a skill disagree, follow the skill and fix the code; when the rule itself should change, update the skill first.

## Current state

- Foundation in place: every approved library is in the catalog and wired into `:app`; `build-logic/` has the `bakamitai.android.application`, `bakamitai.detekt` and `bakamitai.kotlin.only` convention plugins; Koin runs through the Koin compiler plugin (`startKoin<AppKoin>`).
- Theme: `common/ui/theme/` implements `docs/DESIGN.md` ("Obsidian Signal"), with bundled Plus Jakarta Sans / Space Grotesk fonts. DESIGN.md defines dark only; the light scheme is derived from the same tonal palettes (see `Color.kt`). Extra tokens: `BakaMitaiTheme.extendedColors`, `BakaMitaiTheme.spacing`.
- Shared code in `common/`: `UiText`, `ResourceTypes.kt` (incl. `PluralResource`), `ObserveAsEvents`, `@AdaptivePreviews`, `ErrorView`, the skeleton pieces (`SkeletonView`, `SkeletonBlock`, `SkeletonTextLine`), `Navigator` / `NavigationState`, `DomainError`, `DispatcherProvider`, `TimeZoneProvider`, `safeApiCall`, the SubsPlease `HttpClient`, `CommonModule` (also provides a `kotlin.time.Clock`). Moved from Home by the Schedule plan: `Show` / `ShowSlug` / `ScheduledShow` and `GetTodayDateUseCase` (`common/domain/`), `ScheduleItemDTO` / `TodayScheduleDTO` and their mappers plus image-URL mapping (`common/data/`), the time and UTC-offset label mappers (`common/ui/mapper/`), `CoverImageView` and `TopLevelAppBarView` (`common/ui/component/`).
- Navigation shell: `app/navigation/` (`AppNavigation`, `TopLevelDestination`, `AppEntryProvider`), hosted by `app/AppRoute.kt`, using `NavigationSuiteScaffold`. All Shows and Bookmarks are disabled destinations, and the Settings gear is hidden, until those features exist.
- Features: **Home** (`feature/home/`, plan [docs/1-home.md](docs/1-home.md)) and **Schedule** (`feature/schedule/`, plan [docs/2-schedule.md](docs/2-schedule.md)). Until Settings exists, the week starts on Monday and times are 12-hour; cards aren't clickable until Details exists.
- Loading states are shimmering skeletons that mirror the content, never spinners (`best-practices` §5.3).
- Feature plans live in `docs/<n>-<feature>.md` (written by the `planner` agent, built by `implementor`, checked by `tester`). Icons come from `material-icons-extended`.
