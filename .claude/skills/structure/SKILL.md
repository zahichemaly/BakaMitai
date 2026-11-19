---
name: structure
description: Package hierarchy and file placement rules for this Android app. Use when creating, moving, or locating files.
---

# Structure

This skill defines **where every file goes**. It is the single source of truth for the package tree. Layer dependency rules are in `architecture`, naming in `conventions`, libraries in `tech-stack`, quality and testing rules in `best-practices`.

Root package: `com.zc.bakamitai` (source root `app/src/main/java/com/zc/bakamitai/`). Rules marked **MUST** / **NEVER** are non-negotiable.

---

## 1. Package tree

```text
com.zc.bakamitai/
├── app/                             # Application entry point, root navigation & DI assembly
│   ├── MainActivity.kt
│   ├── AppRoute.kt                  # App root: provides app-wide settings (composition locals) around AppNavigation
│   ├── AppViewModel.kt              # Reads those settings (scanned by AppModule)
│   ├── AppState.kt                  # The app root's state (theme, clock, loaded flag); no Intent/Effect
│   ├── AppApplication.kt            # Initializes Koin (+ Coil ImageLoader factory)
│   ├── di/
│   │   ├── AppModule.kt             # @Module(includes = [...]): CommonModule and every feature module
│   │   └── AppKoin.kt               # @KoinApplication entry point for startKoin<AppKoin>
│   └── navigation/
│       ├── AppNavigation.kt         # NavigationSuiteScaffold + NavDisplay (the shell)
│       ├── TopLevelDestination.kt   # The tabs: root key, icon, label
│       └── AppEntryProvider.kt      # entryProvider: calls every feature's entry function, wiring features together
│
├── common/                          # Shared code used across two or more features
│   ├── data/
│   │   ├── network/                 # Shared Ktor HttpClient factory, Json config, plugins
│   │   ├── dto/                     # Wire-format DTOs used by 2+ features (QueryDTO, ImageDTO, TagDTO)
│   │   ├── mapper/                  # DTO -> common/domain mappers for those DTOs
│   │   ├── local/                   # Shared Jetpack DataStore sources (e.g. app settings)
│   │   ├── repository/              # Implementations of common/domain repositories (e.g. SettingsRepositoryImpl)
│   │   └── util/
│   │       ├── SafeApiCall.kt       # safeApiCall { } — Ktor/serialization exceptions -> DomainError
│   │       └── SafeDataStoreCall.kt # safeDataStoreCall { } — DataStore write failures -> DomainError
│   ├── domain/
│   │   ├── model/                   # Shared pure Kotlin domain primitives, DomainError.kt, result types, AppSettings
│   │   ├── repository/              # Interfaces used by 2+ features (e.g. SettingsRepository)
│   │   ├── usecase/                 # Use cases used by 2+ features (e.g. ObserveSettingsUseCase)
│   │   └── util/
│   │       ├── DispatcherProvider.kt  # DispatcherProvider interface + DefaultDispatcherProvider
│   │       └── TimeZoneProvider.kt    # TimeZoneProvider interface + SystemTimeZoneProvider (device zone)
│   ├── di/
│   │   └── CommonModule.kt          # @Module @ComponentScan(common) + @Single providers for Json, HttpClient, Clock, DataStore
│   └── ui/
│       ├── theme/                   # Material 3 Color.kt, Type.kt, Theme.kt
│       ├── util/                    # ObserveAsEvents.kt, AdaptivePreviews.kt, NotificationPermission.kt, OpenLink.kt (non-visual helpers)
│       ├── text/                    # UiText.kt; TimeText.kt (LocalUse24HourClock and the 12/24-hour clock formats)
│       ├── mapper/                  # common/domain -> UiText/UI model mappers used by 2+ features (e.g. NovelLength labels)
│       └── component/               # Shared UI widgets & adaptive scaffolds — same recursive shape as §1.1
│           └── [name]/              # e.g. coverimage/, markdown/, error/
│               ├── model/           # e.g. error/model/ErrorUIModel.kt
│               ├── mapper/          # e.g. error/mapper/ErrorUIModelMapper.kt (DomainError -> ErrorUIModel)
│               └── [Name]View.kt
│
└── feature/
    └── [name]/                      # Lowercase feature package (e.g. novel, search, settings)
        ├── data/
        │   ├── remote/              # Ktor [Name]Service classes and [Name]HtmlParser (Ksoup) for this feature
        │   ├── local/               # Feature-specific DataStore sources, or Room: [Name]Entity, [Name]Dao, [Name]Database
        │   ├── dto/                 # [Name]DTO.kt — strictly one DTO per file (@Serializable, unless a parser builds it)
        │   ├── mapper/              # [Name]DTOMapper.kt / [Name]EntityMapper.kt & [Name]DomainMapper.kt — separate files
        │   ├── worker/              # WorkManager [Name]Worker (@KoinWorker) and [Name]SchedulerImpl (if needed)
        │   └── repository/          # [Name]RepositoryImpl.kt implementing the domain interface
        ├── domain/
        │   ├── model/               # [Name].kt — pure Kotlin domain models, NO suffix
        │   ├── repository/          # [Name]Repository.kt — interface; platform ports [Name]Scheduler / [Name]Notifier
        │   └── usecase/             # [Name]UseCase.kt — single responsibility, operator fun invoke
        ├── di/
        │   └── [Name]Module.kt      # @Module @ComponentScan class; classes carry @Single/@Factory/@KoinViewModel
        └── presentation/
            ├── model/               # Immutable [Name]UIModel.kt (static) & [Name]UIState.kt (dynamic)
            ├── mapper/              # [Name]UIModelMapper.kt (Domain -> UIModel / UIState)
            ├── notification/        # [Name]NotifierImpl.kt: builds and posts system notifications (if needed)
            ├── component/           # Components reused ONLY within this feature — recursive, see §1.1
            │   └── [name]/          # One lowercase subpackage per component (e.g. novelheader/)
            │       ├── model/       # [Name]UIModel.kt / [Name]UIState.kt owned by this component
            │       ├── mapper/      # [Name]UIModelMapper.kt for this component's models
            │       ├── component/   # Sub-components of this component (same shape, recursively)
            │       └── [Name]View.kt
            ├── [Name]State.kt       # Screen-level immutable MVI state
            ├── [Name]Intent.kt      # Sealed interface of user actions & screen triggers
            ├── [Name]Effect.kt      # Sealed interface of one-off side effects
            ├── [Name]ViewModel.kt   # StateFlow<[Name]State>, Flow<[Name]Effect>, fun onIntent(...)
            ├── [Name]Route.kt       # Collects state/effects, passes state + callbacks to Screen
            └── [Name]Screen.kt      # Stateless Compose UI: (State, (Intent) -> Unit)
```

