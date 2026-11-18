---
name: architecture
description: Clean Architecture layers, dependency rules, and MVI data flow for this Android app. Use when creating, moving, or refactoring code.
---

# Architecture

This skill defines **which layer may talk to which** and **how data flows**. Related skills:

- `structure` — the package tree and where every file goes. **Load it for any file placement question.**
- `conventions` — naming, immutability, mappers, MVI contract and Route/Screen/View templates.
- `tech-stack` — approved libraries and which layer may use each.
- `library-management` — version catalog, `build-logic/` and dependency workflows.
- `best-practices` — performance, Detekt, concurrency, error handling and testing standards.

Rules marked **MUST** / **NEVER** are non-negotiable. If existing code violates a rule, fix it as part of your change (or call it out). Never copy a violation into new code.

---

## 1. Clean Architecture (single module)

There is one Gradle module (`:app`). Layers are enforced by **packages** (see `structure`), not modules, so the compiler won't catch violations. These rules have to.

```text
presentation ──▶ domain ◀── data
                   ▲
       di ─────────┘  (wires data implementations to domain interfaces; sees all layers of its feature)
```

### Dependency rule

| Layer | MAY import | MUST NEVER import |
|---|---|---|
| `domain` | Kotlin stdlib, `kotlinx.coroutines` core, kotlinx-datetime, Koin annotations (`org.koin.core.annotation.*` only), `common/domain`, other features' `domain` | `presentation`, `data`, `di`, `android.*`, `androidx.*`, Compose, Ktor, `kotlinx.serialization`, DataStore, Koin beyond `org.koin.core.annotation.*`, `javax.inject` |
| `data` | own feature's `domain`, `common/domain`, `common/data`, Ktor, kotlinx.serialization, DataStore, Koin annotations, Android framework where needed | `presentation`, `common/ui`, Compose |
| `presentation` | own feature's `domain`, other features' `domain` (models + use cases), `common/domain`, `common/ui`, Android, Compose, Lifecycle, Coil | any `data` package, DTOs, Ktor, DataStore, repository implementations |
| `di` | everything in its own feature, `common`, Koin | — |

### Domain purity

`domain` is plain Kotlin. A `domain` file is **wrong** if it contains:

- Android or Compose: `android.*`, `androidx.*`, `@StringRes`, `Parcelable`, `Context`, `Uri`, `Color`, `Dp`, `R.*`
- Networking or serialization: `io.ktor.*`, `kotlinx.serialization.*` (`@Serializable`, `@SerialName`)
- Persistence: `androidx.datastore.*`
- DI: `org.koin.*` except the annotations in `org.koin.core.annotation.*`, and `javax.inject.*`. Domain classes use plain constructors; the only Koin touch is an annotation on the class (`@Factory` on use cases, `@Single` on `DefaultDispatcherProvider`).

Both commands must print nothing:

```bash
grep -rnE "import (android\.|androidx\.|io\.ktor\.|kotlinx\.serialization\.|org\.koin\.(android|compose|core\.(context|module|parameter|scope))|javax\.inject\.)" app/src/main/java/com/zc/bakamitai/feature/*/domain app/src/main/java/com/zc/bakamitai/common/domain
```

```bash
grep -rnE "import com\.zc\.bakamitai\.feature\.[a-z]+\.data\." app/src/main/java/com/zc/bakamitai/feature/*/presentation
```

---

## 2. Call hierarchy & layer boundaries

### The only allowed direction

```text
Composable ──Intent──▶ ViewModel ──▶ UseCase ──▶ Repository (interface, domain)
                                                      │ implemented by
                                                      ▼
                                       RepositoryImpl (data) ──▶ Ktor Service / DataStore source
                                                      │
                                          DTO ──[Name]DTOMapper──▶ Domain model
                                                      │
ViewModel ◀────────── Domain model / Result ◀─────────┘
    │
    └──[Name]UIModelMapper──▶ UIModel / UIState ──reduce──▶ State ──▶ Composable
```

