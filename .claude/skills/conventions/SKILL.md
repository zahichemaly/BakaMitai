---
name: conventions
description: Naming, immutability, mapper, MVI contract, and Compose conventions for this Android app. Use when writing or refactoring Kotlin code.
---

# Conventions

This skill defines **how files and classes are named and shaped**. Where files live is defined in `structure`, which layer may depend on which in `architecture`, and which libraries to use (Koin, Ktor, kotlinx.serialization, DataStore, Navigation 3, Coil, kotlinx-datetime, …) in `tech-stack`. Production-quality rules (performance, Detekt, error handling, testing) are in `best-practices`, and dependency declaration in `library-management`. Load all six when generating code.

Rules marked **MUST** / **NEVER** are non-negotiable. Examples use the feature `feature/novel` in package `com.zc.bakamitai.feature.novel`.

## Naming cheat sheet

| Artifact | Layer / package | File & class name | Example |
|---|---|---|---|
| Use case | `domain/usecase` | `[Verb][Name]UseCase` | `GetVisualNovelDetailsUseCase` |
| Repository interface | `domain/repository` | `[Name]Repository` | `VisualNovelRepository` |
| Repository impl | `data/repository` | `[Name]RepositoryImpl` | `VisualNovelRepositoryImpl` |
| Remote service (Ktor) | `data/remote` | `[Name]Service` | `NovelService` |
| Data source | `data/remote`, `data/local` | `[Name]RemoteDataSource` / `[Name]LocalDataSource` | `NovelRemoteDataSource` |
| DTO | `data/dto` | `[Name]DTO` | `VisualNovelDTO` |
| DTO → Domain mapper | `data/mapper` | `[Name]DTOMapper.kt` | `VisualNovelDTOMapper.kt` |
| Domain → DTO mapper | `data/mapper` | `[Name]DomainMapper.kt` | `VoteRequestDomainMapper.kt` |
| Domain model | `domain/model` | `[Name]` (no suffix) | `VisualNovel` |
| Static UI model | `presentation/model` | `[Name]UIModel` | `VisualNovelUIModel` |
| Dynamic UI model | `presentation/model` | `[Name]UIState` | `ScreenshotUIState` |
| Domain → UI mapper | `presentation/mapper` | `[Name]UIModelMapper.kt` | `VisualNovelUIModelMapper.kt` |
| Screen state | `presentation` | `[Screen]State` | `NovelDetailState` |
| Intents | `presentation` | `[Screen]Intent` | `NovelDetailIntent` |
| Effects | `presentation` | `[Screen]Effect` | `NovelDetailEffect` |
| ViewModel | `presentation` | `[Screen]ViewModel` | `NovelDetailViewModel` |
| Navigation 3 key | `presentation/navigation/[Screen]NavKey.kt` | `[Screen]NavKey` (`@Serializable`) | `NovelDetailNavKey` |
| Entry registration | `presentation/navigation/[Screen]Navigation.kt` | `EntryProviderScope<NavKey>.[screen]Entry(...)` | `novelDetailEntry` |
| Navigate-to helper | `presentation/navigation/[Screen]Navigation.kt` | `Navigator.navigateTo[Screen](args)` (one per key other features open) | `Navigator.navigateToNovelDetail(id)` |
| Navigation actions (only when a Route has more than ~4 navigation callbacks) | `presentation/navigation/[Screen]NavActions.kt` | `[Screen]NavActions` | `NovelDetailNavActions` |
| Nav destination composable | `presentation` | `[Screen]Route` | `NovelDetailRoute` |
| Stateless screen | `presentation` | `[Screen]Screen` | `NovelDetailScreen` |
| Reusable widget | `presentation/component/[name]/` or `common/ui/component/[name]/` | `[Name]View` | `component/ratingbadge/RatingBadgeView` |
| A component's loading placeholder | same file as its `[Name]View` | `[Name]SkeletonView` | `ReleaseRowSkeletonView` in `component/releaserow/ReleaseRowView.kt` |
| A screen's loading layout | `private` in `[Screen]Screen.kt` | `[Screen]Skeleton` | `HomeSkeleton` |
| Koin module | `di` | file `[Name]Module.kt`, `@Module class [Name]Module` | `NovelModule.kt` → `class NovelModule` |

Acronyms in suffixes are **all caps**: `DTO`, `UI` — never `Dto`, `Ui`, `UiModel`, `UiState`. The one exception is the shared text type `UiText` (`common/ui/text/UiText.kt`, see §2.5).

**File name == primary declaration name.** One public top-level class/interface per file (sealed hierarchies nest their members inside the sealed interface).

---

## 1. Use cases

