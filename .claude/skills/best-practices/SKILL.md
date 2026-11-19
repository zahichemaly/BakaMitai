---
name: best-practices
description: Compose performance, Detekt/lint rules, coroutine safety, error handling, previews, accessibility, and testing standards for this Android app. Use when writing, reviewing, or testing code.
---

# Best Practices

This skill defines **production-quality rules** layered on top of:

- `structure` — where files go.
- `architecture` — layer rules and MVI flow.
- `conventions` — naming, immutability, MVI contract and Route/Screen/View templates.
- `tech-stack` — approved libraries.
- `library-management` — version catalog, `build-logic/` and dependency workflows.

Rules marked **MUST** / **NEVER** are non-negotiable. Examples use the `feature/novel` feature in `com.zc.bakamitai`.

---

## 1. Compose performance & recomposition

### 1.1 Stability

- **MUST** annotate every `[Name]State`, `[Name]UIModel` and `[Name]UIState`:
  - `@Immutable` when every property is deeply immutable (the default for this codebase).
  - `@Stable` only when the class exposes observable Compose state (`State<T>`, `SnapshotStateList`). That should be rare, because UI state lives in the ViewModel.
- **MUST** use `kotlinx.collections.immutable` (`ImmutableList`, `PersistentList`, `ImmutableMap`, `ImmutableSet`) for every collection in those classes. Compose treats plain `List`/`Map`/`Set` as unstable.

```kotlin
// ✅
@Immutable
data class NovelListState(
    val isLoading: Boolean = false,
    val items: ImmutableList<NovelListItemUIModel> = persistentListOf(),
    val error: ErrorUIModel? = null,
)

// ❌ unstable: List parameter, no annotation
data class NovelListState(val items: List<NovelListItemUIModel> = emptyList())
```

### 1.2 Lazy lists, rows & grids

- **MUST** pass a stable, unique `key` to every `items(...)` call: `key = { it.id }`. **NEVER** use the index as the key for data that can change.
- **MUST** pass `contentType` when a list renders more than one item type.
- Model heterogeneous rows as a sealed UI model that carries its own `key` and `contentType`.

```kotlin
// presentation/model/NovelListItemUIModel.kt
@Immutable
sealed interface NovelListItemUIModel {
    val key: String
    val contentType: String

    @Immutable
    data class Header(val title: String) : NovelListItemUIModel {
        override val key = "header-$title"
        override val contentType = "header"
    }

    @Immutable
    data class Novel(val novel: VisualNovelUIModel) : NovelListItemUIModel {
        override val key = novel.id
        override val contentType = "novel"
    }
}
```

```kotlin
// ✅
LazyColumn(state = listState) {
    items(
        items = state.items,
        key = { it.key },
        contentType = { it.contentType },
    ) { item ->
        when (item) {
            is NovelListItemUIModel.Header -> SectionHeaderView(title = item.title)
            is NovelListItemUIModel.Novel -> NovelCardView(model = item.novel, onClick = onNovelClick)
        }
    }
}

// ❌ index keys, no contentType
LazyColumn { itemsIndexed(state.items) { index, item -> /* … */ } }
```

### 1.3 Defer state reads & shrink recomposition scope

- Read fast-changing state (scroll offset, animation values, text input) **as low in the tree as possible**, inside the composable that needs it.
- Prefer **lambda modifiers** so changes skip composition and run only in layout/draw: `Modifier.offset { … }`, `Modifier.graphicsLayer { … }`, `Modifier.drawBehind { … }`.

```kotlin
// ✅ the scroll value is read in the placement phase only
Box(
    Modifier.graphicsLayer {
        translationY = -listState.firstVisibleItemScrollOffset * PARALLAX_FACTOR
    },
)

// ❌ recomposes this whole composable on every scroll pixel
Box(Modifier.offset(y = (-listState.firstVisibleItemScrollOffset * PARALLAX_FACTOR).dp))
```

### 1.4 `remember` & `derivedStateOf`

- Sorting, filtering, grouping and formatting belong in the ViewModel or use case. If a composable must compute something, wrap it in `remember(inputs) { … }`.
- Use `derivedStateOf` when the input changes more often than the output (scroll position → boolean).