- **MUST**: ViewModels depend only on **use cases**, navigation arguments (`@InjectedParam`, passed by the Route with `parametersOf`) and presentation mappers that have dependencies.
- **NEVER** inject a repository, `RepositoryImpl`, Ktor service, `HttpClient`, `DataStore`, or data source into a ViewModel, even for "simple" reads. Write a use case. Pass-through use cases are intentional: they keep the rule uniform and give business logic an obvious home.
- **NEVER** call a use case, repository or service from a Composable. Composables only emit intents.
- Use cases depend on repository **interfaces** (or other use cases), never on implementations or data sources.
- Repository interfaces live in `domain`, speak only **domain models**, and expose `suspend` functions or `Flow`.
- Repository implementations live in `data`. They are the **only** place that touches DTOs and data sources together, and they wrap every service call in `safeApiCall {}`, so failures reach `domain` only as `DomainError` (`common/domain/model`).
- Errors cross the layers as **`data`** exception → `DomainError` → **`domain`** `Result<T>` → **`presentation`** `ErrorUIModel`. Details are in `best-practices` §4.
- Threading: every `suspend` function is main-safe. Ktor and DataStore already are, so `data` switches dispatchers only for heavy CPU work, using the injected `DispatcherProvider`. Nothing outside `DispatcherProvider` and `ObserveAsEvents` references `Dispatchers.*`.

### Model boundaries

| Layer | Owns | Leaves the layer as |
|---|---|---|
| `data` | `[Name]DTO` | **Never leaves `data`.** Mapped via `[Name]DTOMapper` / `[Name]DomainMapper` |
| `domain` | `[Name]` | Returned from repositories and use cases to the ViewModel |
| `presentation` | `[Name]UIModel`, `[Name]UIState`, `[Name]State` | Rendered by Compose |

- `presentation` maps **Domain → UIModel / UIState inside the ViewModel**, then reduces the result into `State`.
- Composables receive only `State`, UI models, primitives and lambdas. **NEVER** pass a domain model to a Composable when any formatting or UI-only state is involved, which in practice means always map.
- **NEVER** put serialization annotations on domain or UI models, or Compose types on domain models.

### Do / Don't

```kotlin
// ❌ DON'T — repository + network in the ViewModel, domain model exposed to UI
class NovelDetailViewModel(
    private val repository: VisualNovelRepository,
    private val client: HttpClient,
) : ViewModel() {
    val novel = MutableStateFlow<VisualNovel?>(null)
}

// ✅ DO — ViewModel -> UseCase, map to UI models, reduce into State
@KoinViewModel
class NovelDetailViewModel(
    private val novelId: String,
    private val getVisualNovelDetails: GetVisualNovelDetailsUseCase,
) : ViewModel()
```

```kotlin
// ❌ DON'T — DTO in a domain interface
interface VisualNovelRepository { suspend fun getById(id: String): VisualNovelDTO }

// ✅ DO
interface VisualNovelRepository { suspend fun getById(id: String): Result<VisualNovel> }
```

```kotlin
// ✅ data/repository/VisualNovelRepositoryImpl.kt — the only DTO -> Domain hand-off point
@Single
class VisualNovelRepositoryImpl(
    private val service: NovelService,
) : VisualNovelRepository {

    override suspend fun getById(id: String): Result<VisualNovel> =
        safeApiCall { service.fetchById(id).toDomain() }
}
```

---

## 3. MVI (Model–View–Intent)

### The loop

```text
 ┌──────────────┐  onIntent(Intent)   ┌──────────────────┐  invoke()  ┌──────────┐
 │  UI (Route/  │────────────────────▶│    ViewModel     │───────────▶│ UseCase  │
 │   Screen)    │                     │                  │◀───────────│          │
 └──────────────┘                     │ map Domain→UI    │  domain    └──────────┘
        ▲   ▲                         │ reduce via       │  result
        │   │   State (StateFlow)     │ _state.update {} │
        │   └─────────────────────────│                  │
        │       Effect (Channel→Flow) │                  │
        └─────────────────────────────│ _effect.send()   │
                                      └──────────────────┘
```