Rules:

- **MUST** create all four feature subpackages — `data`, `domain`, `di`, `presentation` — for every feature.
- Feature names are **lowercase**, no underscores or hyphens (`feature/userlist`, never `feature/UserList` or `feature/user_list`).
- **NEVER** add top-level packages beyond `app`, `common`, `feature`. No layer-first packages (`com.zc.bakamitai.data`, `com.zc.bakamitai.ui`, `com.zc.bakamitai.viewmodels`, `com.zc.bakamitai.utils`).
- **NEVER** add folders that are not in the tree (`helper/`, `base/`, `extensions/`, `manager/`). Place the code in the folder that matches its role.
- `common/data/dto/` and `common/data/mapper/` hold only DTOs (and their mappers) that two or more features send or receive; a DTO used by one feature stays in that feature.
- `util/` exists **only** at `common/data/util/`, `common/domain/util/` and `common/ui/util/`, for the cross-cutting helpers listed in §2. **NEVER** create `util/` inside a feature, and never use these folders as a dumping ground for feature logic.
- Feature `presentation/` is flat: a feature with several screens holds several `[Name]State/Intent/Effect/ViewModel/Route/Screen` sets side by side.

### 1.1 Components mirror `presentation` (recursively)

Each component under `component/` is a **mini `presentation` package**. It gets its own lowercase subpackage, which holds its `model/`, `mapper/` and nested `component/` folders next to its `[Name]View.kt`.

