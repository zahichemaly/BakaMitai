# BakaMitai

View the latest daily and weekly anime releases from [SubsPlease](https://subsplease.org/). Bookmark a show to be notified of upcoming releases via push notifications.

> **Status:** the app is being rewritten from scratch. `develop` holds a fresh Jetpack Compose scaffold; the previous version is on `master` and in the git history.

## Tech stack

- Kotlin, Jetpack Compose and Material 3 (with Material 3 Adaptive for larger screens)
- Clean Architecture with MVI, single `:app` module organized by feature
- Planned: Navigation 3, Koin, Ktor Client, kotlinx.serialization, DataStore, Coil 3, kotlinx-datetime
- Quality: Detekt with Compose rules, JUnit 4, kotlinx-coroutines-test, Turbine

The full list of approved libraries, and which layer may use each, is in [`.claude/skills/tech-stack`](.claude/skills/tech-stack/SKILL.md).

## Requirements

- JDK 17 or newer
- Android SDK with the API 37 platform
- A device or emulator running Android 8.1 (API 27) or newer

## Getting started

```bash
git clone https://github.com/zahichemaly/BakaMitai.git
```

```bash
cd BakaMitai
```

```bash
./gradlew installDebug
```

Or open the project in Android Studio and run the `app` configuration.

## Tests

```bash
./gradlew testDebugUnitTest
```

```bash
./gradlew connectedDebugAndroidTest
```

Instrumented tests need a running emulator or connected device.

## Project structure

```text
app/src/main/java/com/zc/bakamitai/
├── app/          # Activity, Application, root navigation, DI assembly
├── common/       # Code shared by two or more features (data, domain, di, ui)
└── feature/
    └── [name]/   # One package per feature: data, domain, di, presentation
```

This is the target layout for the rewrite. Placement rules are in [`.claude/skills/structure`](.claude/skills/structure/SKILL.md).

## Contributing

Architecture, naming, library and testing rules live in [`.claude/skills/`](.claude/skills/). [`CLAUDE.md`](CLAUDE.md) (also available as `AGENTS.md`) summarizes them for coding agents.

## License

Licensed under the [Apache License 2.0](LICENSE).
