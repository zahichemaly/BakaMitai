---
name: tech-stack
description: Approved libraries and which layer may use each, for this Android app. Use when choosing a library or API.
---

# Tech Stack

This skill defines **which libraries and APIs** code may use, and **in which layer**. It works together with:

- `structure` — the package tree and where every file goes.
- `architecture` — the dependency rule between `presentation`, `domain`, `data`, `di`, and MVI data flow.
- `conventions` — file/class naming, immutability, MVI contracts, Route/Screen/View.

Quality rules for using these libraries (performance, Detekt, error handling, testing) are in `best-practices`. How dependencies are declared (version catalog, `build-logic/`) is in `library-management`. Load all six when generating code. Rules marked **MUST** / **NEVER** are non-negotiable. Adding any library not listed in §1 requires updating this skill first.

---

## 1. Approved stack

| Concern | Approved library |
|---|---|
| Language | Kotlin (100%), Compose & serialization compiler plugins |
| Concurrency | Kotlin Coroutines & Flow |
| Dates/time | kotlinx-datetime |
| Immutable collections | kotlinx-collections-immutable |
| UI | Jetpack Compose + Material 3 |
| Icons | Material Icons Extended (`Icons.*` `ImageVector`s; R8 strips the unused ones) |
| Adaptive UI | Material 3 Adaptive (navigation suite, adaptive layout, adaptive Navigation 3 scenes) |
| Lifecycle | Lifecycle runtime/ViewModel Compose, ViewModel Navigation 3 |
| Rich text | Compose RichText (CommonMark) |
| Images | Coil 3 (Compose + Ktor network fetcher) |
| Navigation | Navigation 3 |
| Networking | Ktor Client (OkHttp engine, content negotiation, kotlinx-json, logging) |
| Serialization | kotlinx.serialization (JSON) |
| HTML parsing | Ksoup (`com.fleeksoft.ksoup:ksoup`), scraped SubsPlease pages only, inside a `data/remote/[Name]HtmlParser` |
| Persistence | Jetpack DataStore (Preferences or Proto/typed) |
| DI | Koin (Android, Compose) |
| Static analysis | Detekt + ktlint formatting + Compose rules |
| Compose diagnostics | Compose compiler reports |
| Testing | JUnit 4, kotlin.test, kotlinx-coroutines-test, Turbine, Ktor MockEngine, koin-test |

Coordinates, catalog accessors and build setup (Detekt, Compose reports) are in `library-management`. Detekt rules themselves are in `best-practices` §2.

### Banned

| ❌ Never use | ✅ Use instead |
|---|---|
| Java source files (`*.java`) | Kotlin |
| RxJava / RxKotlin, `LiveData`, `AsyncTask`, `Executor`s, `Thread`, `Handler.post` | Coroutines, `Flow`, `StateFlow`, `Channel` |
| `runBlocking` (outside tests), `Future.get()`, `CountDownLatch`, callback-based APIs in our code | `suspend`, `suspendCancellableCoroutine`, `callbackFlow` |
| XML layouts, `Fragment`s for UI, `RecyclerView`, View-based widgets | Compose |
| Material 2 (`androidx.compose.material.*` components: `Scaffold`, `Button`, `TopAppBar`, …) | Material 3 (`androidx.compose.material3.*`) — `androidx.compose.material.icons.*` is allowed |
| `LocalConfiguration.current.screenWidthDp` / orientation checks for layout | `currentWindowAdaptiveInfo().windowSizeClass`, adaptive scaffolds |
| `Html.fromHtml`, `AndroidView { TextView }`, `Linkify`, `Spanned` | Compose RichText `Markdown` |
| Glide, Picasso, Fresco, manual `BitmapFactory` decoding | Coil `AsyncImage` |
| Navigation 2 string routes (`"detail/{id}"`), `NavController`, `Bundle` args, `SavedStateHandle["id"]` for nav args | Navigation 3 `@Serializable` `NavKey`s |
| Retrofit, raw OkHttp (`okhttp3.*` imports), Volley, `HttpURLConnection` | Ktor `HttpClient` |
| Gson, Moshi, Jackson, `org.json` | kotlinx.serialization |
| jsoup (Java), regex / `indexOf` HTML scraping, `Html.fromHtml` for parsing | Ksoup in a `[Name]HtmlParser` |
| `SharedPreferences`, `EncryptedSharedPreferences`, file I/O on the main thread | DataStore |
| Dagger, Hilt, `javax.inject`, hand-rolled service locators, `KoinComponent` / `by inject()` in classes | Koin Annotations + constructor injection |
| `java.util.Date`, `Calendar`, `SimpleDateFormat`, `java.time` | kotlinx-datetime |
| `collectAsState()` in Routes, `LaunchedEffect { flow.collect }` for effects | `collectAsStateWithLifecycle()`, `ObserveAsEvents` |
| Mockito / mocking frameworks for our own classes | Hand-written fakes (`Fake[Name]Repository`) |
| Shimmer / placeholder libraries (accompanist-placeholder, compose-shimmer, …); `CircularProgressIndicator` / `LinearProgressIndicator` as a loading state | `common/ui/component/skeleton/` (`SkeletonView`, `SkeletonBlock`, `SkeletonTextLine`), see `best-practices` §5.3 |