```text
presentation/
├── model/                           # Models used by the screen State or by 2+ components
├── mapper/
├── component/
│   ├── novelheader/
│   │   ├── model/NovelHeaderUIModel.kt
│   │   ├── mapper/NovelHeaderUIModelMapper.kt
│   │   ├── component/
│   │   │   └── ratingbadge/
│   │   │       ├── model/RatingBadgeUIModel.kt
│   │   │       ├── mapper/RatingBadgeUIModelMapper.kt
│   │   │       └── RatingBadgeView.kt
│   │   └── NovelHeaderView.kt
│   └── screenshotgallery/
│       ├── model/ScreenshotUIState.kt
│       ├── mapper/ScreenshotUIModelMapper.kt
│       └── ScreenshotGalleryView.kt
├── NovelDetailState.kt              # composes NovelHeaderUIModel, ImmutableList<ScreenshotUIState>
└── …
```

Rules:

- **MUST** give every component its own subpackage named after the component in lowercase (`component/novelheader/NovelHeaderView.kt`, never `component/NovelHeaderView.kt`).
- The component subpackage uses the **same folder names and meanings** as `presentation/`: `model/` for its `[Name]UIModel` / `[Name]UIState`, `mapper/` for its `[Name]UIModelMapper`, and `component/` for its sub-components, nested to any depth.
- Create `model/`, `mapper/` and `component/` only when the component has something to put in them. A component that takes only primitives and lambdas is just `component/[name]/[Name]View.kt`.
- Components are stateless views. They **NEVER** have their own `ViewModel`, `Route`, `Screen`, `State`, `Intent` or `Effect`. Those exist only at the screen level of `presentation/`.
- **A model lives with its narrowest owner:**
  - rendered only by one component → that component's `model/` (and its mapper in that component's `mapper/`)
  - rendered by 2+ components of the feature, or read directly by the Screen → `presentation/model/` and `presentation/mapper/`
- The screen `State` and the ViewModel may import a component's models and mappers. That is how the ViewModel maps domain → component model. A component **NEVER** imports from its parent component, from a sibling component, or from screen-level files (`[Name]State`, `[Name]Intent`).
- A sub-component used by two sibling components moves **up** to the nearest shared `component/` level. When two features use it, it moves to `common/ui/component/[name]/`.
- `common/ui/component/[name]/` follows the same shape. Its models are feature-agnostic, and its mappers (if any) map only from `common/domain` types.

---

### 1.2 Repository root (build files)

```text
BakaMitai/
├── app/build.gradle.kts             # Declarative only: plugins, namespace, dependencies (libs.*)
├── app/schemas/                     # Room schemas exported at build time (bakamitai.android.room), committed
├── build-logic/                     # Included build: convention plugins + ALL custom Gradle tasks
├── config/detekt/detekt.yml
├── gradle/libs.versions.toml        # Version catalog: every library, BOM and plugin
├── build.gradle.kts                 # Declarative only: plugins … apply false
└── settings.gradle.kts              # includeBuild("build-logic"), include(":app")
```

Rules are in `library-management`.

## 2. Placement lookup