```kotlin
// ✅
val showScrollToTop by remember {
    derivedStateOf { listState.firstVisibleItemIndex > SCROLL_TO_TOP_THRESHOLD }
}

// ❌ recomposes on every scroll frame
val showScrollToTop = listState.firstVisibleItemIndex > SCROLL_TO_TOP_THRESHOLD

// ❌ sorts on every recomposition, and it's business logic in the UI
val sorted = state.novels.sortedByDescending { it.rating }
```

### 1.5 Lambda & parameter stability

- Pass **method references** (`viewModel::onIntent`) or remembered callbacks. **NEVER** pass lambdas that capture unstable objects.
- **NEVER** pass a `ViewModel`, the back stack, a `NavController`, a domain model or a DTO into a `[Name]Screen` or `[Name]View`.
- Give reusable Views specific callbacks (`onClick: (String) -> Unit`) and build the intent once, at the Screen level.

```kotlin
// ✅ Route
NovelListScreen(state = state, onIntent = viewModel::onIntent)

// ✅ Screen: one remembered adapter, reused by every row
val onNovelClick = remember(onIntent) { { id: String -> onIntent(NovelListIntent.NovelClicked(id)) } }

// ❌
NovelListScreen(state = state, onIntent = { viewModel.onIntent(it) }, viewModel = viewModel)
```

### 1.6 Coil images

- **MUST** give every `AsyncImage` a deterministic size (`Modifier.size(…)`, `aspectRatio(…)`, or fixed width + aspect ratio) and an explicit `contentScale`. Then the layout doesn't jump when the image arrives.
- Reuse `common/ui/component/coverimage/CoverImageView` for cover art.

---

## 2. Static analysis & compiler auditing

### 2.1 Detekt + Compose rules

The codebase runs Detekt with formatting (ktlint) rules and Compose rules (`io.nlopez.compose.rules:detekt`). Setup is in `library-management` §4 (`bakamitai.detekt` convention plugin in `build-logic/`). `./gradlew detekt` **MUST** pass with zero findings and no baseline.

Generated code must comply out of the box:

| Rule | ✅ Do | ❌ Don't |
|---|---|---|
| Modifier parameter | `modifier: Modifier = Modifier` as the **first optional** parameter of every public `[Name]Screen` / `[Name]View`, applied **once** to the root node | No modifier, a modifier applied to several children, or `modifier` placed before required parameters |
| Content-emitting composables return `Unit` | `@Composable fun NovelCardView(...)` | `@Composable fun NovelCardView(...): Boolean` |
| Value-returning composables | Named like factories: `rememberXxxState()`, never emit UI | A composable that emits UI **and** returns a value |
| No `MutableState` params | `value: String, onValueChange: (String) -> Unit` | `text: MutableState<String>` |
| No ViewModel params below the Route | `state`, `onIntent` | `viewModel: NovelListViewModel` in a Screen or View |
| Small functions | Extract sub-composables; group parameters into a UI model | Deep nesting, >~60-line composables, long parameter lists of primitives |
| No magic numbers | `private const val SCROLL_TO_TOP_THRESHOLD = 2`, dimensions in `Dp` constants | `if (index > 2)`, `padding(13.dp)` repeated across files |

```kotlin
// ✅ text input
@Composable
fun SearchFieldView(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(value = value, onValueChange = onValueChange, modifier = modifier.fillMaxWidth())
}

// ❌ MutableState parameter, no modifier
@Composable
fun SearchFieldView(query: MutableState<String>) { /* … */ }
```

### 2.2 Zero blind suppression

- **NEVER** add `@Suppress(...)` to silence Detekt, lint or compiler warnings. Fix the code.
- If a suppression is genuinely unavoidable, scope it to the **smallest** declaration and add a comment on the same line saying why.

```kotlin
// ✅ rare, scoped, justified
@Suppress("LongParameterList") // Mirrors the API's filter parameters 1:1; grouping would leak DTO shape into domain.
fun buildFilters(/* … */)

// ❌
@file:Suppress("MagicNumber", "LongMethod", "TooManyFunctions")
@Suppress("UNCHECKED_CAST")
```

### 2.3 Compose compiler reports

- Compose compiler reports are enabled by the `bakamitai.android.application` convention plugin (`library-management` §4). After UI changes, build and check `app/build/compose_compiler/`:
  - `*-classes.txt`: every `State`, `UIModel` and `UIState` is listed as `stable`.
  - `*-composables.txt`: every Screen and View is `restartable skippable`, with **zero** `unstable` parameters.