- File & class: `[Verb][Name]UseCase.kt` — start with a verb: `Get`, `Observe`, `Search`, `Submit`, `Update`, `Delete`, `Toggle`, `Validate`.
- **Single responsibility**: one business action per class.
- Expose exactly **one** public method: `operator fun invoke(...)` (`suspend` for one-shot, returning `Result<T>`; non-suspend returning `Flow<T>` for streams).
- Take repository **interfaces** (or other use cases) as plain constructor parameters — no annotations; annotated with `@Factory` and picked up by the feature module's `@ComponentScan`.
- No state, no `var`s, no Android types.

```kotlin
// ✅ domain/usecase/GetVisualNovelDetailsUseCase.kt
package com.zc.bakamitai.feature.novel.domain.usecase

import com.zc.bakamitai.feature.novel.domain.model.VisualNovel
import com.zc.bakamitai.feature.novel.domain.repository.VisualNovelRepository
import org.koin.core.annotation.Factory

@Factory
class GetVisualNovelDetailsUseCase(
    private val repository: VisualNovelRepository,
) {
    suspend operator fun invoke(id: String): Result<VisualNovel> = repository.getById(id)
}
```

```kotlin
// ✅ domain/usecase/ObserveFavoriteNovelsUseCase.kt
@Factory
class ObserveFavoriteNovelsUseCase(
    private val repository: VisualNovelRepository,
) {
    operator fun invoke(): Flow<List<VisualNovel>> =
        repository.observeFavorites().map { list -> list.sortedBy { it.title } }
}
```

| ❌ Don't | ✅ Do |
|---|---|
| `NovelUseCases` with `getNovel()`, `deleteNovel()`, `rateNovel()` | Three classes: `GetVisualNovelUseCase`, `DeleteVisualNovelUseCase`, `RateVisualNovelUseCase` |
| `fun execute(id: String)` / `fun run()` | `operator fun invoke(id: String)` |
| `NovelInteractor`, `NovelManager`, `GetNovel` | `GetVisualNovelUseCase` |
| Injecting `VisualNovelRepositoryImpl` | Injecting `VisualNovelRepository` |

---

## 2. Models

### 2.1 DTOs (`data/dto`)

- Name: `[Name]DTO.kt`, class `[Name]DTO`.
- **One DTO per file.** Every nested JSON object gets its **own** `[Name]DTO.kt`. **NEVER** nest DTO classes or declare several DTOs in one file — including response wrappers.
- DTOs mirror the wire format: nullable fields where the API may omit them, `@SerialName` for snake_case keys. No logic, no defaults that hide missing data.
- DTOs are `internal`-ish by convention: referenced only from `data`.

```kotlin
// ❌ DON'T — multiple / nested DTOs in one file
@Serializable
data class VisualNovelResponseDTO(val results: List<Result>, val more: Boolean) {
    @Serializable data class Result(val id: String, val image: Image?)   // ❌ nested
    @Serializable data class Image(val url: String)                       // ❌ nested
}
```

```kotlin
// ✅ data/dto/VisualNovelPageDTO.kt
@Serializable
data class VisualNovelPageDTO(
    @SerialName("results") val results: List<VisualNovelDTO>,
    @SerialName("more") val more: Boolean,
)
```

```kotlin
// ✅ data/dto/VisualNovelDTO.kt
@Serializable
data class VisualNovelDTO(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("released") val released: String?,
    @SerialName("rating") val rating: Double?,
    @SerialName("image") val image: ImageDTO?,
)
```

```kotlin
// ✅ data/dto/ImageDTO.kt
@Serializable
data class ImageDTO(
    @SerialName("url") val url: String,
    @SerialName("sexual") val sexual: Double?,
)
```

### 2.2 Domain models (`domain/model`)

- Name: `[Name].kt` — **no suffix**. Never `VisualNovelDomain`, `VisualNovelModel`, `VisualNovelEntity`, `VisualNovelData`, `VisualNovelBO`.
- One domain model per file (a small closed `enum class` or `sealed interface` tightly bound to the model may share the file only if it is used nowhere else — prefer its own file).
- Pure Kotlin `data class` with `val`s; use meaningful types (`kotlinx.datetime.LocalDate`, `kotlin.time.Duration`, enums, value classes) rather than raw API strings.
- **NEVER** serialization, Parcelable, DI, or Compose annotations.

```kotlin
// ✅ domain/model/VisualNovel.kt
package com.zc.bakamitai.feature.novel.domain.model

import kotlinx.datetime.LocalDate

data class VisualNovel(
    val id: String,
    val title: String,
    val releaseDate: LocalDate?,
    val rating: Double?,           // 10..100 scale as returned by the API
    val coverUrl: String?,
    val isNsfwCover: Boolean,
)
```

