---
name: library-management
description: Dependency declaration (Gradle Version Catalog), artifact coordinates, build-logic convention plugins and custom tasks for this Android app. Use when adding, updating, or removing a dependency, or changing any Gradle file.
---

# Library Management

This skill defines **how dependencies and build logic are declared**. *Which* libraries are approved, and how code uses them, is in `tech-stack`. Rules marked **MUST** / **NEVER** are non-negotiable.

---

## 1. Rules at a glance

| ✅ Do | ❌ Don't |
|---|---|
| Declare every library, BOM and plugin in `gradle/libs.versions.toml` | Coordinates or versions in any `*.gradle.kts`: `implementation("io.ktor:ktor-client-core:3.1.0")` |
| Consume through type-safe accessors: `libs.ktor.client.core`, `libs.plugins.kotlin.serialization` | `id("org.jetbrains.kotlin.plugin.serialization") version "…"` |
| Pinned versions shared per release train with `version.ref` | Dynamic versions (`+`, ranges, `latest.release`) |
| Use a BOM (`platform(...)`) where the library publishes one | Repeating a version for every BOM-managed artifact |
| Put shared build config and **every custom Gradle task** in `build-logic/` convention plugins | `buildSrc/`; `tasks.register`, `doLast`, `afterEvaluate`, `subprojects {}` in module or root scripts |
| Kotlin DSL (`*.gradle.kts`) | Groovy `*.gradle` files |
| Add a library only after it is approved in `tech-stack` §1 | Adding an unapproved library "just for this feature" |

---

## 2. Artifact map

Each approved library from `tech-stack` §1, with its catalog accessor and coordinates. Artifacts marked *BOM* take their version from the BOM and declare none.

| Library | Catalog accessor(s) | Coordinates |
|---|---|---|
| Kotlin plugins | `libs.plugins.kotlin.compose`, `libs.plugins.kotlin.serialization`, `libs.plugins.kotlin.android` (root `apply false` only, see §4) | `org.jetbrains.kotlin.plugin.compose`, `org.jetbrains.kotlin.plugin.serialization`, `org.jetbrains.kotlin.android` |
| Coroutines | `libs.kotlinx.coroutines.android` | `org.jetbrains.kotlinx:kotlinx-coroutines-android` |
| kotlinx-datetime | `libs.kotlinx.datetime` | `org.jetbrains.kotlinx:kotlinx-datetime` |
| kotlinx-collections-immutable | `libs.kotlinx.collections.immutable` | `org.jetbrains.kotlinx:kotlinx-collections-immutable` |
| Compose + Material 3 | `libs.androidx.compose.bom`, `libs.androidx.compose.material3` *BOM*, `libs.androidx.activity.compose` | `androidx.compose:compose-bom`, `androidx.compose.material3:material3`, `androidx.activity:activity-compose` |
| Material Icons Extended | `libs.androidx.compose.material.icons.extended` *BOM* | `androidx.compose.material:material-icons-extended` |
| Material 3 Adaptive | `libs.androidx.compose.material3.adaptive.navigation.suite`, `libs.androidx.compose.material3.adaptive.*` | `androidx.compose.material3:material3-adaptive-navigation-suite`, `androidx.compose.material3.adaptive:adaptive`, `adaptive-layout`, `adaptive-navigation3` |
| Lifecycle | `libs.androidx.lifecycle.runtime.compose`, `libs.androidx.lifecycle.viewmodel.compose`, `libs.androidx.lifecycle.viewmodel.navigation3` | `androidx.lifecycle:lifecycle-runtime-compose`, `lifecycle-viewmodel-compose`, `lifecycle-viewmodel-navigation3` |
| Compose RichText | `libs.richtext.ui.material3`, `libs.richtext.commonmark` | `com.halilibo.compose-richtext:richtext-ui-material3`, `richtext-commonmark` |
| Coil 3 | `libs.coil.compose`, `libs.coil.network.ktor3` | `io.coil-kt.coil3:coil-compose`, `coil-network-ktor3` |
| Navigation 3 | `libs.androidx.navigation3.runtime`, `libs.androidx.navigation3.ui` | `androidx.navigation3:navigation3-runtime`, `navigation3-ui` |
| Ktor Client | `libs.bundles.ktor.client`, `libs.ktor.client.logging` | `io.ktor:ktor-client-core`, `ktor-client-okhttp` (engine only), `ktor-client-content-negotiation`, `ktor-serialization-kotlinx-json`, `ktor-client-logging` |
| kotlinx.serialization | `libs.kotlinx.serialization.json` | `org.jetbrains.kotlinx:kotlinx-serialization-json` |
| HTML parsing (Ksoup) | `libs.ksoup` | `com.fleeksoft.ksoup:ksoup` (KMP; resolves to `ksoup-android`) |
| DataStore | `libs.androidx.datastore.preferences` (or `libs.androidx.datastore`) | `androidx.datastore:datastore-preferences` / `androidx.datastore:datastore` |
| Koin | `libs.koin.bom`, `libs.koin.android` *BOM*, `libs.koin.androidx.compose` *BOM*, `libs.koin.annotations` *BOM*, `libs.plugins.koin.compiler` | `io.insert-koin:koin-bom`, `koin-android`, `koin-androidx-compose`, `koin-annotations`, plugin `io.insert-koin.compiler.plugin` |
| Detekt (2.x) | `libs.plugins.detekt`, `libs.detekt.formatting`, `libs.compose.rules.detekt` | plugin `dev.detekt`, `dev.detekt:detekt-rules-ktlint-wrapper`, `io.nlopez.compose.rules:detekt` (Compose rules 0.6.x require Detekt 2.x) |
| Testing | `libs.bundles.test.unit` | `junit:junit`, `org.jetbrains.kotlin:kotlin-test-junit` (provides `kotlin.test.Test`; AGP's built-in Kotlin doesn't pick the JUnit variant of plain `kotlin-test`), `org.jetbrains.kotlinx:kotlinx-coroutines-test`, `app.cash.turbine:turbine`, `io.ktor:ktor-client-mock`, `io.insert-koin:koin-test` |
| build-logic compile-only | `libs.android.gradle.plugin`, `libs.kotlin.gradle.plugin`, `libs.compose.gradle.plugin`, `libs.detekt.gradle.plugin` | `com.android.tools.build:gradle`, `org.jetbrains.kotlin:kotlin-gradle-plugin`, `org.jetbrains.kotlin:compose-compiler-gradle-plugin`, `dev.detekt:detekt-gradle-plugin` |