### Verification commands

Each of these must print nothing:

```bash
find app/src -name "*.java"
```

```bash
find app/src/main/res -type d -name "layout*"
```

```bash
grep -rnE "import (retrofit2|okhttp3|com\.google\.gson|com\.squareup\.moshi|dagger|javax\.inject|io\.reactivex|androidx\.lifecycle\.(Mutable)?LiveData|com\.bumptech\.glide|com\.squareup\.picasso|androidx\.compose\.material\.[A-Z]|android\.content\.SharedPreferences|androidx\.fragment|androidx\.navigation\.(compose|NavController)|android\.text\.Html|java\.util\.(Date|Calendar)|java\.text\.SimpleDateFormat|java\.time)" app/src
```

```bash
grep -rnE "runBlocking|GlobalScope|KoinComponent|by inject\(\)" app/src/main
```

Ksoup is imported only by `data/remote/[Name]HtmlParser.kt` files:

```bash
grep -rln "import com.fleeksoft.ksoup" app/src/main | grep -v "/data/remote/[A-Za-z]*HtmlParser.kt$"
```

---

## 2. Layer-mapping table

✅ = allowed, — = forbidden. Columns follow the `structure` skill: `domain` includes `common/domain`, `data` includes `common/data`, `di` includes `common/di` and `app/di`, and "app" means `app/` (Activity, Application, `app/navigation/`). `presentation` includes each feature's `presentation/navigation/`.

| Library | `domain` | `data` | `presentation` | `common/ui` | `di` | app |
|---|---|---|---|---|---|---|
| Kotlin stdlib, `kotlinx.coroutines` core (`suspend`, `Flow`) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| kotlinx-datetime | ✅ | ✅ | ✅ (formatting in mappers) | ✅ | — | — |
| kotlinx-collections-immutable | — (use `List`) | — | ✅ | ✅ | — | — |
| kotlinx.serialization | — | ✅ DTOs only | ✅ `NavKey`s only (`presentation/navigation/`) | — | ✅ `Json` config | ✅ `NavKey`s only |
| Ktor Client | — | ✅ | — | — | ✅ create `HttpClient` | — |
| Ksoup | — | ✅ `[Name]HtmlParser` only (`data/remote/`) | — | — | — | — |
| DataStore | — | ✅ | — | — | ✅ create instance | — |
| Compose, Material 3, Adaptive | — | — | ✅ | ✅ | — | ✅ |
| Material Icons Extended | — | — | ✅ | ✅ | — | ✅ |
| Compose RichText | — | — | ✅ | ✅ | — | — |
| Coil | — | — | ✅ | ✅ | ✅ `DiskCache` provider | ✅ `ImageLoader` setup |
| Navigation 3 | — | — | ✅ `NavKey`s and entry functions (`presentation/navigation/`) only | ✅ `common/ui/navigation/` plumbing | — | ✅ `NavDisplay`, back stack, entry wiring |
| Lifecycle / ViewModel | — | — | ✅ | — | — | ✅ |
| Koin | ✅ annotations only (`@Factory`, `@Single`) | ✅ annotations only (`@Single`) | ✅ `@KoinViewModel` / `@Factory`, `koinViewModel()` in Routes only | — | ✅ `@Module`, providers | ✅ `startKoin<AppKoin>` |