### 2.3 UI models & UI states (`presentation/model`)

Pick the suffix by **whether the user can change it**:

| Suffix | Use for | Examples |
|---|---|---|
| `[Name]UIModel` | Static, display-ready, read-only data mapped from domain | `VisualNovelUIModel` (formatted date, rating text), list rows, cards, chips |
| `[Name]UIState` | Dynamic/interactive sub-models whose values change through interaction or lifecycle | `SearchFieldUIState` (text, error, isFocused), `ScreenshotUIState` (isSelected), `SectionUIState` (isExpanded) |

- UI models hold **display-ready** values: `UiText` for every user-visible text (§2.5), `ImageVector` icons, resolved booleans, `ImmutableList`s. A Composable should never format, parse, or compute business rules.
- They **may** reference Android/Compose types where useful (`UiText`, `ImageVector`) — they live in `presentation`.

### 2.4 Maximum immutability (UIModel, UIState, screen State)

**MUST**:

- `data class` with **`val`-only** properties.
- Collections are `kotlinx.collections.immutable.ImmutableList` / `ImmutableSet` / `ImmutableMap` (use `persistentListOf()`, `toImmutableList()`, `toPersistentList()`). Never plain `List`/`Set`/`Map`, which Compose treats as unstable.
- Updates happen **only** through `.copy(...)` producing a new instance.
- **Always** annotate the class with `@Immutable` (or `@Stable` in the rare case it exposes observable Compose state). See `best-practices` §1.1.

**NEVER**:

- `var` properties.
- `MutableList`, `MutableSet`, `MutableMap`, `ArrayList`, `HashMap`, `mutableStateOf`, `MutableStateFlow`, arrays (`Array<T>`, `IntArray`) inside these classes.
- Mutating in place: `state.items.add(x)`, `(list as MutableList).remove(x)`, `uiState.isSelected = true`.

```kotlin
// ❌ DON'T
data class NovelListState(
    var isLoading: Boolean,                              // ❌ var, no default
    val novels: MutableList<VisualNovelUIModel>,         // ❌ mutable collection
    val selected: ArrayList<String> = arrayListOf(),     // ❌ mutable collection
)
```

```kotlin
// ✅ presentation/model/VisualNovelUIModel.kt
@Immutable
data class VisualNovelUIModel(
    val id: String,
    val title: UiText,
    val releaseLabel: UiText,          // "Mar 2021" or StringResource.release_tba
    val ratingLabel: UiText?,          // "8.42"
    val coverUrl: String?,             // not user-visible text → String
    val blurCover: Boolean,
    val tags: ImmutableList<UiText> = persistentListOf(),
)
```

```kotlin
// ✅ presentation/model/ScreenshotUIState.kt
@Immutable
data class ScreenshotUIState(
    val url: String,
    val isSelected: Boolean = false,
)
```

```kotlin
// ✅ updating a nested item in State
_state.update { state ->
    state.copy(
        screenshots = state.screenshots
            .map { if (it.url == url) it.copy(isSelected = !it.isSelected) else it }
            .toImmutableList(),
    )
}
```

### 2.5 UI text & string resources (`UiText`, `ResourceTypes.kt`)

Everything the app shows as text is a `UiText` (`common/ui/text/UiText.kt`), and `UiText` is the **only** code that resolves resources. String resources are referenced through the aliases in `common/ui/resource/ResourceTypes.kt`, never through `R`.

```kotlin
// common/ui/resource/ResourceTypes.kt
typealias StringResource = com.zc.bakamitai.R.string    // StringResource.app_wordmark
typealias PluralResource = com.zc.bakamitai.R.plurals   // PluralResource.detail_votes (add it with the first <plurals>: R.plurals doesn't exist before)
typealias FontResource = com.zc.bakamitai.R.font        // FontResource.space_grotesk (used only by theme/Type.kt)

// common/ui/text/UiText.kt
sealed interface UiText {
    data class StringValue(val value: String) : UiText                                              // hardcoded / API data
    data class StringResource(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText     // StringResource.*
    data class StringPlural(@PluralsRes val id: Int, val quantity: Int, val args: List<Any> = emptyList()) : UiText // PluralResource.*
    @Composable fun asString(): String
    fun asString(context: Context): String
}
```

```kotlin
// ✅ every string, everywhere (composables included)
Text(UiText.StringResource(StringResource.login_connect).asString())
Icon(icon, contentDescription = UiText.StringResource(StringResource.cd_log_out).asString())
label = UiText.StringResource(StringResource.detail_tab_with_count, listOf(name, count))   // in a mapper
UiText.StringPlural(PluralResource.detail_votes, count, listOf(count))
snackbarHostState.showSnackbar(effect.message.asString(context))                           // outside composition
```