| I am writing… | It goes in |
|---|---|
| Activity, `Application`, `startKoin<AppKoin>` | `app/` |
| Aggregated Koin module (`@Module(includes = [...])`) and the `@KoinApplication` entry point | `app/di/AppModule.kt`, `app/di/AppKoin.kt` |
| `NavigationSuiteScaffold`, `NavDisplay`, tab definitions | `app/navigation/AppNavigation.kt`, `app/navigation/TopLevelDestination.kt` |
| Wiring features' entries and cross-feature navigation lambdas | `app/navigation/AppEntryProvider.kt` |
| `Navigator`, `NavigationState`, `BottomSheetSceneStrategy`, `FullScreenNavKey` / `OverlayNavKey` | `common/ui/navigation/` |
| A feature's `@Serializable` `NavKey`s | `feature/[name]/presentation/navigation/[Name]NavKey.kt` |
| A feature's `Navigator.navigateTo[Name](...)` helpers and `EntryProviderScope<NavKey>.[name]Entry(...)` (and `[Name]NavActions` when a Route has more than ~4 navigation callbacks) | `feature/[name]/presentation/navigation/[Name]Navigation.kt` (`[Name]NavActions.kt`) |
| Shared `HttpClient` factory, `Json` instance | `common/data/network/` |
| `safeApiCall { }` (exception → `DomainError` mapping) | `common/data/util/SafeApiCall.kt` |
| `safeDataStoreCall { }` (DataStore write failure → `DomainError`) | `common/data/util/SafeDataStoreCall.kt` |
| DTO used by 2+ features (query body, image, tag) | `common/data/dto/[Name]DTO.kt` |
| Mapper from a shared DTO to a `common/domain` model | `common/data/mapper/[Name]DTOMapper.kt` |
| Shared DataStore helpers (file factory, preference key helpers: `createSettingsDataStore`, `safeData()`, `enumOrNull()`) and the settings source (`SettingsLocalDataSource`) | `common/data/local/` |
| `DomainError` and other error/result/value types used by 2+ features' domains (incl. `AppSettings`) | `common/domain/model/` |
| Repository interface / use case used by 2+ features (e.g. settings) | `common/domain/{repository,usecase}/` |
| `DispatcherProvider` / `DefaultDispatcherProvider` | `common/domain/util/DispatcherProvider.kt` |
| `TimeZoneProvider` / `SystemTimeZoneProvider` | `common/domain/util/TimeZoneProvider.kt` |
| Koin providers for types we don't own (`HttpClient`, `Json`, `Clock`, `DataStore`) | `common/di/CommonModule.kt` |
| Theme, colors, typography | `common/ui/theme/` |
| `ObserveAsEvents`, `@AdaptivePreviews` and other non-visual Compose helpers (incl. `ContentWidth.kt`: `MaxContentWidth` and `centeredContentPadding`, the 720 dp centered column for tablets) | `common/ui/util/` |
| Navigation plumbing shared by all features (`Navigator`, `NavigationState`, marker interfaces, bottom-sheet scene) | `common/ui/navigation/` |
| The intent a notification opens and how `MainActivity` reads it (`showDetailsIntent`, `showSlugOrNull`) | `common/ui/navigation/ShowDeepLink.kt` |
| The `POST_NOTIFICATIONS` request and the app-notification-settings shortcut, used by 2+ features | `common/ui/util/NotificationPermission.kt` |
| Presentation mapper for a `common/domain` type used by 2+ features (not tied to one component) | `common/ui/mapper/[Name]UIModelMapper.kt` |
| `UiText` (all user-visible text in `presentation`) | `common/ui/text/UiText.kt` |
| `LocalUse24HourClock` (provided by `AppRoute` from the *Time format* setting) and the 12/24-hour clock formats used by `UiText.TimeValue` | `common/ui/text/TimeText.kt` |
| `Context.openLink(url)` (`ACTION_VIEW`, used by 2+ features) | `common/ui/util/OpenLink.kt` |
| `StringResource` / `PluralResource` / `FontResource` / `DrawableResource` typealiases for `R.string` / `R.plurals` / `R.font` / `R.drawable` (the only way code names a resource) | `common/ui/resource/ResourceTypes.kt` |
| `ErrorUIModel`, `ErrorUIModelMapper`, `ErrorView` | `common/ui/component/error/{model,mapper}/`, `ErrorView.kt` |
| Loading placeholders: `SkeletonView`, `SkeletonBlock`, `SkeletonTextLine`, `textLineHeight` (`best-practices` §5.3) | `common/ui/component/skeleton/SkeletonView.kt` |
| Composable used by 2+ features | `common/ui/component/[name]/[Name]View.kt` |
| Ktor API calls for one feature | `feature/[name]/data/remote/[Name]Service.kt` |
| HTML parser for a scraped page (the only Ksoup user) | `feature/[name]/data/remote/[Name]HtmlParser.kt` |
| DataStore source for one feature | `feature/[name]/data/local/[Name]LocalDataSource.kt` |
| Room entity / DAO / database of one feature | `feature/[name]/data/local/[Name]Entity.kt` / `[Name]Dao.kt` / `[Name]Database.kt` |
| Entity → Domain / Domain → Entity | `feature/[name]/data/mapper/[Name]EntityMapper.kt` / `[Name]DomainMapper.kt` |
| Room database, DAO and `WorkManager` providers | the feature's `feature/[name]/di/[Name]Module.kt` |
| WorkManager worker (`@KoinWorker`) / the scheduler that enqueues it | `feature/[name]/data/worker/[Name]Worker.kt` / `[Name]SchedulerImpl.kt` |
| Domain port for a platform service (`[Name]Scheduler`, `[Name]Notifier`) | `feature/[name]/domain/repository/` |
| System notifications (implements the domain `[Name]Notifier`) | `feature/[name]/presentation/notification/[Name]NotifierImpl.kt` |
| Wire-format class (a DTO built by an HTML parser is a plain `data class`, not `@Serializable`) | `feature/[name]/data/dto/[Name]DTO.kt` |
| DTO → Domain / Domain → DTO | `feature/[name]/data/mapper/[Name]DTOMapper.kt` / `[Name]DomainMapper.kt` |
| Repository implementation | `feature/[name]/data/repository/[Name]RepositoryImpl.kt` |
| Domain model / repository interface / use case | `feature/[name]/domain/{model,repository,usecase}/` |
| Feature Koin module (`@Module @ComponentScan("com.zc.bakamitai.feature.[name]")`) | `feature/[name]/di/[Name]Module.kt` |
| UI model / UI state read by the Screen or 2+ components | `feature/[name]/presentation/model/` |
| Domain → UI mapper for those models | `feature/[name]/presentation/mapper/[Name]UIModelMapper.kt` |
| Composable reused by screens of one feature | `feature/[name]/presentation/component/[component]/[Name]View.kt` |
| UI model / UI state rendered only by one component | `…/component/[component]/model/` |
| Mapper for a component's own models | `…/component/[component]/mapper/[Name]UIModelMapper.kt` |
| Sub-component used only inside one component | `…/component/[component]/component/[sub]/[Name]View.kt` |
| A component's loading placeholder `[Name]SkeletonView` | the same file as the component: `…/component/[component]/[Name]View.kt` |
| A screen's loading layout `[Name]Skeleton` | `private` in that `[Name]Screen.kt` |
| Composable used by one screen only | `private` in that `[Name]Screen.kt` |
| MVI contract, ViewModel, Route, Screen | `feature/[name]/presentation/` |
| Library / plugin dependency declaration | `gradle/libs.versions.toml` |
| Custom Gradle task, convention plugin, shared build config | `build-logic/convention/src/main/kotlin/` |
| Unit test for `X` | `app/src/test/java/com/zc/bakamitai/<same package as X>/XTest.kt` |
| Instrumented test for `X` (Room DAO, worker, scheduler, notifier, anything needing a real `Context` or `Intent`) | `app/src/androidTest/java/com/zc/bakamitai/<same package as X>/XTest.kt` |
| Test fakes (`Fake[Name]Repository`, `TestDispatcherProvider`) and rules (`MainDispatcherRule`) | Test source set, mirroring the package of the thing they fake (`MainDispatcherRule` → `common/util/`) |