---

## 3. Core language & concurrency (all layers)

### Kotlin only

- **MUST** write every file in Kotlin, including tests and build scripts (`*.gradle.kts`).
- Prefer Kotlin-first APIs: `kotlin.time.Duration` over `TimeUnit`, kotlinx-datetime over `java.time`, `buildList` / `map` / `associate` over manual loops into `ArrayList`, `require`/`check` over `IllegalArgumentException` throws, `use {}` for closeables.
- Idioms: `data class`, `sealed interface`, `value class` for typed ids, `when` exhaustiveness, extension functions for mappers, null-safety over `!!` (**NEVER** `!!` in production code).

### Coroutines & Flow

| Need | Use |
|---|---|
| One-shot async work (use case, repository call, Ktor request, DataStore write) | `suspend fun` |
| Reactive stream (DataStore read, observing a repository) | `Flow<T>` (cold) |
| Screen UI state | `StateFlow<[Name]State>` backed by `MutableStateFlow` (see `conventions` §4) |
| One-off UI side effects | `Channel<[Name]Effect>(Channel.BUFFERED)` exposed as `Flow` via `receiveAsFlow()` |
| Wrapping a callback API | `suspendCancellableCoroutine` (single value) / `callbackFlow` (stream) — in `data` only |
| Parallel work | `coroutineScope { async {} … awaitAll() }` inside a `suspend` function |

Rules:

- Launch coroutines only from `viewModelScope` (presentation) or structured `coroutineScope {}` inside `suspend` functions. **NEVER** `GlobalScope`, never an unowned `CoroutineScope(Dispatchers.IO).launch`.
- `suspend` functions are **main-safe**. Ktor and DataStore already are; switch with `withContext(dispatchers.default)` only for heavy CPU work (large mapping/parsing). Inject `DispatcherProvider` (`common/domain/util/`) rather than referencing `Dispatchers.*`, so tests can swap it.
- **NEVER** swallow `CancellationException` — wrap service calls in `safeApiCall {}` (`common/data/util/SafeApiCall.kt`), which rethrows cancellation and maps failures to `DomainError`. **NEVER** `runCatching {}` around suspend calls.
- Reactive streams reach the ViewModel through `combine(_state, stream).stateIn(viewModelScope, WhileSubscribed(5_000), …)`; user actions update `_state` with `_state.update { … }`.

`DispatcherProvider`, `safeApiCall`, `DomainError` and `ObserveAsEvents` are defined in `best-practices` §3–4.

```kotlin
// ❌ DON'T
val novels: LiveData<List<VisualNovel>> = …                      // ❌ LiveData
fun load() = runBlocking { repository.getAll() }                 // ❌ blocks the main thread
fun load(callback: (List<VisualNovel>) -> Unit) { … }            // ❌ callback API
GlobalScope.launch { repository.sync() }                         // ❌ unstructured
runCatching { service.fetch() }                                  // ❌ swallows cancellation
```

---

## 4. UI & design system (`presentation`, `common/ui`)

### Compose + Material 3

- 100% of UI is Compose. `app/MainActivity` is a thin host: `setContent { BakaMitaiTheme { AppNavigation() } }`.
- Import components from `androidx.compose.material3.*` only. Icons from `androidx.compose.material.icons.*` are fine.
- Theme tokens via `MaterialTheme.colorScheme`, `MaterialTheme.typography`, `MaterialTheme.shapes`, plus `BakaMitaiTheme.extendedColors` / `BakaMitaiTheme.spacing` for DESIGN.md tokens — never hard-coded `Color(0xFF…)` in screens (define them in `common/ui/theme/`). The palette is fixed: no dynamic color.
- Loading states are shimmering skeletons built from `common/ui/component/skeleton/` with plain Compose animation, never spinners and never a placeholder library (`best-practices` §5.3).

### Material 3 Adaptive