**MUST**:

- Every user-visible text in a `UIModel`, `UIState`, screen `State`, `Effect` and View parameter is typed `UiText` (or `UiText?`), never `String`, `Int` or `@StringRes Int`. That includes API data, which the mapper wraps in `UiText.StringValue(...)`.
- Reference strings as `StringResource.x` / `PluralResource.x`, wrapped in `UiText.StringResource(...)` / `UiText.StringPlural(...)`.
- Composables resolve text at the last moment with `.asString()`; code outside composition (snackbars in a Route effect, share choosers) uses `.asString(context)`. This applies to static labels and `contentDescription`s too.
- A label chosen by a `when` is a function returning `UiText`: `private fun Genre.toLabel(): UiText = UiText.StringResource(when (this) { … StringResource.genre_romance … })`. Properties and parameters hold that `UiText`, not a resource id.
- Non-display strings (ids, URLs, query keys) stay `String`.

**NEVER** (Detekt `ForbiddenImport` fails the build on the first three):

- `import com.zc.bakamitai.R` / `R.string.*` / `R.plurals.*` — use `StringResource.*` / `PluralResource.*`.
- `stringResource(...)` / `pluralStringResource(...)` — use `UiText…asString()`. Only `UiText.kt` calls them, by their full names.
- `context.getString(...)` / `resources.getQuantityString(...)` — use `UiText…asString(context)`.
- Typealiases for resource ids (`typealias StringResourceId = Int`, …) or properties/parameters/return types that carry a raw resource id outside `UiText`.
- `String.format` / string templates on localized text in a composable — put the args in `UiText.StringResource(id, args)`.
- `UiText`, `StringResource` or `PluralResource` in `domain` or `data`.

| ❌ Don't | ✅ Do |
|---|---|
| `Text(stringResource(R.string.login_connect))` | `Text(UiText.StringResource(StringResource.login_connect).asString())` |
| `UiText.StringResource(R.string.x)` | `UiText.StringResource(StringResource.x)` |
| `val title: String` in a UIModel | `val title: UiText` + `UiText.StringValue(title)` in the mapper |
| `@StringRes val messageRes: Int` / `val label: Int` | `val message: UiText` / `val label: UiText` |
| `private fun Tab.labelRes(): Int = when (this) { … }` | `private fun Tab.toLabel(): UiText = UiText.StringResource(when (this) { … })` |
| `context.getString(R.string.share_chooser)` in a Route | `UiText.StringResource(StringResource.share_chooser).asString(context)` |
| `"$count novels"` in a mapper | `UiText.StringPlural(PluralResource.novel_count, count, listOf(count))` |

Check (must print nothing):

```bash
grep -rnE "R\.(string|plurals)\.|import com\.zc\.bakamitai\.R$|[^.]stringResource\(|pluralStringResource\(|getString\(|getQuantityString\(" app/src --include=*.kt | grep -v "common/ui/text/UiText.kt"
```

---

## 3. Mappers

- **One mapper file per direction per boundary.** Never bundle unrelated mappers (`Mappers.kt`, `NovelMappers.kt`) or put mappers inside model files.
- Implement as top-level **extension functions** named `toDomain()`, `toDTO()`, `toUIModel()` / `toUIState()`.
  - If a presentation mapper needs dependencies (e.g. a locale-aware formatter or `ResourceProvider`), make it a class with the **same file name** — `class VisualNovelUIModelMapper(private val formatter: …) { fun map(novel: VisualNovel): VisualNovelUIModel }` — annotate it with `@Factory` and inject it into the ViewModel.
- Mappers are pure: no I/O, no logging side effects, no throwing for missing optional data (choose a sensible default or `null`).

| Direction | Layer | File | Function |
|---|---|---|---|
| `DTO → Domain` | `data/mapper` | `[Name]DTOMapper.kt` | `fun [Name]DTO.toDomain(): [Name]` |
| `Domain → DTO` (request payloads only) | `data/mapper` | `[Name]DomainMapper.kt` | `fun [Name].toDTO(): [Name]DTO` |
| `Domain → UIModel/UIState` | `presentation/mapper` | `[Name]UIModelMapper.kt` | `fun [Name].toUIModel(): [Name]UIModel` |

**NEVER**:

- Use a DTO in `domain` or `presentation` (no `fun VisualNovelDTO.toUIModel()`).
- Put `@Serializable`/`@SerialName` on domain or UI models to "skip" a mapper.
- Map in a Composable (`novel.toUIModel()` inside `@Composable`) — mapping happens in the ViewModel.
- Write `UIModel → Domain` mappers to send data back; build domain inputs from the intent's primitive values in the ViewModel.

