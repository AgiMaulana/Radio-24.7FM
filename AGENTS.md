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

<!-- gitnexus:start -->
# GitNexus — Code Intelligence

This project is indexed by GitNexus as **247FM** (1748 symbols, 4049 relationships, 140 execution flows). Use the GitNexus MCP tools to understand code, assess impact, and navigate safely.

> If any GitNexus tool warns the index is stale, run `npx gitnexus analyze` in terminal first.

## Always Do

- **MUST run impact analysis before editing any symbol.** Before modifying a function, class, or method, run `gitnexus_impact({target: "symbolName", direction: "upstream"})` and report the blast radius (direct callers, affected processes, risk level) to the user.
- **MUST run `gitnexus_detect_changes()` before committing** to verify your changes only affect expected symbols and execution flows.
- **MUST warn the user** if impact analysis returns HIGH or CRITICAL risk before proceeding with edits.
- When exploring unfamiliar code, use `gitnexus_query({query: "concept"})` to find execution flows instead of grepping. It returns process-grouped results ranked by relevance.
- When you need full context on a specific symbol — callers, callees, which execution flows it participates in — use `gitnexus_context({name: "symbolName"})`.

## Never Do

- NEVER edit a function, class, or method without first running `gitnexus_impact` on it.
- NEVER ignore HIGH or CRITICAL risk warnings from impact analysis.
- NEVER rename symbols with find-and-replace — use `gitnexus_rename` which understands the call graph.
- NEVER commit changes without running `gitnexus_detect_changes()` to check affected scope.

## Resources

| Resource | Use for |
|----------|---------|
| `gitnexus://repo/247FM/context` | Codebase overview, check index freshness |
| `gitnexus://repo/247FM/clusters` | All functional areas |
| `gitnexus://repo/247FM/processes` | All execution flows |
| `gitnexus://repo/247FM/process/{name}` | Step-by-step execution trace |

## CLI

| Task | Read this skill file |
|------|---------------------|
| Understand architecture / "How does X work?" | `.claude/skills/gitnexus/gitnexus-exploring/SKILL.md` |
| Blast radius / "What breaks if I change X?" | `.claude/skills/gitnexus/gitnexus-impact-analysis/SKILL.md` |
| Trace bugs / "Why is X failing?" | `.claude/skills/gitnexus/gitnexus-debugging/SKILL.md` |
| Rename / extract / split / refactor | `.claude/skills/gitnexus/gitnexus-refactoring/SKILL.md` |
| Tools, resources, schema reference | `.claude/skills/gitnexus/gitnexus-guide/SKILL.md` |
| Index, status, clean, wiki CLI commands | `.claude/skills/gitnexus/gitnexus-cli/SKILL.md` |

<!-- gitnexus:end -->
