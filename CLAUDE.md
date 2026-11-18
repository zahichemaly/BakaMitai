# BakaMitai

Android app, being rewritten from scratch. The repo holds a fresh Jetpack Compose scaffold; the previous codebase is in git history and on the `master`, `clean-architecture` and `compose-migration` branches.

## Project

- Single Gradle module `:app`, Kotlin only, Jetpack Compose + Material 3.
- Root package `com.zc.bakamitai` (`app/src/main/java/com/zc/bakamitai/`).
- `minSdk` 27, `compileSdk` / `targetSdk` 37. AGP 9.5.1, Gradle 9.6.0, Kotlin 2.2.10, Compose BOM 2026.02.01.
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
- `./gradlew detekt` is required by the skills but not set up yet (see `library-management`).

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

- `MainActivity.kt` and `ui/theme/` are still in their template locations. Move them to `app/` and `common/ui/theme/` when next touched (`structure` §7).
- `build-logic/`, Detekt and the approved libraries (Koin, Ktor, Navigation 3, …) are not wired in yet.