- Top-level destinations use **`NavigationSuiteScaffold`** (bottom bar on compact, rail on medium, drawer on expanded — chosen automatically).
- List/detail flows use the **`ListDetailPaneScaffold`** — with Navigation 3, through the list-detail scene strategy (`rememberListDetailSceneStrategy()` + `ListDetailSceneStrategy.listPane()` / `detailPane()` metadata on entries). Use `SupportingPaneScaffold` for main + supporting content.
- Layout decisions read **`currentWindowAdaptiveInfo().windowSizeClass`** in a Route/Screen-level composable and pass plain values (`columns: Int`, `isExpanded: Boolean`) down. Window size is UI-only — it never goes into a ViewModel or `State`.

```kotlin
// ✅ app/navigation/AppNavigation.kt (entry point)
@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(NovelListNavKey)
    val current = backStack.lastOrNull()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { destination ->
                item(
                    selected = current == destination.key,
                    onClick = {
                        backStack.clear()
                        backStack.add(destination.key)
                    },
                    icon = { Icon(destination.icon, contentDescription = null) },
                    label = { Text(destination.label.asString()) },   // label: UiText
                )
            }
        },
    ) {
        AppNavDisplay(backStack)
    }
}
```

```kotlin
// ✅ adaptive grid — window size read in the composable, not the ViewModel
@Composable
fun NovelListScreen(state: NovelListState, onIntent: (NovelListIntent) -> Unit) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val columns = when {
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> 4
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> 3
        else -> 2
    }
    LazyVerticalGrid(columns = GridCells.Fixed(columns)) { /* items(state.novels, key = { it.id }) */ }
}
```

### Rich text / Markdown

- Render formatted text with Compose RichText's `Markdown` inside a shared `MarkdownView` in `common/ui/component/markdown/`.
- Source markup conversion (e.g. HTML or BBCode returned by an API → Markdown) happens in the `[Name]UIModelMapper`, so the UI model already carries a Markdown `String`.

```kotlin
// ✅ common/ui/component/markdown/MarkdownView.kt
@Composable
fun MarkdownView(
    markdown: String,
    modifier: Modifier = Modifier,
) {
    RichText(modifier = modifier) {
        Markdown(content = markdown)
    }
}
```

```kotlin
// ❌ DON'T
AndroidView(factory = { TextView(it).apply { text = Html.fromHtml(html, 0) } })
```

### Images (Coil 3)

- Use `AsyncImage` by default; `SubcomposeAsyncImage` only when the loading/error slots need composables; `rememberAsyncImagePainter` only where a `Painter` is required.
- Always pass `contentDescription` (or `null` for decorative images) and a `contentScale`.
- The singleton `ImageLoader` is configured once in `app/AppApplication.kt` (implements `SingletonImageLoader.Factory`, see `structure` §5) and uses the Ktor network fetcher, so images and API share one HTTP stack.
- Wrap repeated image patterns (cover art, avatar) in a `[Name]View` (`common/ui/component/coverimage/CoverImageView.kt`).

```kotlin
// ✅ common/ui/component/coverimage/CoverImageView.kt
@Composable
fun CoverImageView(
    url: String?,
    blur: Boolean,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        placeholder = painterResource(R.drawable.cover_placeholder),
        error = painterResource(R.drawable.cover_placeholder),
        modifier = modifier
            .aspectRatio(2f / 3f)
            .clip(MaterialTheme.shapes.medium)
            .then(if (blur) Modifier.blur(24.dp) else Modifier),
    )
}
```

---

## 5. Navigation (Navigation 3)

For deep detail (scenes, multiple back stacks, results, deep links) load the `navigation-3` skill.

