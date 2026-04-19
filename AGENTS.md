# Agent Instructions

## Build Commands

Build — always append: --console=plain 2>&1 | grep -E '(e:|w:|error:|warning:|FAILED|BUILD|\.kt:)'
Unit tests — always append: --console=plain 2>&1 | grep -E -A 8 -B 2 '(FAILED$|ComparisonFailure|AssertionError|at com\..*Test\.kt:[0-9]+|BUILD)'
Detekt — always append: --console=plain 2>&1 | grep -E '(detekt|e:|w:|error:|warning:|FAILED|BUILD|\.kt:)'

```bash
# Development
./gradlew assembleDevDebug         # Build dev debug APK
./gradlew testDevDebugUnitTest     # Run unit tests

# Staging
./gradlew assembleStagingDebug  # Build staging debug APK
./gradlew testStagingDebugUnitTest # Run unit tests

# Production
./gradlew compileProdReleaseSources  # Compile prod release
./gradlew :app:assembleProdRelease   # Build prod release APK with signing
```

## Build Variants

- **Build Types**: `debug`, `release`
- **Product Flavors**: `dev`, `staging`, `prod` (combined with build types)
- **APK Splits**: arm64-v8a, armeabi-v7a, x86, x86_64, universal

## Release Signing

- **CI**: Reads from `keystore.properties` (created in workflow)
- **Local**: Reads from `local.properties`
- Required keys: `keystoreFile`, `storeFilePassword`, `keyAlias`, `keyPassword`

## Mono-Repo Structure

| Directory | Purpose |
|-----------|---------|
| `app/` | Android application |
| `feature/` | Feature modules (stationlist) |
| `core/` | Shared libraries (design, network, radioplayer) |
| `domain/` | Domain layer (api, impl) |
| `infrastructure/` | Infrastructure implementations |
| `build-logic/` | Custom Gradle convention plugins |

## Tech Stack

- Kotlin 2.2.10, AGP 8.13.0, SDK 36
- Jetpack Compose, Hilt DI, Media3
- Detekt for linting

## Key Config Files

- `gradle/libs.versions.toml` - Version catalog
- `build-logic/convention/build.gradle.kts` - Custom Gradle plugins

## Tests

- Unit tests: `:module:testDevDebugUnitTest`
- Instrumented tests: `:module:connectedDevDebugAndroidTest`
- Test source in `src/test/` and `src/androidTest/`