---

## 3. Shared code (`common/`)

- Code moves to `common/` **only** when two or more features use it. Until then it stays in its feature.
- When a second feature needs something from `feature/a`, **move** it to the matching `common/` folder — never import `feature.a.*` from `feature.b`.
- `common/` is feature-agnostic: it **NEVER** imports `feature.*` or `app.*`.
- `common/ui/component/` widgets accept primitives, `common` types, and lambdas — never a feature's `UIModel`, `State`, or `Intent`.

### Component placement

```text
Is the composable used by more than one screen?
├── No  → private in that [Name]Screen.kt
└── Yes → Are all users inside the SAME feature?
          ├── Yes → Are all users inside the SAME parent component?
          │         ├── Yes → …/component/[parent]/component/[name]/[Name]View.kt
          │         └── No  → feature/[name]/presentation/component/[name]/[Name]View.kt
          └── No  → common/ui/component/[name]/[Name]View.kt
```

Each component folder follows §1.1 (`model/`, `mapper/`, nested `component/`).

---

## 4. Allowed package imports

| From ↓ may import → | `app` | `common` | own `feature/[name]` | other `feature/*` |
|---|---|---|---|---|
| `app` | ✅ | ✅ | ✅ (Routes, modules) | ✅ (Routes, modules) |
| `common` | — | ✅ | — | — |
| `feature/[name]` | — | ✅ | ✅ (per layer rules in `architecture`) | `domain` only (models, use cases) |