- Fix instability at the source (an immutable collection, an `@Immutable` annotation, a UI model instead of a domain model). Don't paper over it with stability configuration files.

---

## 3. Coroutines, flows & concurrency

### 3.1 Lifecycle-aware collection

- `[Name]Route` **MUST** collect state with `collectAsStateWithLifecycle()`. **NEVER** `collectAsState()`.
- `[Name]Route` **MUST** collect effects with `ObserveAsEvents` (`common/ui/util/ObserveAsEvents.kt`). It wraps `LaunchedEffect` and `repeatOnLifecycle(STARTED)`, and dispatches on `Main.immediate` so events are neither dropped nor duplicated across configuration changes.

```kotlin
// ✅ common/ui/util/ObserveAsEvents.kt
@Composable
fun <T> ObserveAsEvents(
    events: Flow<T>,
    vararg keys: Any?,
    onEvent: suspend (T) -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnEvent by rememberUpdatedState(onEvent)
    LaunchedEffect(events, lifecycleOwner, *keys) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                events.collect { currentOnEvent(it) }
            }
        }
    }
}
```

```kotlin
// ✅ Route
val state by viewModel.state.collectAsStateWithLifecycle()
ObserveAsEvents(viewModel.effect) { effect ->
    when (effect) {
        NovelDetailEffect.NavigateBack -> onNavigateBack()
        is NovelDetailEffect.ShowError ->
            snackbarHostState.showSnackbar(effect.error.message.asString(context))
    }
}

// ❌
val state by viewModel.state.collectAsState()
LaunchedEffect(Unit) { viewModel.effect.collect { /* … */ } }   // keeps collecting in background, no lifecycle
```

### 3.2 Structured concurrency & dispatcher injection

- ViewModel coroutines run in `viewModelScope` only. **NEVER** `GlobalScope`, `CoroutineScope(...)` fields or `MainScope()` in app code.
- **NEVER** hardcode `Dispatchers.IO` / `Dispatchers.Default` in repositories or use cases. Inject `DispatcherProvider` (`common/domain/util/DispatcherProvider.kt`), so tests can swap in a `TestDispatcher`. The only exception is `Dispatchers.Main.immediate` inside `ObserveAsEvents`.
- Ktor and DataStore are already main-safe. Switch dispatchers only for blocking or CPU-heavy work (large mapping or parsing).

```kotlin
// ✅ common/domain/util/DispatcherProvider.kt
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}
```

```kotlin
// ✅ repository using it for CPU-heavy mapping
@Single
class VisualNovelRepositoryImpl(
    private val service: NovelService,
    private val dispatchers: DispatcherProvider,
) : VisualNovelRepository {
    override suspend fun search(query: String): Result<List<VisualNovel>> =
        safeApiCall { service.queryNovels(query.toQueryDTO()) }
            .map { page -> withContext(dispatchers.default) { page.toDomain() } }
}
```

### 3.3 Cancellation safety

- **NEVER** catch `Exception` / `Throwable` around suspend calls without rethrowing `CancellationException`.
- Use `safeApiCall` (`common/data/util/SafeApiCall.kt`), which rethrows cancellation, or catch specific exception types.

```kotlin
// ❌ swallows cancellation; the coroutine keeps running after its scope is cancelled
try { service.fetch() } catch (e: Exception) { null }
runCatching { service.fetch() }

// ✅
safeApiCall { service.fetch() }
```

### 3.4 Cold → hot conversion in ViewModels

- To expose a repository or DataStore `Flow`, **combine** it with the private `_state` and convert once with `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), [Name]State())`.
- The result is still the **single** public `state: StateFlow<[Name]State>`. User-driven changes still go through `_state.update { … }`.
- `WhileSubscribed(5_000)` keeps upstream alive across configuration changes and stops it 5 s after the app goes to the background.

```kotlin
// ✅
@KoinViewModel
class SettingsViewModel(
    observeSettings: ObserveSettingsUseCase,
    private val updateShowNsfw: UpdateShowNsfwUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> =
        combine(_state, observeSettings()) { state, settings ->
            state.copy(settings = settings.toUIModel())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = SettingsState(),
        )

    // onIntent / _effect as in conventions §4.4

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

// ❌ collected forever, even while the app is backgrounded
init { viewModelScope.launch { observeSettings().collect { s -> _state.update { it.copy(settings = s.toUIModel()) } } } }
// ❌ SharingStarted.Eagerly / Lazily for UI state
```