---

## 3. Gradle Version Catalog (`gradle/libs.versions.toml`)

- Versions live in `[versions]` and are referenced with `version.ref`. Use **one version key per release train** (`ktor`, `koin-bom`, `coil`, `navigation3`), shared by all its artifacts.
- Keys are lowercase kebab-case, grouped by prefix: `ktor-client-core` becomes `libs.ktor.client.core`.
- BOM-managed libraries declare **no** version and are added with `platform(...)`: `implementation(platform(libs.koin.bom))`.
- Use `[bundles]` for artifacts that are always added together (Ktor client, unit-test libraries).
- Our own convention plugins are listed under `[plugins]` **without a version**.

```toml
# gradle/libs.versions.toml (excerpt)
[versions]
agp = "…"
kotlin = "…"
ktor = "…"
koin-bom = "…"
coroutines = "…"

[libraries]
ktor-client-core = { group = "io.ktor", name = "ktor-client-core", version.ref = "ktor" }
ktor-client-okhttp = { group = "io.ktor", name = "ktor-client-okhttp", version.ref = "ktor" }
ktor-client-content-negotiation = { group = "io.ktor", name = "ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { group = "io.ktor", name = "ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-mock = { group = "io.ktor", name = "ktor-client-mock", version.ref = "ktor" }
koin-bom = { group = "io.insert-koin", name = "koin-bom", version.ref = "koin-bom" }
koin-android = { group = "io.insert-koin", name = "koin-android" }                  # version from BOM
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
android-gradle-plugin = { group = "com.android.tools.build", name = "gradle", version.ref = "agp" }
kotlin-gradle-plugin = { group = "org.jetbrains.kotlin", name = "kotlin-gradle-plugin", version.ref = "kotlin" }

[bundles]
ktor-client = ["ktor-client-core", "ktor-client-okhttp", "ktor-client-content-negotiation", "ktor-serialization-kotlinx-json"]

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
bakamitai-android-application = { id = "bakamitai.android.application" }   # build-logic, no version
bakamitai-detekt = { id = "bakamitai.detekt" }
bakamitai-kotlin-only = { id = "bakamitai.kotlin.only" }
```

