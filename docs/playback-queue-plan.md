# Playback Queue Plan - Media3 Native Migration

## Overview

Current state

- Playback is split across `MediaSessionService`, `PlaybackManager`, and `ServiceResolver`.
- The service owns too much orchestration.
- The controller path still depends on a callback bridge to start playback.
- Playlist metadata already carries `playback_context_type` and `playback_context_query`, so process-death restore is already partially supported.

Goal

- Move playback to a Media3-native design.
- Expose a browsable catalog for Android Auto and other library-capable clients.
- Remove global callback/state for playback start.
- Keep current playback behavior stable during migration.
- Keep the plan trackable in small, shippable steps.

Non-goals

- No UI redesign.
- No search UI rewrite.
- No pagination redesign beyond what is needed to support browse/playback migration.
- No persistence rewrite unless a later step proves it necessary.

## Current Constraints

- `RadioService` currently extends `MediaSessionService`.
- `ServiceResolver.registerPlaybackStartCallback` is still used to route playback start.
- `PlaybackManager` currently owns queue state, paging state, and context restore.
- `StationListViewModel` still registers the fetch resolver from the UI side.
- Playback context metadata is already present in `MediaItem.mediaMetadata.extras`.

## Migration Strategy

### Phase 1 - Library service foundation
- Replace `MediaSessionService` with `MediaLibraryService`.
- Add a library root and children callbacks.
- Return station catalog items as browsable media entries.
- Keep playback start behavior compatible with the current UI.

Acceptance:
- Browsing works from a Media3 library client.
- App still plays stations normally.
- No regression in the existing single-station play flow.

### Phase 2 - Remove callback bridge
- Remove `ServiceResolver.registerPlaybackStartCallback`.
- Make `RadioPlayerController.startPlayback()` use direct Media3 commands.
- Keep `setMediaItem()` as a compatibility fallback only if needed.
- Verify the service no longer depends on UI-owned global state.

Acceptance:
- No playback-start path depends on global callback registration.
- Station selection starts playback through the controller/session flow directly.

### Phase 3 - Reduce service-side orchestration
- Keep `PlaybackManager` only if it still adds clear value.
- If retained, limit it to playback context and catalog coordination.
- Remove queue ownership from custom code where Media3 already provides the behavior.
- Re-evaluate `onTaskRemoved()` and session callback assumptions.

Acceptance:
- Queue behavior is owned by Media3 as much as possible.
- Custom state is minimal and explicit.

### Phase 4 - Cleanup and hardening
- Delete dead resolver code.
- Update tests to cover the new service and controller path.
- Confirm process-death restore still works from metadata extras.
- Verify Android Auto browse/playback behavior.

Acceptance:
- No unused resolver or dead code remains.
- Tests cover browse, play, and restore flows.

## Trackable Tasks

### Done
- [x] Confirm current playback context metadata is already stored in `MediaItem.extras`.
- [x] Inventory current playback architecture and identify `ServiceResolver` coupling.
- [x] Identify existing queue-plan doc location in `docs/`.
- [x] Convert `RadioService` to `MediaLibraryService`.
- [x] Implement `onGetLibraryRoot()`.
- [x] Implement `onGetChildren()`.
- [x] Remove `ServiceResolver.registerPlaybackStartCallback`.
- [x] Update `RadioPlayerControllerImpl` to use direct session/controller commands.
- [x] Remove `RadioMediaSessionCallback` and `ServiceResolver`.
- [x] Remove `PlaybackManager` and its tests.

### To do
- [ ] Update or remove service-side pagination assumptions.
- [ ] Run targeted `core/radioplayer` and `feature/stationlist` tests.
- [ ] Remove dead code after the new path is stable.

## Files Likely to Change

- `core/radioplayer/src/main/kotlin/io/github/agimaulana/radio/core/radioplayer/RadioService.kt`
- `core/radioplayer/src/main/kotlin/io/github/agimaulana/radio/core/radioplayer/internal/RadioPlayerControllerImpl.kt`
- `core/radioplayer/src/main/kotlin/io/github/agimaulana/radio/core/radioplayer/internal/PlaybackManager.kt`
- `feature/stationlist/src/main/java/io/github/agimaulana/radio/feature/stationlist/StationListViewModel.kt`
- `core/radioplayer/src/test/...`
- `docs/playback-queue-plan.md`

## Risks

- Browse tree shape may need adjustment after the first implementation pass.
- `PlaybackManager` removal could expose hidden dependencies in tests or UI behavior.
- Android Auto playback expectations may differ from current in-app flow.
- `onTaskRemoved()` behavior can regress if it still relies on weak playback-state checks.

## Test Plan

- Unit test browse root creation.
- Unit test children item mapping.
- Unit test playback start path without resolver callback.
- Keep restore tests for `playback_context_type` and `playback_context_query`.
- Verify `StationListViewModel` still starts playback from pinned and non-pinned stations.
- Smoke test playback, previous/next, and process-death restore.

## Notes

- This doc is the queue/playback migration tracker.
- Keep it updated as each phase lands.
- Use it to avoid mixing the service migration with unrelated feature work.
