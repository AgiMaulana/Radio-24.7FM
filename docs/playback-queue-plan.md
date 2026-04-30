# Playback Queue Plan — Media3 Pagination & Playback Context

## Overview

Problem

The current player pipeline sets a single MediaItem when a station is played. Stations come from a dynamic, paginated, and searchable source and can include pinned stations. This prevents Media3 from exposing previous/next controls and breaks seamless navigation across pages.

Goal

- Provide previous/next controls in player & notification
- Support forward pagination (append) while playing
- Keep player layer dumb; orchestration in PlaybackManager (service-side)
- Handle pinned stations and search contexts cleanly
- Persist minimal context so playback survives process death

## High-level approach

1. Introduce PlaybackContext data model (DEFAULT, SEARCH, PINNED) and PlaybackQueue representation.
2. Extend RadioPlayerController API to support playlist operations: setMediaItems(items, startIndex), addMediaItems(items), addMediaItems(index, items), mediaItemCount, currentIndex, and helpers for metadata.
3. Implement PlaybackManager inside RadioService (MediaSessionService) to own queue state, pagination state, dedup set, and drive ExoPlayer. PlaybackManager listens to player transitions and triggers fetchNextPage when reaching the end.
4. ViewModel remains thin: send "start playback" intent (with context and items/startIndex) to service via controller/MediaController commands. Service performs on-demand pagination when player reaches end using repository/use-cases.
5. Persist minimal context in MediaItem.mediaMetadata.extras (context_type, query, page/index). Optionally extend persistence using DataStore/Room later.
6. Implement pinned Strategy A (separate playback context) initially; merging pins into the main list is Phase 2.

## Files / Components to modify

- core/radioplayer/
  - RadioPlayerController (interface) — add playlist methods
  - internal/RadioPlayerControllerImpl — proxy new playlist commands to MediaController (or use SessionCommand)
  - internal/PlaybackEventFlow — already emits media transitions; ensure suffices
  - RadioService — add PlaybackManager instance, attach player listener, and expose startPlayback API
  - (new) PlaybackManager under core/radioplayer/internal

- feature/stationlist/
  - StationListViewModel — call controller.startPlayback(...) with context + current UI list + startIndex; add search-context banner for restored sessions
  - Update tests referencing setMediaItem

- domain/ & infrastructure/
  - Ensure PlaybackManager has access to a small repository interface (or minimal use-case) to request next pages from API

## Implementation order (iterative)

1. Add playlist methods to RadioPlayerController and implement proxies in RadioPlayerControllerImpl. Keep single-item API working.
2. Create PlaybackManager class; integrate into RadioService. PlaybackManager manages loadedStations, loadedIds, nextPage, isFetching flags, and context.
3. Implement forward pagination and append via player.addMediaItems(newItems).
4. Persist minimal PlaybackContext in MediaItem metadata and implement restoreFromPlayer.
5. Update StationListViewModel to use startPlayback API and add a UI banner to explain restored search context and offer "View results".
6. Add/adjust unit and integration tests.
7. Phase 2: backward pagination (prepend) and merged pinned strategy if needed.

## Todos (session tracked)
- playback-add-controller-playlist-api (done)
- playback-introduce-playbackmanager (done)
- playback-service-pagination (done)
- playback-persist-context (done)
- stationlist-viewmodel-integration (done)
- tests-update (done)

## Progress
- Implemented playlist APIs and ViewModel integration so player receives full in-memory playlist on Play.
- Added PlaybackManager and wired forward pagination fetching via a runtime resolver; PlaybackManager appends new items using addMediaItems.
- Implemented combined pinned-first playlist behavior: when playing from pinned UI, the playlist is pinnedStations first followed by the current main list (duplicates removed). startIndex is computed against the combined list so Prev/Next cross the pinned→main boundary correctly.
- Unit tests added/updated:
  - core/radioplayer/src/test/kotlin/io/github/agimaulana/radio/core/radioplayer/RadioMediaItemTest.kt — verifies playback_context_type/query/page extras are attached by RadioMediaItem.toMediaItem.
  - core/radioplayer/src/test/kotlin/io/github/agimaulana/radio/core/radioplayer/internal/PlaybackManagerTest.kt — verifies fetchNextPage dedupe/appending behavior and restoreFromPlayer page estimation and playback context restoration.
- StationListViewModel changes:
  - Use pinnedStations as playlist source for pinned-only playback (previous behavior).
  - Implemented pinned-first combined playlist to continue into the main list after pinned items (current implementation).
  - Resolver registration remains in init(); plan updated to re-register resolver when currentPosition (user location) changes so fetches include latest location.
- Local test run: core:radioplayer unit tests executed locally after adding test dependencies and minor testability adjustments; tests and commits are on feat/playback-queue-wip.
- Confirmed notification shows prev/next and buttons behave as expected in most scenarios. When playback starts at the first item of the default list, the previous action is not available (expected behavior).

## Next steps
- ~~Re-register ServiceResolver when _uiState.currentPosition (user location) updates so fetcher calls include latest location~~ (done: StationListViewModel.observeLocationChangesAndReregisterResolver)
- ~~Run full CI for feat/playback-queue-wip and address any flaky/platform-specific failures.~~ (done: CI passed)
- ~~Run device smoke tests for notification Prev/Next, pinned→main boundary playback, and search-context restore UI.~~ (done: smoke tests passed)
- ~~Add integration tests (optional): simulate process death and restore to verify restoreFromPlayer behavior across platforms.~~ (done: integration-process-death-tests)
- Consider persisting loaded page ids (DataStore/Room) if playlist restore accuracy is important for the product. (todo: persist-loaded-page-ids)
- Phase 2: implement backward pagination (prepend) and merged pinned strategy (todo: playback-backward-pagination)
- Verify Hilt injection for RadioService/PlaybackManager and add providers if missing. (todo: ensure-hilt-injection)


## UX notes
- Don’t auto-enter search UI after process death. Restore playback and show a banner "Playing from search: 'keyword'" with an option to view results.

## Persistence & Restore
- Store minimal context inside MediaItem.mediaMetadata.extras so MediaSessionService/ExoPlayer playlist is the primary source of truth.
- Estimate nextPage by player.mediaItemCount / PAGE_SIZE as fallback. Optionally persist loaded page numbers for accuracy.

## Risks & Mitigations
- Index desync during prepend: defer to Phase 2
- API break: keep old single-item path working while migrating
- Service needs access to repository/use-cases: inject small repository interface into RadioService (Hilt)

---

This document is committed to the repository so work can continue across interruptions. For incremental work, update the session's todos (tracked via the SQL todos table) and reference this document.