```kotlin
// ✅ app/build.gradle.kts — declarative only
plugins {
    alias(libs.plugins.bakamitai.android.application)
    alias(libs.plugins.bakamitai.detekt)
    alias(libs.plugins.bakamitai.kotlin.only)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.koin.compiler)          // Koin Annotations, no KSP
}

dependencies {
    implementation(libs.bundles.ktor.client)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.annotations)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

```kotlin
// ❌
dependencies { implementation("io.ktor:ktor-client-core:3.1.0") }
tasks.register("printVersion") { doLast { println(version) } }
```

---

## 4. `build-logic/`: convention plugins & custom tasks

- `build-logic/` is an **included build** that holds all shared build configuration (convention plugins) and **every custom Gradle task**. `buildSrc/` is not used.
- Module scripts (`app/build.gradle.kts`) and the root `build.gradle.kts` stay **declarative**: they apply plugins, set the namespace/application id, and declare dependencies, nothing else.
- Each custom task is an `abstract class` extending `DefaultTask`, with annotated inputs/outputs and a `@TaskAction`, so it is cacheable and configuration-cache compatible. A convention plugin registers it.
- Convention plugins read the shared catalog through `VersionCatalogsExtension`, never with hardcoded coordinates.
- Plugin ids use the `bakamitai.` prefix and are registered in `build-logic/convention/build.gradle.kts`.

```text
build-logic/
├── settings.gradle.kts                  # reuses ../gradle/libs.versions.toml as `libs`
└── convention/
    ├── build.gradle.kts                 # `kotlin-dsl`, compileOnly gradle plugins, gradlePlugin { plugins { … } }
    └── src/main/kotlin/
        ├── AndroidApplicationConventionPlugin.kt   # bakamitai.android.application: SDKs, JVM target, Compose, compose compiler reports
        ├── DetektConventionPlugin.kt               # bakamitai.detekt: detekt config + detektPlugins
        ├── KotlinOnlyConventionPlugin.kt           # bakamitai.kotlin.only: registers verifyNoJavaSources
        ├── extension/                               # Project.libs helper, shared configure functions
        └── task/
            └── VerifyNoJavaSourcesTask.kt