---

## 4. Error handling & resilience

Flow of an error: **`data`** catches it and maps it to a `DomainError`, **`domain`** passes `Result<T>` through, and **`presentation`** maps the `DomainError` to an `ErrorUIModel`.

### 4.1 Typed domain errors

```kotlin
// ✅ common/domain/model/DomainError.kt — pure Kotlin, no HTTP or Ktor types
sealed class DomainError(cause: Throwable? = null) : Exception(cause) {
    data object Network : DomainError()
    data object Timeout : DomainError()
    data object Unauthorized : DomainError()
    data object NotFound : DomainError()
    data object RateLimited : DomainError()
    data object Server : DomainError()
    data object InvalidData : DomainError()
    data class Unknown(override val cause: Throwable) : DomainError(cause)
}
```

```kotlin
// ✅ common/data/util/SafeApiCall.kt — the ONLY place that knows Ktor/serialization exceptions
suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpRequestTimeoutException) {
        Result.failure(DomainError.Timeout)
    } catch (e: ClientRequestException) {
        Result.failure(e.response.status.toDomainError())
    } catch (e: ServerResponseException) {
        Result.failure(DomainError.Server)
    } catch (e: SerializationException) {
        Result.failure(DomainError.InvalidData)
    } catch (e: IOException) {
        Result.failure(DomainError.Network)
    }

private fun HttpStatusCode.toDomainError(): DomainError = when (this) {
    HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> DomainError.Unauthorized
    HttpStatusCode.NotFound -> DomainError.NotFound
    HttpStatusCode.TooManyRequests -> DomainError.RateLimited
    else -> DomainError.Unknown(IllegalStateException("HTTP $value"))
}
```

- Every repository method that calls a service **MUST** wrap the call in `safeApiCall`, so failures reaching `domain` are always `DomainError`.
- **NEVER** let HTTP status codes, `HttpResponse`, Ktor exceptions or stack traces reach `domain` or `presentation`.
- `presentation` turns errors into UI with the shared error component: `common/ui/component/error/mapper/ErrorUIModelMapper.kt` maps a `Throwable` to an `ErrorUIModel(message: UiText, isRetryable)` (text rules: `conventions` §2.5).

```kotlin
// ✅ common/ui/component/error/mapper/ErrorUIModelMapper.kt
fun Throwable.toErrorUIModel(): ErrorUIModel = when (this) {
    DomainError.Network, DomainError.Timeout -> ErrorUIModel(UiText.StringResource(StringResource.error_network), isRetryable = true)
    DomainError.NotFound -> ErrorUIModel(UiText.StringResource(StringResource.error_not_found), isRetryable = false)
    DomainError.RateLimited -> ErrorUIModel(UiText.StringResource(StringResource.error_rate_limited), isRetryable = true)
    DomainError.Unauthorized -> ErrorUIModel(UiText.StringResource(StringResource.error_unauthorized), isRetryable = false)
    else -> ErrorUIModel(UiText.StringResource(StringResource.error_generic), isRetryable = true)
}
```

```kotlin
// ✅ ViewModel
getVisualNovelDetails(novelId)
    .onSuccess { novel -> _state.update { it.copy(isLoading = false, novel = novel.toUIModel()) } }
    .onFailure { error -> _state.update { it.copy(isLoading = false, error = error.toErrorUIModel()) } }

// ❌
.onFailure { e -> _state.update { it.copy(errorMessage = e.message) } }          // raw exception text in UI
.onFailure { e -> if ((e as ClientRequestException).response.status.value == 404) … } // HTTP in presentation
```

### 4.2 DataStore stream safety

Every `DataStore.data` read **MUST** handle `IOException` by emitting defaults and rethrow everything else:

```kotlin
dataStore.data
    .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
    .map { prefs -> prefs[SHOW_NSFW_COVERS] ?: false }
```

### 4.3 Room errors