- **Destinations are `@Serializable` `NavKey`s**, one per screen, named `[Name]NavKey` (the `[Name]Route` name is reserved for the composable — see `conventions`). Each is declared in its owning feature's `presentation/navigation/[Name]NavKey.kt` and carries only primitive/serializable arguments (ids, not objects).
- A feature registers its entries in an `EntryProviderScope<NavKey>.[name]Entry(...)` extension (`presentation/navigation/[Name]Navigation.kt`). The **back stack**, `Navigator` and **`NavDisplay`** live only in `app/navigation/` and `common/ui/navigation/`; features never see the back stack or a `Navigator`.
- `NavDisplay` installs `rememberSaveableStateHolderNavEntryDecorator()` and `rememberViewModelStoreNavEntryDecorator()` so each entry gets its own saved state and ViewModel store.
- Arguments flow **key → Route → ViewModel**: the entry block in the feature's `[name]Entry` (`entry<NovelDetailNavKey> { key -> … }`) passes `key.id` to `NovelDetailRoute(novelId = key.id, …)`, which passes it to Koin with `koinViewModel { parametersOf(novelId) }`.
- Navigation is triggered **only** by the Route, in response to an `[Name]Effect` (or a direct callback for trivial clicks), through lambdas like `onNavigateBack: () -> Unit`, `onOpenNovel: (String) -> Unit`. Screens never navigate; they emit intents.
- Cross-feature navigation: `[name]Entry` takes lambdas (or a `[Name]NavActions` past ~4), and `app/navigation/AppEntryProvider.kt` builds them from `Navigator` and the other features' `NavKey`s. Features never import each other's navigation package.

```kotlin
// ✅ keys: feature/[name]/presentation/navigation/[Name]NavKey.kt; the graph below is the pattern, shown in one place for brevity
@Serializable
data object NovelListNavKey : NavKey

@Serializable
data class NovelDetailNavKey(val id: String) : NavKey

@Serializable
data class ScreenshotNavKey(val url: String) : NavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun AppNavDisplay(backStack: NavBackStack<NavKey>) {
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        sceneStrategy = rememberListDetailSceneStrategy<NavKey>(),
        entryProvider = entryProvider {
            entry<NovelListNavKey>(metadata = ListDetailSceneStrategy.listPane()) {
                NovelListRoute(onOpenNovel = { id -> backStack.add(NovelDetailNavKey(id)) })
            }
            entry<NovelDetailNavKey>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
                NovelDetailRoute(
                    novelId = key.id,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onOpenScreenshot = { url -> backStack.add(ScreenshotNavKey(url)) },
                )
            }
        },
    )
}
```

| ❌ Don't | ✅ Do |
|---|---|
| `navController.navigate("novel/$id")` | `backStack.add(NovelDetailNavKey(id))` in `app/navigation/` |
| `NavKey` declared in `app/navigation/` | Declared in the owning feature's `presentation/navigation/` |
| A feature importing another feature's `NavKey` or taking a `Navigator` | A lambda parameter on its `[name]Entry`, wired in `AppEntryProvider.kt` |
| `savedStateHandle.get<String>("id")` / `arguments?.getString("id")` | `entry<NovelDetailNavKey> { key -> NovelDetailRoute(novelId = key.id) }` |
| `NovelDetailScreen(navController = …)` | `NovelDetailScreen(state, onIntent)`; Route maps `Effect.NavigateBack` → `onNavigateBack()` |
| `data class NovelDetailNavKey(val novel: VisualNovel)` | Pass the id; the destination loads its own data |
| State flag `shouldNavigate = true` | `NovelDetailEffect.NavigateBack` |

---

## 6. Networking, serialization & persistence (`data`)

### Ktor Client

- **One** app-wide `HttpClient`, built in `common/data/network/HttpClientFactory.kt` and provided by a `@Single` function in `CommonModule`. Base URL, JSON, timeouts, logging and response validation are configured there (`expectSuccess = true`, so non-2xx responses throw `ClientRequestException` / `ServerResponseException`, which `safeApiCall` maps to `DomainError`).
- Per-feature API classes `[Name]Service` live in `feature/[name]/data/remote/`, take `HttpClient` in the constructor, expose `suspend` functions returning **DTOs**, and contain no mapping or business logic.
- The OkHttp artifact is used only as Ktor's **engine** — never import `okhttp3.*` directly.
- Ktor types (`HttpClient`, `HttpResponse`, `ResponseException`) **NEVER** leave `data`. Repository implementations wrap every service call in `safeApiCall {}`, which converts failures into `Result.failure(DomainError.…)`.