```kotlin
// ✅ data/mapper/VisualNovelDTOMapper.kt
package com.zc.bakamitai.feature.novel.data.mapper

fun VisualNovelDTO.toDomain(): VisualNovel = VisualNovel(
    id = id,
    title = title,
    releaseDate = released?.let(::parseReleaseDate),
    rating = rating,
    coverUrl = image?.url,
    isNsfwCover = (image?.sexual ?: 0.0) >= NSFW_THRESHOLD,
)

fun VisualNovelPageDTO.toDomain(): List<VisualNovel> = results.map { it.toDomain() }

private const val NSFW_THRESHOLD = 1.0
```

```kotlin
// ✅ data/mapper/VoteRequestDomainMapper.kt
fun VoteRequest.toDTO(): VoteRequestDTO = VoteRequestDTO(
    vnId = novelId,
    vote = score * 10,
)
```

```kotlin
// ✅ presentation/mapper/VisualNovelUIModelMapper.kt
package com.zc.bakamitai.feature.novel.presentation.mapper

private val releaseFormat = LocalDate.Format {
    monthName(MonthNames.ENGLISH_ABBREVIATED); char(' '); year()
}

fun VisualNovel.toUIModel(): VisualNovelUIModel = VisualNovelUIModel(
    id = id,
    title = UiText.StringValue(title),
    releaseLabel = releaseDate?.let { UiText.StringValue(releaseFormat.format(it)) }
        ?: UiText.StringResource(StringResource.release_tba),
    ratingLabel = rating?.let { UiText.StringValue("%.2f".format(it / 10)) },
    coverUrl = coverUrl,
    blurCover = isNsfwCover,
)

fun List<VisualNovel>.toUIModels(): ImmutableList<VisualNovelUIModel> =
    map { it.toUIModel() }.toImmutableList()
```

---

## 4. MVI contract & ViewModel

Each screen has **four separate files** in `presentation/`: `[Name]ViewModel.kt`, `[Name]State.kt`, `[Name]Intent.kt`, `[Name]Effect.kt`. Never declare State/Intent/Effect inside the ViewModel file or in a single `[Name]Contract.kt`.

### 4.1 `[Name]State.kt`

- Immutable `data class`, **every property has a default** so `[Name]State()` is a valid initial state.
- Composes `UIModel`s / `UIState`s plus screen-wide flags (`isLoading`, `isRefreshing`) and `error: ErrorUIModel?` (from `common/ui/component/error/model/`).
- No domain models, no DTOs, no raw exception messages, no one-off event flags.

```kotlin
// ✅ presentation/NovelDetailState.kt
@Immutable
data class NovelDetailState(
    val isLoading: Boolean = false,
    val error: ErrorUIModel? = null,
    val novel: VisualNovelUIModel? = null,
    val screenshots: ImmutableList<ScreenshotUIState> = persistentListOf(),
    val isFavorite: Boolean = false,
) {
    val showContent: Boolean get() = novel != null && !isLoading
}
```

```kotlin
// ❌ DON'T
data class NovelDetailState(
    val novel: VisualNovel? = null,           // ❌ domain model
    val navigateBack: Boolean = false,        // ❌ one-off event as state
    val showSnackbar: Boolean = false,        // ❌ one-off event as state
)
```

### 4.2 `[Name]Intent.kt`

- `sealed interface [Name]Intent`.
- `data object` for intents without payload, `data class` with `val`s for intents with payload.
- Named after **what happened** (user action / lifecycle trigger), in present or past tense: `Load`, `Retry`, `Refresh`, `BackClicked`, `QueryChanged(query)`, `ScreenshotClicked(url)`.

```kotlin
// ✅ presentation/NovelDetailIntent.kt
sealed interface NovelDetailIntent {
    data object Load : NovelDetailIntent
    data object Retry : NovelDetailIntent
    data object BackClicked : NovelDetailIntent
    data object ToggleFavorite : NovelDetailIntent
    data class ScreenshotClicked(val url: String) : NovelDetailIntent
}
```

### 4.3 `[Name]Effect.kt`

- `sealed interface [Name]Effect` — one-off side effects only: navigation, snackbar, toast, open URL, share sheet, transient dialog.

```kotlin
// ✅ presentation/NovelDetailEffect.kt
sealed interface NovelDetailEffect {
    data object NavigateBack : NovelDetailEffect
    data class OpenScreenshot(val url: String) : NovelDetailEffect
    data class ShowError(val error: ErrorUIModel) : NovelDetailEffect
}
```