1. UI dispatches an **Intent** through the single `onIntent(intent)` entry point.
2. ViewModel runs business logic through **use cases**.
3. ViewModel maps domain results to UI models and **reduces** them into a new immutable **State**.
4. One-off events are sent as **Effects** through a `Channel`.
5. UI renders `State`. The Route collects `Effect`s and performs navigation, snackbars and similar actions.

### State
- Exactly **one** `StateFlow<[Name]State>` per screen. No other public flows, `LiveData` or `mutableStateOf` on the ViewModel. Reactive sources are folded in with `combine(_state, …).stateIn(viewModelScope, WhileSubscribed(5_000), …)`.
- Derived values are computed properties on `State` or reduced into it. Composables don't recompute business logic.

### Intent
- Every user interaction **and** lifecycle trigger (initial load, retry, refresh) is an `Intent`, routed through one public `onIntent`.
- Initial load: `init { onIntent([Name]Intent.Load) }`, so it runs once per ViewModel and survives configuration changes. Don't trigger it from `LaunchedEffect(Unit)`, which fires again when the screen is recreated.

### Effect
- One-off events (navigation, snackbar, toast, open URL, transient dialog) go through `Channel<[Name]Effect>(Channel.BUFFERED)`, exposed via `receiveAsFlow()`.
- **NEVER** store one-off events as `State` flags (`shouldNavigateBack`, `showToast`). They fire again on recomposition and rotation.
- **NEVER** use `SharedFlow(replay = 0)` for effects. Events emitted while nothing is collecting are lost.
- Persistent UI, such as an error placeholder or a dialog that must survive rotation, belongs in `State`. The one-off notification about it belongs in `Effect`.

File templates for State, Intent, Effect, ViewModel, Route and Screen are in `conventions` §4–5.

### Navigation
- A feature owns its `NavKey`s and an `EntryProviderScope<NavKey>` extension function (`presentation/navigation/`) that registers its entries and calls its Route.
- Everything that leaves the feature — opening another feature's screen, going back — is a lambda parameter of that function (or a `[Name]NavActions` for many). **NEVER** import another feature's navigation package or take a `Navigator` in a feature.
- Each feature exposes `Navigator.navigateTo[Name](...)` extensions next to its keys, so callers never construct another feature's `NavKey` directly. `app/navigation/NavigatorExtensions.kt` adds `Navigator.navigateBack()`; `AppEntryProvider.kt` passes these helpers as bound references (`navigator::navigateToDetail`), not wrapper lambdas.
- `app/navigation/AppEntryProvider.kt` is the only place that knows every feature: it passes each entry function lambdas built from `Navigator` and the other features' keys.
- Shared plumbing (`Navigator`, `NavigationState`, `FullScreenNavKey`, `OverlayNavKey`, `BottomSheetSceneStrategy`) lives in `common/ui/navigation/`.

---

## 4. Checklist

- [ ] Files are placed per `structure`.
- [ ] `domain` (feature and common) has no Android, Compose, Ktor, serialization, DataStore or DI imports.
- [ ] `presentation` imports nothing from any `data` package.
- [ ] ViewModels depend only on use cases, navigation arguments and presentation mappers.
- [ ] Navigation: keys and entry functions live in the feature; cross-feature navigation goes through lambdas wired in `AppEntryProvider.kt`.
- [ ] Repository interfaces are in `domain`, implementations in `data`, and the implementation is annotated (`@Single`) so `@ComponentScan` binds it to its interface automatically.
- [ ] DTOs never leave `data`. Domain models never reach a Composable without being mapped.
- [ ] Each ViewModel has one `StateFlow<State>`, one `Channel`-backed `Flow<Effect>` and one public `onIntent`. No one-off flags in `State`.
