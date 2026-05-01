# Android Auto Feature — 247FM

## Context
App is phone-only. This adds in-car support via Android Car App Library. Three screens match mockups in `docs/plan/screens/` and `docs/plan/search/`. Existing `RadioService` (Media3 `MediaSessionService`), domain use cases, and `RadioPlayerController` are reused as-is.

**Constraint summary:** 76×76dp min touch targets · no text input while driving · max 6 list items visible · voice-only search while moving · parked detection → keyboard unlock.

---

## Architecture

**Approach:** Car App Library (`CarAppService`) for custom UI templates. `RadioService` stays unchanged — add `MediaBrowserService` intent filter so Auto connects for media session.

**New module:** `feature:auto`
- Convention: `boilerplate.android.library` + `boilerplate.android.hilt` (no Compose — Car App Library uses template system)
- Deps: `:domain:api`, `:core:radioplayer`, `androidx.car.app:car-app-android:1.4.0`

**Screen → Template mapping:**

| Screen | Car App Template |
|---|---|
| Browse / Home | `ListTemplate` (pinned section + all stations) |
| Now Playing | `PlaybackTemplate` (80dp play/pause, stop + pin) |
| Search — driving | `SearchTemplate` (voice mode, text auto-blocked by platform) |
| Search — parked gate | `MessageTemplate` ("Type to search" / "Use voice") |
| Search — empty keyboard | `SearchTemplate` (recent searches + genre suggestions) |
| Search — typing live | `SearchTemplate` with `SearchCallback.onSearchTextChanged` |
| Search — safety lock | `SearchTemplate` (platform auto-disables input on drive resume) |

---

## Files

### Modify (4 existing files)
- `gradle/libs.versions.toml` — add car-app version + library alias
- `settings.gradle.kts` — include `:feature:auto`
- `app/build.gradle.kts` — add `implementation(project(":feature:auto"))`
- `app/src/main/AndroidManifest.xml` — add `CarAppService` entry + `MediaBrowserService` intent filter on `RadioService`

### Create (8 new files)
```
feature/auto/
  build.gradle.kts
  src/main/
    AndroidManifest.xml
    kotlin/io/github/agimaulana/radio/feature/auto/
      AutoAppService.kt          ← CarAppService + @AndroidEntryPoint
      AutoSession.kt             ← Session, creates BrowseScreen as root
      screen/
        BrowseScreen.kt          ← ListTemplate: pinned row + all stations + mini player action
        NowPlayingScreen.kt      ← PlaybackTemplate: artwork, play/pause 80dp, stop + pin
        SearchScreen.kt          ← SearchTemplate + parked gate MessageTemplate
      di/
        AutoModule.kt            ← Hilt: inject use cases into screens
```

---

## Reused symbols
- `GetRadioStationsUseCase.execute(page, searchName, location)` — `domain/api/.../usecase/GetRadioStationsUseCase.kt`
- `GetPinnedStationsUseCase` — `domain/api/.../usecase/GetPinnedStationsUseCase.kt`
- `PinStationUseCase` / `UnpinStationUseCase` — `domain/api/.../usecase/`
- `RadioPlayerController` (play, pause, stop, playbackState flow) — `core/radioplayer/.../RadioPlayerController.kt`
- `RadioPlayerControllerFactory` — `core/radioplayer/.../RadioPlayerControllerFactory.kt`
- `RadioMediaItem` + `PlaybackContext.Type.PINNED / SEARCH` — `core/radioplayer/.../RadioMediaItem.kt`

---

## Checklist

### Phase 1 — Module scaffold
- [x] 1.1 Add `car-app = "1.4.0"` + `androidx-car-app` library alias to `gradle/libs.versions.toml`
- [x] 1.2 Add `:feature:auto` to `settings.gradle.kts`
- [x] 1.3 Create `feature/auto/build.gradle.kts` (library + hilt, no compose)
- [x] 1.4 Create `feature/auto/src/main/AndroidManifest.xml` (CarAppService declaration)
- [x] 1.5 Update `app/build.gradle.kts` — add `:feature:auto` dep