```kotlin
// ✅ common/data/network/HttpClientFactory.kt
fun createHttpClient(
    json: Json,
    engine: HttpClientEngine = OkHttp.create(),   // tests pass a MockEngine
): HttpClient = HttpClient(engine) {
    expectSuccess = true
    install(ContentNegotiation) { json(json) }
    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
        connectTimeoutMillis = 10_000
    }
    install(Logging) { level = LogLevel.INFO }
    defaultRequest {
        url("https://api.example.com/")   // the app's API base URL
        contentType(ContentType.Application.Json)
    }
}

val appJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}
```

```kotlin
// ✅ feature/novel/data/remote/NovelService.kt
@Single
class NovelService(private val client: HttpClient) {

    suspend fun queryNovels(request: NovelQueryDTO): VisualNovelPageDTO =
        client.post("vn") { setBody(request) }.body()
}
```

```kotlin
// ❌ DON'T
interface NovelApi {                                   // ❌ Retrofit
    @GET("vn/{id}") suspend fun get(@Path("id") id: String): VisualNovelDTO
}
val client = OkHttpClient()                            // ❌ raw OkHttp
HttpClient().get(url)                                  // ❌ ad-hoc client per call
```

### kotlinx.serialization

- `@Serializable` / `@SerialName` appear **only** on `[Name]DTO` classes (data) and `[Name]NavKey`s (navigation). **NEVER** on domain models, `UIModel`s, `UIState`s, or screen `State`.
- Always use `@SerialName` for wire names so Kotlin properties can be renamed safely.
- Use the shared `appJson` instance — don't create ad-hoc `Json {}` configurations per call.

### Jetpack DataStore

- Preferences DataStore for simple key/value settings; Proto/typed DataStore (with a kotlinx.serialization `Serializer`) for structured data.
- Exactly **one** `DataStore` instance per file, provided by a `@Single` function in a `@Module` class (e.g. the `settings` store in `CommonModule`).
- Wrapped in a `[Name]LocalDataSource` in `feature/[name]/data/local/`. Reads are `Flow`s; writes are `suspend` functions using `edit {}` / `updateData {}`.
- Handle read `IOException`s by emitting defaults; rethrow everything else.

```kotlin
// ✅ feature/settings/data/local/SettingsLocalDataSource.kt
@Single
class SettingsLocalDataSource(private val dataStore: DataStore<Preferences>) {

    val showNsfwCovers: Flow<Boolean> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[SHOW_NSFW_COVERS] ?: false }
        .distinctUntilChanged()

    suspend fun setShowNsfwCovers(value: Boolean) {
        dataStore.edit { prefs -> prefs[SHOW_NSFW_COVERS] = value }
    }

    private companion object {
        val SHOW_NSFW_COVERS = booleanPreferencesKey("show_nsfw_covers")
    }
}
```

```kotlin
// ❌ DON'T
context.getSharedPreferences("settings", MODE_PRIVATE).getBoolean("nsfw", false)   // ❌
runBlocking { dataStore.data.first() }                                              // ❌ blocking read
```

---

## 7. Dependency injection (Koin)

- Koin is wired with **Koin Annotations** through the Koin Compiler Plugin (`io.insert-koin.compiler.plugin`, no KSP). Each feature declares one `@Module @ComponentScan("com.zc.bakamitai.feature.[name]") class [Feature]Module` in `feature/[name]/di/[Name]Module.kt`. `CommonModule` (`common/di/`) scans `common` and holds provider functions for things that are not our classes (`Json`, `HttpClient`, `Clock`, `DataStore`). `app/di/AppModule.kt` aggregates them with `@Module(includes = [...])`, and `app/di/AppKoin.kt` (`@KoinApplication`) is the entry point for `startKoin<AppKoin>`.
- Annotate the classes themselves; `@ComponentScan` registers them:
  - `@Single` — services, data sources, repository implementations.
  - `@Factory` — use cases, presentation mappers.
  - `@KoinViewModel` — ViewModels; navigation arguments are `@InjectedParam` constructor parameters (nullable allowed).
  - **No manual binds**: a class with a single interface is registered under that interface automatically.
  - Use a provider function in a `@Module` class only for types you do not own. An Android `Context` parameter is resolved from `androidContext(...)`.