- Room reports disk problems (corrupt database, full disk) as `SQLiteException`. The repository implementation maps it to `DomainError.Unknown(e)` in one private wrapper around every DAO call, and rethrows `CancellationException` untouched. A Room `Flow` rethrows it as `DomainError.Unknown` too (`.catch { if (it is SQLiteException) throw DomainError.Unknown(it) else throw it }`), so the ViewModel's `catch` shows the error state.
- Code that must not fail half-way (a background run's "save, then notify") checks the save's `Result` before the side effect, and runs both in `withContext(NonCancellable)`.

---

## 5. UI composition, adaptive layouts & previews

### 5.1 Stateless Screens & Views with rich previews

- `[Name]Screen` and `[Name]View` are **100% stateless**: no ViewModel, no `remember { mutableStateOf(...) }` holding business data, no side effects that load data.
- Every Screen and every View **MUST** have previews covering **light + dark** and **compact + expanded** widths, built from sample `[Name]State` / `[Name]UIModel` instances. Use the shared multi-preview annotation `@AdaptivePreviews` (`common/ui/util/AdaptivePreviews.kt`).
- Keep preview sample data `private` in the same file, or in a `PreviewParameterProvider` when several states are previewed.

```kotlin
// ✅ common/ui/util/AdaptivePreviews.kt
@Preview(name = "Compact · Light", widthDp = 360, heightDp = 800, showBackground = true)
@Preview(name = "Compact · Dark", widthDp = 360, heightDp = 800, showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Preview(name = "Expanded · Light", widthDp = 1280, heightDp = 800, showBackground = true)
@Preview(name = "Expanded · Dark", widthDp = 1280, heightDp = 800, showBackground = true, uiMode = UI_MODE_NIGHT_YES)
annotation class AdaptivePreviews
```

```kotlin
// ✅ bottom of NovelDetailScreen.kt
private class NovelDetailStateProvider : PreviewParameterProvider<NovelDetailState> {
    override val values = sequenceOf(
        NovelDetailState(isLoading = true),
        NovelDetailState(novel = sampleNovel),
        NovelDetailState(error = ErrorUIModel(UiText.StringResource(StringResource.error_network), isRetryable = true)),
    )
}

@AdaptivePreviews
@Composable
private fun NovelDetailScreenPreview(
    @PreviewParameter(NovelDetailStateProvider::class) state: NovelDetailState,
) {
    BakaMitaiTheme { NovelDetailScreen(state = state, onIntent = {}) }
}
```

### 5.2 Theme tokens, adaptive layout & accessibility

- Colors and type come from `MaterialTheme.colorScheme` / `MaterialTheme.typography` / `MaterialTheme.shapes`, and DESIGN.md tokens Material has no slot for from `BakaMitaiTheme.extendedColors` (status badges, progress gradient, glows) and `BakaMitaiTheme.spacing`. **NEVER** hardcode `Color(0xFF…)`, `fontSize = 18.sp`, `FontWeight` or spacing `Dp`s in Screens or Views.
- Responsive layout uses Material 3 Adaptive (`tech-stack` §4). Window size is read in the composable, never stored in `State`.
- Every interactive element has a touch target of at least **48.dp**. Material components (`IconButton`, `Button`, `Checkbox`) already do this; custom clickables add `Modifier.minimumInteractiveComponentSize()`.
- `contentDescription` is a meaningful localized string (`UiText.StringResource(StringResource.x).asString()`) for informative images and icons, and `null` only for decorative ones. Clickable rows that merge text use `Modifier.semantics(mergeDescendants = true) {}`.

```kotlin
// ✅
IconButton(onClick = onFavoriteClick) {
    Icon(
        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
        contentDescription = UiText.StringResource(
            if (isFavorite) StringResource.cd_remove_favorite else StringResource.cd_add_favorite,
        ).asString(),
    )
}

// ❌ hardcoded color, 24dp touch target, no description
Icon(Icons.Filled.Favorite, null, tint = Color(0xFFE91E63), modifier = Modifier.size(24.dp).clickable { … })
```

### 5.3 Loading states: shimmering skeletons, never spinners

While content loads, show a **skeleton of that content** with a shimmer sweeping across it, never a spinner. The building blocks are in `common/ui/component/skeleton/SkeletonView.kt`:

| Piece | Use |
|---|---|
| `SkeletonView(modifier) { … }` | Wraps a whole skeleton. Sweeps **one** highlight band across everything inside, and TalkBack reads it as a single "Loading" item. |
| `SkeletonBlock(modifier, shape)` | Stands in for an image, chip, badge or any shape. Give it the real size and shape. |
| `SkeletonTextLine(style, modifier)` | Stands in for one line of text. It's exactly one line of `style` tall, so it follows the font scale. Set its width with `fillMaxWidth(fraction)` or `width(…)`. |
| `textLineHeight(style)` | One line's height. Use it with the real padding for pill skeletons (chips, badges). |

- **NEVER** use `CircularProgressIndicator`, `LinearProgressIndicator` or `LoadingIndicator` as the loading state of a screen, section, list, card or image. The only progress indicators allowed are:
  - the one built into `PullToRefreshBox` while content that's already on screen refreshes;
  - determinate progress that shows a real value (download or episode progress). That's data, not loading.
- **Screen first load:** when `state.isLoading` (nothing to show yet), `[Name]Screen` shows a private `[Name]Skeleton(modifier)` in the same file. It:
  - wraps everything in **one** `SkeletonView`, never one per item;
  - mirrors the content layout: the same container, padding, spacing, columns and window-size rules (share a private helper with the content, e.g. the grid), with `userScrollEnabled = false`, so nothing moves when the content replaces it;
  - shows a fixed number of placeholders per section (about one screen's worth), with no fake `UIModel`s or sample data;
  - renders static chrome for real: the top app bar, section titles and any text that doesn't come from the data.
- **Component skeletons:** every component that renders loaded data has a `[Name]SkeletonView(modifier: Modifier = Modifier)` **in the same file** as `[Name]View`. It reuses the component's private dimensions, container color and shape, with `SkeletonBlock`s and `SkeletonTextLine`s in place of the data. Size it like the real component (e.g. reserve both title lines when the title has `minLines = 2`).
- **Partial loads:** a section that loads after the rest of the screen (e.g. the Details downloads below an already-loaded header) shows its own `SkeletonView` in that section's place.
- Placeholders shimmer only inside a `SkeletonView`. Don't give its content a background (the band is drawn only over the placeholders' pixels), and never nest one `SkeletonView` in another.
- **Colors:** blocks use `colorScheme.surfaceContainerHighest` and the band uses `BakaMitaiTheme.extendedColors.shimmerHighlight`, both through the shared pieces. Never hardcode them or roll your own shimmer.
- **Performance:** `SkeletonView` reads its animation in the draw phase only, so the shimmer never recomposes the placeholders. Keep it that way.
- **Previews:** every `[Name]SkeletonView` gets an `@AdaptivePreviews` preview wrapped in a `SkeletonView`. The Screen's `PreviewParameterProvider` keeps its `isLoading = true` state, which renders the skeleton.

```kotlin
// ✅ component/novelheader/NovelHeaderView.kt, next to NovelHeaderView (same private dimensions)
@Composable
fun NovelHeaderSkeletonView(modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(BakaMitaiTheme.spacing.md), horizontalArrangement = Arrangement.spacedBy(BakaMitaiTheme.spacing.sm)) {
        SkeletonBlock(Modifier.width(CoverWidth).aspectRatio(COVER_ASPECT_RATIO), shape = MaterialTheme.shapes.medium)
        Column(Modifier.weight(1f)) {
            SkeletonTextLine(MaterialTheme.typography.titleLarge, Modifier.fillMaxWidth())
            SkeletonTextLine(MaterialTheme.typography.bodyMedium, Modifier.fillMaxWidth(RELEASE_LINE_FRACTION))
        }
    }
}

// ✅ NovelDetailScreen.kt
when {
    state.isLoading -> NovelDetailSkeleton(Modifier.padding(padding))   // private: SkeletonView { NovelHeaderSkeletonView() … }
    …
}

// ❌ a spinner, or one SkeletonView per row
state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
```

---

## 6. Testing standards

### 6.1 Layout & tooling

- Unit tests live in `app/src/test/java/com/zc/bakamitai/` and **mirror the main package tree**. For example, `feature/novel/domain/usecase/GetVisualNovelDetailsUseCaseTest.kt`.
- Test classes are named `[ClassUnderTest]Test`. Test names are backtick sentences: `` `returns NotFound when the API responds 404` ``.
- Tools: JUnit 4, `kotlin.test` assertions, `kotlinx-coroutines-test` (`runTest`, `StandardTestDispatcher`), Turbine for flows, Ktor `MockEngine` for HTTP.
- Prefer **hand-written fakes** (`Fake[Name]Repository`, `TestDispatcherProvider`) over mocking frameworks. Keep fakes in the test source set next to the tests that use them, mirroring the main tree.

```kotlin
// ✅ test: common/util/MainDispatcherRule.kt
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(testDispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
```

```kotlin
// ✅ test: common/domain/util/TestDispatcherProvider.kt
class TestDispatcherProvider(dispatcher: TestDispatcher) : DispatcherProvider {
    override val main = dispatcher
    override val io = dispatcher
    override val default = dispatcher
}
```

### 6.2 `domain`: every use case

Pure Kotlin tests with a fake repository. Cover success, each relevant `DomainError`, and any business rule in the use case.

```kotlin
class GetVisualNovelDetailsUseCaseTest {

    private val repository = FakeVisualNovelRepository()
    private val useCase = GetVisualNovelDetailsUseCase(repository)

    @Test
    fun `returns the novel from the repository`() = runTest {
        repository.novels["v17"] = sampleNovel
        assertEquals(Result.success(sampleNovel), useCase("v17"))
    }

    @Test
    fun `propagates NotFound`() = runTest {
        assertEquals(DomainError.NotFound, useCase("missing").exceptionOrNull())
    }
}
```

```kotlin
// test: feature/novel/domain/repository/FakeVisualNovelRepository.kt
class FakeVisualNovelRepository : VisualNovelRepository {
    val novels = mutableMapOf<String, VisualNovel>()
    override suspend fun getById(id: String): Result<VisualNovel> =
        novels[id]?.let { Result.success(it) } ?: Result.failure(DomainError.NotFound)
    override fun observeFavorites(): Flow<List<VisualNovel>> = flowOf(novels.values.toList())
}
```

### 6.3 `data`: services with `MockEngine`, and mappers' edge cases

- Test each `[Name]Service` against a `MockEngine` passed to `createHttpClient(appJson, engine)`. Assert the request (method, path, body) and the parsed DTO.
- Test `safeApiCall` mapping through the repository: 404 → `NotFound`, 500 → `Server`, malformed JSON → `InvalidData`, timeout → `Timeout`.
- Test every `[Name]DTOMapper` / `[Name]DomainMapper` with nulls, missing optional fields, empty lists and boundary values.

```kotlin
class NovelServiceTest {

    @Test
    fun `queryNovels posts to vn and parses the page`() = runTest {
        val engine = MockEngine { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("/vn", request.url.encodedPath)
            respond(
                content = """{"results":[{"id":"v17","title":"Ever17"}],"more":false}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val service = NovelService(createHttpClient(appJson, engine))

        val page = service.queryNovels(NovelQueryDTO(filters = listOf("id", "=", "v17")))

        assertEquals("Ever17", page.results.single().title)
        assertNull(page.results.single().image)
    }
}
```

```kotlin
class VisualNovelDTOMapperTest {

    @Test
    fun `missing release and image map to null and SFW`() {
        val domain = VisualNovelDTO(id = "v1", title = "T", released = null, rating = null, image = null).toDomain()
        assertNull(domain.releaseDate)
        assertNull(domain.coverUrl)
        assertFalse(domain.isNsfwCover)
    }
}
```

### 6.4 `presentation`: ViewModels and UI mappers

- Test every `[Name]ViewModel` for **state transitions** (initial → loading → content/error) and **effect sequences**, using `MainDispatcherRule` and Turbine. Build ViewModels with real use cases backed by fake repositories.
- `Navigator` and every `NavKey`'s serialization are unit tested (`NavigatorTest`, `NavKeySerializationTest`); a new `NavKey` is added to the serialization test's sample list.

### 6.5 Instrumented tests (`androidTest`)

- Code that needs Android to run (Room DAOs, WorkManager workers and schedulers, notification posting, `Intent`s) is tested in `app/src/androidTest/java/com/zc/bakamitai/`, mirroring the main tree, with `./gradlew connectedDebugAndroidTest` on an emulator.
  - DAOs: `Room.inMemoryDatabaseBuilder`.
  - Workers: `TestListenableWorkerBuilder` with a `WorkerFactory` over fakes; schedulers: `WorkManagerTestInitHelper`.
- Keep the logic out of these classes: rules (dedup, schedules, retry ladders) live in pure domain code with thorough unit tests. Instrumented tests only check the platform wiring.
- Test every `[Name]UIModelMapper` for formatting: dates, ratings, empty/null fallbacks, markup → Markdown.

```kotlin
class NovelDetailViewModelTest {

    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeVisualNovelRepository()

    private fun createViewModel() = NovelDetailViewModel(
        novelId = "v17",
        getVisualNovelDetails = GetVisualNovelDetailsUseCase(repository),
        toggleFavorite = ToggleFavoriteUseCase(repository),
    )

    @Test
    fun `load emits loading then content`() = runTest {
        repository.novels["v17"] = sampleNovel
        val viewModel = createViewModel()

        viewModel.state.test {
            assertEquals(NovelDetailState(isLoading = true), awaitItem())
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(UiText.StringValue("Ever17"), loaded.novel?.title)
        }
    }

    @Test
    fun `missing novel shows a non-retryable error`() = runTest {
        val viewModel = createViewModel()
        viewModel.state.test {
            skipItems(1)
            assertEquals(UiText.StringResource(StringResource.error_not_found), awaitItem().error?.message)
        }
    }

    @Test
    fun `back click emits NavigateBack`() = runTest {
        val viewModel = createViewModel()
        viewModel.effect.test {
            viewModel.onIntent(NovelDetailIntent.BackClicked)
            assertEquals(NovelDetailEffect.NavigateBack, awaitItem())
        }
    }
}
```

```kotlin
class VisualNovelUIModelMapperTest {
    @Test
    fun `unknown release date shows TBA resource`() {
        assertEquals(
            UiText.StringResource(StringResource.release_tba),
            sampleNovel.copy(releaseDate = null).toUIModel().releaseLabel,
        )
    }
}
```

---

## 7. Commands (must pass before finishing)

```bash
./gradlew detekt
```

```bash
./gradlew testDebugUnitTest
```

```bash
./gradlew assembleRelease && grep -rn "unstable" app/build/compose_compiler/*-composables.txt
```

The last `grep` must print nothing for Screens and Views.

When `app/src/androidTest/` or the code it covers changed, also run (needs an emulator; it uninstalls the app afterwards):

```bash
./gradlew connectedDebugAndroidTest
```

## 8. Checklist

- [ ] Every State, UIModel and UIState is `@Immutable` (or `@Stable`) and uses only immutable collections.
- [ ] Every lazy `items(...)` has a stable `key`, plus `contentType` when the list mixes item types.
- [ ] Fast-changing state is read late; lambda modifiers are used; `derivedStateOf` / `remember` are used where needed; no sorting or filtering in composables.
- [ ] Callbacks are method references or remembered; no ViewModel, navigation, domain or DTO objects below the Route.
- [ ] `AsyncImage` has explicit size and `contentScale`.
- [ ] `./gradlew detekt` passes; no unexplained `@Suppress`; compose reports show no unstable parameters.
- [ ] Routes use `collectAsStateWithLifecycle()` and `ObserveAsEvents`.
- [ ] No hardcoded dispatchers (only `DispatcherProvider`); `CancellationException` is never swallowed; Flows reach the ViewModel through `stateIn(WhileSubscribed(5_000))`.
- [ ] Network failures go through `safeApiCall` → `DomainError` → `ErrorUIModel`; DataStore reads `.catch` `IOException`; Room's `SQLiteException` becomes `DomainError.Unknown`.
- [ ] User-visible text in State/UIModel/UIState/Effect is `UiText`, resolved with `asString()`; strings are referenced as `StringResource.x` / `PluralResource.x` (never `R.string` / `R.plurals`), and nothing outside `UiText` calls `stringResource()` / `getString()` or carries a raw resource id.
- [ ] Screens and Views are stateless, with `@AdaptivePreviews`; theme tokens only; 48.dp touch targets; meaningful `contentDescription`s.
- [ ] Loading states are shimmering skeletons (§5.3): one `SkeletonView` per screen or section, mirroring the content layout, built from each component's `[Name]SkeletonView`; no `CircularProgressIndicator` / `LinearProgressIndicator` except `PullToRefreshBox`'s and determinate progress.
- [ ] Tests exist for every new use case, service, mapper and ViewModel, and they pass; new `NavKey`s are in `NavKeySerializationTest`; DAOs, workers and notifiers have instrumented tests.