### Phase 2 — Manifest wiring
- [x] 2.1 `AutoAppService` declared in `feature/auto/AndroidManifest.xml` with `CarAppService` + `IOT` category
- [x] 2.2 Add `android.media.browse.MediaBrowserService` intent filter to `RadioService` in `core/radioplayer/AndroidManifest.xml`
- [x] 2.3 Add `<uses-feature android:name="android.hardware.type.automotive" android:required="false"/>` to `app/AndroidManifest.xml`

### Phase 3 — Entry points
- [x] 3.1 `AutoAppService.kt` — `@AndroidEntryPoint CarAppService`, injects use cases + factory, creates `AutoSession`
- [x] 3.2 `AutoSession.kt` — `onCreateScreen()` returns `BrowseScreen`, `onNewIntent()` pushes `SearchScreen` for voice commands; manages `RadioPlayerController` lifecycle via `StateFlow`

### Phase 4 — Browse screen
- [x] 4.1 `BrowseScreen.kt`
  - `ListTemplate` with PINNED + ALL STATIONS sections (max 6 each)
  - Live `isPlaying` + `currentMediaId` from `PlaybackEvent` flow
  - "▶ Now playing" row decoration for current station
  - ActionStrip: play/pause toggle when active + Search navigation
  - Tap row → `startPlayback()` + push `NowPlayingScreen`

### Phase 5 — Now Playing screen
- [x] 5.1 `NowPlayingScreen.kt`
  - `PaneTemplate` (minCarApiLevel 2 compatible, vs `PlaybackTemplate` which needs level 6)
  - Play/Pause + Stop in `Pane` actions; Pin/Unpin in `ActionStrip`
  - Observes `PlaybackEvent.PlayingChanged` for live button state

### Phase 6 — Search screen (5 states)
- [x] 6.1 **State 1 — driving/voice:** `MessageTemplate` with voice hint chips using `ParkedOnlyOnClickListener`
- [x] 6.2 **State 2 — parked gate:** `MessageTemplate` "Vehicle Parked" with "Type to search" + "Use voice" actions
- [x] 6.3 **State 3 — empty keyboard:** `SearchTemplate` with genre suggestion rows
- [x] 6.4 **State 4 — typing live:** `SearchCallback.onSearchTextChanged` → live results with pinned badge
- [x] 6.5 **State 5 — safety lock:** `ConstraintManager.CONFIG_REQUIRES_DRIVING_OPTIMIZED_ONLY` detected in `onGetTemplate()` → `SearchTemplate` with disabled keyboard + "⚠ Driving — use voice" strip

### Phase 7 — DI
- [x] 7.1 No separate `AutoModule` needed — existing `UseCaseModule` in `:domain:impl` covers all bindings; Hilt graph resolved at `:app` level

### Phase 8 — Verification
- [ ] 8.1 `./gradlew :feature:auto:assembleDebug` ← **blocked: disk full — free ~/.gradle/caches first**
- [ ] 8.2 `./gradlew :app:assembleDebug`
- [ ] 8.3 Test on Desktop Head Unit (DHU): `$ANDROID_HOME/extras/google/auto/desktop-head-unit`
- [ ] 8.4 Verify MediaBrowserService connection (DHU shows 247FM in media list)
- [ ] 8.5 Test BrowseScreen: pinned section + all stations + tap-to-play
- [ ] 8.6 Test NowPlayingScreen: full player + pin/unpin persists
- [ ] 8.7 Test SearchScreen: driving→parked gate→type→results→drive again cycle
- [ ] 8.8 `./gradlew :feature:auto:test`

---

## Gotchas
- `CarAppService` Hilt scope → use `@InstallIn(ServiceComponent::class)` in `AutoModule`
- `PlaybackTemplate` max 4 custom actions — play/pause + stop + pin = 3, OK
- `ListTemplate` max 6 visible rows — add "Load more" `Action` for pagination
- `SearchTemplate` driving lock is platform-enforced — no custom code needed for State 5
- `TabTemplate` needs Car API level 6 — use `ListTemplate` + nav `Action`s for broader compatibility (minCarApiLevel 2)
- Media3 `MediaSessionService` auto-exposes `MediaBrowserServiceCompat` shim — only manifest intent filter needed