```

```kotlin
// settings.gradle.kts (root)
pluginManagement {
    includeBuild("build-logic")
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
```

```kotlin
// build-logic/settings.gradle.kts
dependencyResolutionManagement {
    repositories { google(); mavenCentral() }
    versionCatalogs {
        create("libs") { from(files("../gradle/libs.versions.toml")) }
    }
}
rootProject.name = "build-logic"
include(":convention")
```

```kotlin
// build-logic/convention/build.gradle.kts
plugins { `kotlin-dsl` }

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
    compileOnly(libs.detekt.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.bakamitai.android.application.get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("detekt") {
            id = libs.plugins.bakamitai.detekt.get().pluginId
            implementationClass = "DetektConventionPlugin"
        }
        register("kotlinOnly") {
            id = libs.plugins.bakamitai.kotlin.only.get().pluginId
            implementationClass = "KotlinOnlyConventionPlugin"
        }
    }
}
```

```kotlin
// build-logic/convention/src/main/kotlin/extension/ProjectExtensions.kt
val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")
```

```kotlin
// build-logic/convention/src/main/kotlin/DetektConventionPlugin.kt
class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.findPlugin("detekt").get().get().pluginId)
        extensions.configure<DetektExtension> {
            buildUponDefaultConfig = true
            config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        }
        dependencies {
            add("detektPlugins", libs.findLibrary("detekt-formatting").get())
            add("detektPlugins", libs.findLibrary("compose-rules-detekt").get())
        }
    }
}
```

```kotlin
// build-logic/convention/src/main/kotlin/AndroidApplicationConventionPlugin.kt (excerpt)
extensions.configure<ComposeCompilerGradlePluginExtension> {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    metricsDestination = layout.buildDirectory.dir("compose_compiler")
}
```

```kotlin
// build-logic/convention/src/main/kotlin/task/VerifyNoJavaSourcesTask.kt
@CacheableTask
abstract class VerifyNoJavaSourcesTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    @get:OutputFile
    abstract val report: RegularFileProperty

    @TaskAction
    fun verify() {
        val javaFiles = sources.asFileTree.matching { include("**/*.java") }.files
        check(javaFiles.isEmpty()) {
            "Java sources are not allowed:\n" + javaFiles.joinToString("\n")
        }
        report.get().asFile.writeText("OK")
    }
}
```

```kotlin
// build-logic/convention/src/main/kotlin/KotlinOnlyConventionPlugin.kt
class KotlinOnlyConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        val verify = tasks.register<VerifyNoJavaSourcesTask>("verifyNoJavaSources") {
            group = "verification"
            sources.from(layout.projectDirectory.dir("src"))
            report.set(layout.buildDirectory.file("reports/verifyNoJavaSources.txt"))
        }
        tasks.named("check") { dependsOn(verify) }
    }
}
```

> **Current state:** `build-logic/` is included from `settings.gradle.kts` and has all three convention plugins: `bakamitai.android.application` (SDKs 27/37, Java 11, Compose, BuildConfig, release optimization, Compose compiler reports in `app/build/compose_compiler/`), `bakamitai.detekt` and `bakamitai.kotlin.only` (`task/VerifyNoJavaSourcesTask.kt`, wired into `check`).
>
> **Kotlin version:** AGP 9 has built-in Kotlin and brings KGP 2.2.10, but the Koin compiler plugin needs Kotlin 2.3.20–2.4.20. The root `build.gradle.kts` declares `alias(libs.plugins.kotlin.android) apply false` (never applied) so the catalog's `kotlin` version replaces it on the build classpath. When bumping `kotlin`, stay within the Koin compiler plugin's verified Kotlin versions.
>
> **Compose reports:** every variant writes to the same `app/build/compose_compiler/*.txt`, so read them right after `assembleRelease`, before another variant (e.g. unit tests) compiles.
>
> **Detekt specifics** (Detekt 2.x, `dev.detekt.gradle.extensions.DetektExtension`):
> - Because `build-logic` only has the Detekt Gradle plugin as `compileOnly`, the root `build.gradle.kts` declares `alias(libs.plugins.detekt) apply false` so the plugin is on the build classpath when the convention plugin applies it.
> - The convention plugin sets `source` to the whole `src/` directory, so every source set (`main`, `test`, `androidTest`, …) is analyzed.
> - Rule overrides live in `config/detekt/detekt.yml`, layered on Detekt's defaults (`buildUponDefaultConfig`) and the defaults shipped by the ktlint wrapper and Compose rules. Config validation is on, so unknown rule or property names fail the build.
> - Run `./gradlew detekt`. For ktlint formatting findings, `./gradlew detekt --auto-correct` rewrites the files; review the diff and re-run `./gradlew detekt`.

---

## 5. Workflows

### Adding a library
1. Confirm it is approved in `tech-stack` §1. If it isn't, update `tech-stack` (approved table, layer table, banned table if it replaces something) **first**.
2. Add a `[versions]` entry, or reuse the release train's existing key. Skip this for BOM-managed artifacts.
3. Add the `[libraries]` / `[plugins]` entry, plus a `[bundles]` entry if it always ships with others.
4. Reference it via `libs.*` in the module's `dependencies {}`, or in a convention plugin if several modules or the build itself need it.
5. Add its row to the artifact map (§2).
6. Run `./gradlew assembleDebug testDebugUnitTest detekt`.

### Updating a version
1. Change only the `[versions]` value. Every artifact on that release train moves together.
2. Read the release notes for breaking changes and migrate call sites in the same change.
3. Run `./gradlew assembleDebug testDebugUnitTest detekt`.

### Removing a library
1. Remove its usages and the `libs.*` references.
2. Delete its `[libraries]` / `[bundles]` / `[plugins]` entries and any `[versions]` key nothing else uses.
3. Remove its row from the artifact map (§2) and from `tech-stack` §1.

---

## 6. Checks

Each must print nothing:

```bash
grep -rnE "\"[a-zA-Z0-9_.-]+:[a-zA-Z0-9_.-]+:[^\"]+\"" --include="*.gradle.kts" .
```

```bash
grep -rnE "tasks\.(register|create)|afterEvaluate|doLast|doFirst" --include="*.gradle.kts" . | grep -v "^./build-logic/"
```

```bash
grep -nE "= *\"[^\"]*(\+|latest\.)" gradle/libs.versions.toml
```

```bash
ls -d buildSrc 2>/dev/null; find . -name "*.gradle" -type f -not -path "*/.gradle/*"
```

## 7. Checklist

- [ ] Every dependency and plugin comes from `gradle/libs.versions.toml` via `libs.*`; no coordinates or versions in `*.gradle.kts`.
- [ ] Versions are pinned, shared per release train, and omitted for BOM-managed artifacts.
- [ ] Custom Gradle tasks and shared build config live in `build-logic/` convention plugins; module and root scripts stay declarative.
- [ ] New or removed libraries are reflected in `tech-stack` §1 and in this skill's artifact map.