### 4.4 `[Name]ViewModel.kt`

**MUST** have exactly this public surface:

```kotlin
private val _state = MutableStateFlow([Name]State())
val state: StateFlow<[Name]State> = _state.asStateFlow()

private val _effect = Channel<[Name]Effect>(Channel.BUFFERED)
val effect: Flow<[Name]Effect> = _effect.receiveAsFlow()

fun onIntent(intent: [Name]Intent) { when (intent) { ... } }
```

Rules:

- State updates **only** via atomic `_state.update { it.copy(...) }`. **NEVER** `_state.value = _state.value.copy(...)` (races) or `_state.value = [Name]State(...)` (loses fields).
- `onIntent` is the **only** public function. Handlers are `private`. **NEVER** expose `fun refresh()`, `fun onSaveClicked()`, `fun loadNovel(id)`.
- `onIntent` uses an exhaustive `when` (no `else` branch) so new intents fail compilation until handled.
- Plain constructor annotated with `@KoinViewModel` (navigation arguments are `@InjectedParam`), picked up by the feature module's `@ComponentScan`. Constructor takes navigation arguments first (supplied by `parametersOf` from the Route), then **use cases** only (plus presentation mappers with dependencies). Map domain → UI **here**.
- Launch coroutines in `viewModelScope`; never `GlobalScope`, never hold `Context`/`View`/back stack.
- Long-lived streams (DataStore, observed repositories): `combine(_state, observeX()) { … }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), [Name]State())` becomes the single public `state`. See `best-practices` §3.4. **NEVER** collect them forever in `init`, and never expose extra public flows.
- Failures become `error.toErrorUIModel()` (`best-practices` §4). Never put `exception.message` in state.

```kotlin
// ✅ presentation/NovelDetailViewModel.kt
@KoinViewModel
class NovelDetailViewModel(
    @InjectedParam private val novelId: String,        // NovelDetailNavKey.id via parametersOf
    private val getVisualNovelDetails: GetVisualNovelDetailsUseCase,
    private val toggleFavorite: ToggleFavoriteUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(NovelDetailState())
    val state: StateFlow<NovelDetailState> = _state.asStateFlow()

    private val _effect = Channel<NovelDetailEffect>(Channel.BUFFERED)
    val effect: Flow<NovelDetailEffect> = _effect.receiveAsFlow()

    init {
        onIntent(NovelDetailIntent.Load)
    }

    fun onIntent(intent: NovelDetailIntent) {
        when (intent) {
            NovelDetailIntent.Load,
            NovelDetailIntent.Retry -> loadDetails()
            NovelDetailIntent.BackClicked -> sendEffect(NovelDetailEffect.NavigateBack)
            NovelDetailIntent.ToggleFavorite -> onToggleFavorite()
            is NovelDetailIntent.ScreenshotClicked ->
                sendEffect(NovelDetailEffect.OpenScreenshot(intent.url))
        }
    }

    private fun loadDetails() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getVisualNovelDetails(novelId)
                .onSuccess { novel ->
                    _state.update { it.copy(isLoading = false, novel = novel.toUIModel()) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.toErrorUIModel()) }
                }
        }
    }

    private fun onToggleFavorite() {
        _state.update { it.copy(isFavorite = !it.isFavorite) }        // optimistic
        viewModelScope.launch {
            toggleFavorite(novelId).onFailure { error ->
                _state.update { it.copy(isFavorite = !it.isFavorite) } // rollback
                sendEffect(NovelDetailEffect.ShowError(error.toErrorUIModel()))
            }
        }
    }

    private fun sendEffect(effect: NovelDetailEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }
}
```

```kotlin
// ❌ DON'T
class NovelDetailViewModel(...) : ViewModel() {
    val state = MutableStateFlow(NovelDetailState())          // ❌ public mutable flow
    val events = MutableSharedFlow<NovelDetailEffect>()       // ❌ SharedFlow drops events
    var currentNovel: VisualNovel? = null                     // ❌ public mutable field
    fun refresh() { ... }                                     // ❌ public action method
    fun onFavoriteClicked() { ... }                           // ❌ public action method
    private fun load() { state.value = state.value.copy(...) } // ❌ non-atomic update
}
```

---

## 5. Compose UI: Route / Screen / View

### 5.1 `[Name]Route.kt` — navigation destination (stateful)