- A feature owns its `NavKey`s and its entry extension function in `presentation/navigation/`. Everything that leaves the feature (open another feature's screen, go back) is a lambda parameter of that function. A feature never imports another feature's navigation package.
- Only `app/navigation/AppEntryProvider.kt` knows every feature: it passes each entry function the lambdas that navigate to the other features' keys. The back stack and `NavDisplay` live in `app/navigation/` and `common/ui/navigation/`.
- Only `app/di/AppModule.kt` lists all modules; feature modules never include each other.

---

## 5. Reference: `app/` wiring

```kotlin
// app/di/AppModule.kt
@Module(includes = [CommonModule::class, NovelModule::class, SearchModule::class, SettingsModule::class])
class AppModule

// app/di/AppKoin.kt
@KoinApplication(modules = [AppModule::class])
object AppKoin
```

```kotlin
// app/AppApplication.kt
class AppApplication : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        startKoin<AppKoin> {
            androidLogger()
            androidContext(this@AppApplication)
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .crossfade(true)
            .build()
}
```

---

## 6. Do / Don't

| ❌ Don't | ✅ Do |
|---|---|
| `com.zc.bakamitai.ui.screens.NovelDetailScreen` | `com.zc.bakamitai.feature.novel.presentation.NovelDetailScreen` |
| `com.zc.bakamitai.utils.DateUtils` / `feature/novel/util/` | Formatting in `presentation/mapper/[Name]UIModelMapper.kt` |
| `NovelDetailNavKey` declared in `app/navigation/AppNavigation.kt` | `feature/novel/presentation/navigation/NovelDetailNavKey.kt` |
| `common/ui/SkeletonView.kt` | `common/ui/component/skeleton/SkeletonView.kt` |
| `component/releaserow/skeleton/ReleaseRowSkeletonView.kt` (separate package) | `ReleaseRowSkeletonView` in `component/releaserow/ReleaseRowView.kt`, sharing its private dimensions |
| `feature/novel/presentation/component/NovelHeaderView.kt` (flat file) | `feature/novel/presentation/component/novelheader/NovelHeaderView.kt` |
| `NovelHeaderUIModel` in `presentation/model/` when only `NovelHeaderView` renders it | `component/novelheader/model/NovelHeaderUIModel.kt` |
| `component/novelheader/NovelHeaderViewModel.kt` / `NovelHeaderState.kt` | Components are stateless; state lives in the screen `[Name]State` |
| `component/ratingbadge/` importing `component.novelheader.model.*` (sibling) | Move the shared model up to `presentation/model/` |
| `feature/search` importing `feature.novel.presentation.component.cover.CoverView` | Move it to `common/ui/component/cover/CoverView.kt` |
| `common/ui/component/novelcard/NovelCardView(model: VisualNovelUIModel)` | Keep it in `feature/novel/presentation/component/novelcard/` |
| `feature/novel/data/NovelRepositoryImpl.kt` (no subfolder) | `feature/novel/data/repository/VisualNovelRepositoryImpl.kt` |
| `feature/novel/di/` binding another feature's repository | Each feature binds its own; `AppModule` aggregates |

---

## 7. Checks

Each must print nothing:

```bash
ls app/src/main/java/com/zc/bakamitai | grep -vxE "app|common|feature"
```

```bash
grep -rlE "import com\.zc\.bakamitai\.(feature|app)\." app/src/main/java/com/zc/bakamitai/common
```

```bash
grep -rlE "import com\.zc\.bakamitai\.app\." app/src/main/java/com/zc/bakamitai/feature
```

### Current repo state

The template files have moved: `app/MainActivity.kt`, `app/AppApplication.kt` (registered in the manifest), and the theme in `common/ui/theme/`. `app/navigation/` exists (`AppNavigation`, `TopLevelDestination`, `AppEntryProvider`), and `app/AppRoute.kt` hosts it. Home (`feature/home/`), Schedule (`feature/schedule/`) and All Shows (`feature/shows/`) are built and enabled, and their cards and rows open **Details** (`feature/details/`), the first pushed screen: its `DetailsNavKey` implements `FullScreenNavKey` (`common/ui/navigation/`), which hides the bottom bar on phones. **Bookmarks** (`feature/bookmarks/`) is built and enabled too, so all four tabs work: Room in `data/local/`, the release-check worker in `data/worker/`, the scheduler and notifier ports in `domain/repository/`, and the notification code in `presentation/notification/` (Bookmarks plan, [docs/5-bookmarks.md](../../../docs/5-bookmarks.md)). The Details plan's temporary exception (bookmarks without `presentation/`) is closed. **Settings** (`feature/settings/`, [docs/6-settings.md](../../../docs/6-settings.md)) is a pushed `FullScreenNavKey` screen opened from the gear on every tab; the settings themselves live in `common/` (`AppSettings`, `SettingsRepository`, the `settings` Preferences DataStore), and the app root (`AppViewModel` / `AppState` / `AppRoute`) applies the theme before the first frame and provides `LocalUse24HourClock`. Details moved two more pieces to `common/`: the `release_date` parser (`common/data/mapper/ReleaseDateDTOMapper.kt`, from Home) and the centered-column helper (`common/ui/util/ContentWidth.kt`, from All Shows). All Shows added the first HTML parser (`feature/shows/data/remote/AllShowsHtmlParser.kt`, the only Ksoup import) and the shared `common/ui/component/emptystate/EmptyStateView.kt`. Schedule, as the second user, moved Home's shared pieces to `common/` ([docs/2-schedule.md §5.1](../../../docs/2-schedule.md#51-shared-code-moved-from-featurehome)): `Show`, `ShowSlug`, `ScheduledShow` and `GetTodayDateUseCase` in `common/domain/`, `ScheduleItemDTO`, `TodayScheduleDTO`, their mapper and image-URL mapping in `common/data/`, `TimeUIModelMapper` and `UtcOffsetUIModelMapper` in `common/ui/mapper/`, and `CoverImageView` and `TopLevelAppBarView` in `common/ui/component/`.

## 8. Checklist

- [ ] Every new file's package matches a row in §2.
- [ ] No top-level packages or folders outside the tree.
- [ ] `common/` code is used by 2+ features and imports no `feature`/`app` code.
- [ ] Every component has its own `component/[name]/` subpackage. Its own models and mappers sit inside it, its sub-components nest under its `component/`, and it has no ViewModel, Route or State.
- [ ] NavKeys and entry extension functions live in the owning feature's `presentation/navigation/`; no feature imports another feature's navigation package; `AppEntryProvider.kt` does the wiring.
- [ ] `app/di/AppModule.kt` includes every feature module.