- **NEVER** `KoinComponent`, `by inject()`, or `get()` inside classes.
- The **only** Koin call outside `di`/`Application` is `koinViewModel()` in a `[Name]Route`.
- Navigation arguments reach the ViewModel's `@InjectedParam` parameters through `parametersOf(...)` from the Route.
- Add a Koin `verify()` unit test covering all modules (`AppModule().module().verify(...)`).

```kotlin
// ✅ feature/novel/di/NovelModule.kt
@Module
@ComponentScan("com.zc.bakamitai.feature.novel")
class NovelModule
```

```kotlin
// ✅ feature/novel/data/repository/VisualNovelRepositoryImpl.kt (bound to VisualNovelRepository automatically)
@Single
class VisualNovelRepositoryImpl(private val service: NovelService) : VisualNovelRepository
```

```kotlin
// ✅ feature/novel/presentation/NovelDetailViewModel.kt
@KoinViewModel
class NovelDetailViewModel(
    @InjectedParam private val novelId: String, // comes from parametersOf
    private val getDetails: GetVisualNovelDetailsUseCase,
) : ViewModel()
```

```kotlin
// ✅ common/di/CommonModule.kt
@Module
@ComponentScan("com.zc.bakamitai.common")
class CommonModule {
    @Single
    fun json(): Json = appJson

    @Single
    fun settingsDataStore(context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("settings") }
}
```

```kotlin
// ✅ presentation/NovelDetailRoute.kt (excerpt)
@Composable
fun NovelDetailRoute(
    novelId: String,
    onNavigateBack: () -> Unit,
    viewModel: NovelDetailViewModel = koinViewModel { parametersOf(novelId) },
) { /* see conventions §5.1 */ }
```

```kotlin
// ✅ test/…/KoinModulesTest.kt
class KoinModulesTest {
    @Test
    fun `all modules resolve`() {
        AppModule().module().verify(extraTypes = listOf(String::class, Context::class))
    }
}
```

```kotlin
// ❌ DON'T
@HiltViewModel class NovelDetailViewModel @Inject constructor(…)          // ❌ Hilt
class VisualNovelRepositoryImpl : VisualNovelRepository, KoinComponent {
    private val service: NovelService by inject()                          // ❌ service locator
}
object ServiceLocator { val repo by lazy { VisualNovelRepositoryImpl() } } // ❌ manual locator
val novelModule = module { singleOf(::VisualNovelRepositoryImpl) { bind<VisualNovelRepository>() } } // ❌ Koin DSL — annotate the class
@Single(binds = [VisualNovelRepository::class]) class VisualNovelRepositoryImpl(…)                    // ❌ redundant bind
```

---

## 8. Tech-stack checklist (run before finishing any change)

- [ ] No `.java` files and no XML layouts; all four verification commands print nothing.
- [ ] Async code is `suspend` / `Flow` / `StateFlow` / `Channel` — no LiveData, RxJava, callbacks, `runBlocking`, `GlobalScope`.
- [ ] UI is Compose + Material 3; responsive layout via Material 3 Adaptive and window size classes; loading states are `SkeletonView` skeletons, not progress indicators.
- [ ] Images via Coil `AsyncImage`; Markdown via `MarkdownView` (Compose RichText).
- [ ] Destinations are `@Serializable` `[Name]NavKey`s; keys and `[name]Entry` functions in the feature's `presentation/navigation/`, wiring in `app/navigation/AppEntryProvider.kt`, back stack and `NavDisplay` only in `app/navigation/`; Routes navigate via callbacks in response to Effects.
- [ ] Networking via the shared Ktor `HttpClient` in `data`; DTOs use kotlinx.serialization; Ktor types never leave `data`.
- [ ] Persistence via DataStore exposed as `Flow`; no `SharedPreferences`.
- [ ] DI via Koin Annotations: classes carry `@Single` / `@Factory` / `@KoinViewModel` (navigation args `@InjectedParam`), each feature has one `@Module @ComponentScan` class listed in `AppModule`; no Koin DSL (`module { }`, `singleOf`, …), no `KoinComponent` / `inject()`; `koinViewModel()` only in Routes; `KoinModulesTest` passes.
- [ ] Every library used appears in §1, and is used only in the layers allowed by §2.
- [ ] Any new or changed dependency follows `library-management`.