- The **only** composable that touches the ViewModel.
- Receives the `NavKey`'s arguments as plain parameters (`novelId: String`) from the feature's entry function (`presentation/navigation/[Name]Navigation.kt`).
- Obtains the ViewModel with `koinViewModel { parametersOf(...) }`, collects `state` with `collectAsStateWithLifecycle()`, collects `effect` with `ObserveAsEvents` (`common/ui/util/`, a lifecycle-aware `LaunchedEffect`), and delegates rendering to `[Name]Screen(state, viewModel::onIntent)`.
- Receives navigation callbacks as lambdas (`onNavigateBack: () -> Unit`), or one `[Name]NavActions` when there are more than ~4 — never the back stack or a `Navigator`.
- A `NavKey` carries ids only (never display data), is `@Serializable`, and implements `FullScreenNavKey` (hides the navigation bar) or `OverlayNavKey` (bottom sheet) when it is not a plain page or tab. Add every new key to `NavKeySerializationTest`.

```kotlin
// ✅ presentation/NovelDetailRoute.kt
@Composable
fun NovelDetailRoute(
    novelId: String,
    onNavigateBack: () -> Unit,
    onOpenScreenshot: (String) -> Unit,
    viewModel: NovelDetailViewModel = koinViewModel { parametersOf(novelId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    ObserveAsEvents(viewModel.effect) { effect ->
        when (effect) {
            NovelDetailEffect.NavigateBack -> onNavigateBack()
            is NovelDetailEffect.OpenScreenshot -> onOpenScreenshot(effect.url)
            is NovelDetailEffect.ShowError ->
                snackbarHostState.showSnackbar(effect.error.message.asString(context))
        }
    }

    NovelDetailScreen(
        state = state,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
    )
}
```

### 5.2 `[Name]Screen.kt` — full screen (stateless)

- Signature: `fun [Name]Screen(state: [Name]State, onIntent: ([Name]Intent) -> Unit, modifier: Modifier = Modifier, …hoisted UI-only holders)`.
- **NEVER** reference a ViewModel, `koinViewModel()`, the back stack, use case, or repository.
- Every interaction calls `onIntent(...)`. No business logic, no mapping, no formatting.
- Ships with `@AdaptivePreviews` (light/dark × compact/expanded) built from hand-constructed `[Name]State`s. See `best-practices` §5.1.

```kotlin
// ✅ presentation/NovelDetailScreen.kt
@Composable
fun NovelDetailScreen(
    state: NovelDetailState,
    onIntent: (NovelDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { state.novel?.title?.let { Text(it.asString()) } },
                navigationIcon = {
                    IconButton(onClick = { onIntent(NovelDetailIntent.BackClicked) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = UiText.StringResource(StringResource.cd_navigate_back).asString(),
                        )
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> NovelDetailSkeleton(Modifier.padding(padding))   // private in this file, see best-practices §5.3
            state.error != null -> ErrorView(
                model = state.error,
                onRetry = { onIntent(NovelDetailIntent.Retry) },
                modifier = Modifier.padding(padding),
            )
            state.novel != null -> NovelHeaderView(
                model = state.novel,
                isFavorite = state.isFavorite,
                onFavoriteClick = { onIntent(NovelDetailIntent.ToggleFavorite) },
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@AdaptivePreviews
@Composable
private fun NovelDetailScreenPreview() {
    BakaMitaiTheme {
        NovelDetailScreen(
            state = NovelDetailState(
                novel = VisualNovelUIModel(
                    id = "v17",
                    title = UiText.StringValue("Ever17"),
                    releaseLabel = UiText.StringValue("Aug 2002"),
                    ratingLabel = UiText.StringValue("8.71"),
                    coverUrl = null,
                    blurCover = false,
                ),
            ),
            onIntent = {},
        )
    }
}
```

### 5.3 `[Name]View.kt` — reusable component

- Accepts an immutable `[Name]UIModel` / `[Name]UIState` (or primitives), plain lambdas for events, and `modifier: Modifier = Modifier` as the **first optional** parameter.
- Event lambdas are specific (`onFavoriteClick: () -> Unit`), not the screen's `onIntent` — keeps the component reusable outside one MVI contract.
- Placement: its own subpackage `feature/[name]/presentation/component/[component]/` when used only inside one feature, or `common/ui/component/[component]/` when used by 2+ features (then it must not depend on any feature type). A component's own `UIModel` / `UIState` and `UIModelMapper` go in that subpackage's `model/` and `mapper/`, and sub-components nest under its `component/`. See `structure` §1.1 and §3.
- Stateless; any internal `remember` is purely visual (animation, scroll) — never business data.
- A component that renders loaded data also has a `[Name]SkeletonView(modifier: Modifier = Modifier)` in the same file: the same container, size and shape, with `SkeletonBlock` / `SkeletonTextLine` in place of the data. See `best-practices` §5.3.

```kotlin
// ✅ feature/novel/presentation/component/novelheader/NovelHeaderView.kt
@Composable
fun NovelHeaderView(
    model: VisualNovelUIModel,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.padding(16.dp)) {
        CoverImageView(url = model.coverUrl, blur = model.blurCover)   // common/ui/component
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(model.title.asString(), style = MaterialTheme.typography.titleLarge)
            Text(model.releaseLabel.asString(), style = MaterialTheme.typography.bodyMedium)
            model.ratingLabel?.let { Text(it.asString()) }
        }
        IconToggleButton(checked = isFavorite, onCheckedChange = { onFavoriteClick() }) {
            Icon(
                if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = UiText.StringResource(
                    if (isFavorite) StringResource.cd_remove_favorite else StringResource.cd_add_favorite,
                ).asString(),
            )
        }
    }
}
```

| ❌ Don't | ✅ Do |
|---|---|
| `fun NovelDetailScreen(viewModel: NovelDetailViewModel)` | `fun NovelDetailScreen(state, onIntent)` |
| `val vm = koinViewModel<...>()` inside a Screen or View | Only in the Route |
| `Text(novel.releaseDate.format(...))` in a composable | `Text(model.releaseLabel.asString())` (formatted by the mapper) |
| `fun NovelHeaderView(novel: VisualNovel)` (domain model) | `fun NovelHeaderView(model: VisualNovelUIModel)` |
| `fun NovelCardView(onIntent: (NovelListIntent) -> Unit)` | `fun NovelCardView(onClick: () -> Unit)` |
| `LaunchedEffect(Unit) { viewModel.load() }` in Route | `init { onIntent(Load) }` in the ViewModel |
| `if (state.navigateBack) { onBack(); viewModel.consumed() }` | `Effect.NavigateBack` collected in the Route |
| `NovelScreen.kt` containing Route + Screen + ViewModel | Separate `NovelDetailRoute.kt`, `NovelDetailScreen.kt`, `NovelDetailViewModel.kt` |
| `state.isLoading -> CircularProgressIndicator()` | `state.isLoading -> NovelDetailSkeleton()`: one `SkeletonView` mirroring the content (`best-practices` §5.3) |

---

## 6. New-screen scaffold

File locations follow the `structure` skill. When adding a screen `[Name]`, work inside-out:

1. `domain`: the model, repository interface and `[Verb][Thing]UseCase`.
2. `data`: the DTOs (one per file), `[Thing]DTOMapper`, `[Feature]Service` and `[Thing]RepositoryImpl`.
3. `presentation`: `[Thing]UIModel` / `[Thing]UIState`, `[Thing]UIModelMapper`, then `[Name]State`, `[Name]Intent`, `[Name]Effect`, `[Name]ViewModel`, `[Name]Screen` (with a preview) and `[Name]Route`.
4. `di`: register everything in the feature's `[Name]Module.kt`, and make sure `app/di/AppModule.kt` includes that module.
5. `presentation/navigation/`: add `[Name]NavKey.kt` and `[Name]Navigation.kt` with `[name]Entry(...)`, add `Navigator.navigateTo[Name](...)` there too, then call `[name]Entry(...)` from `app/navigation/AppEntryProvider.kt` with lambdas that use the other features' `navigateTo…` helpers. Add the key to `NavKeySerializationTest`.

## 7. Conventions checklist (run before finishing any change)

- [ ] File name matches its primary declaration; acronyms are `DTO` / `UI`.
- [ ] Use cases are `[Verb][Name]UseCase` with a single `operator fun invoke`.
- [ ] One DTO per file, no nested DTOs; domain models have no suffix and no annotations.
- [ ] UIModel = static display data, UIState = interactive data; all `val`, `ImmutableList`, updated via `.copy`.
- [ ] Loading states are skeletons: a private `[Screen]Skeleton` in the Screen file, and a `[Name]SkeletonView` next to each data component's `[Name]View`.
- [ ] Separate mapper file per direction: `[Name]DTOMapper`, `[Name]DomainMapper`, `[Name]UIModelMapper`.
- [ ] State / Intent / Effect / ViewModel each in its own file; State has defaults for every property.
- [ ] ViewModel: private `_state` + public `StateFlow`, private `Channel` + public `Flow`, single public `onIntent`, `_state.update { it.copy() }` only.
- [ ] Route owns `koinViewModel { parametersOf(…) }`, `collectAsStateWithLifecycle()` and `ObserveAsEvents`; Screen is stateless `(state, onIntent)` with `@AdaptivePreviews`; Views take UI models and specific lambdas.
- [ ] Quality rules in `best-practices` (stability, lazy keys, Detekt, error mapping, tests) are met.
