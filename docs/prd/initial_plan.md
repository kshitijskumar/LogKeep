# LogKeep v1 — Implementation Plan (Vertical Slices)

## Context

LogKeep is a "Chucker, but for app logs" KMP library. QA testers on debug builds passively capture all logs for a session, then share the plain-text log file to developers — no device connection required. The library must be entirely inert in release builds.

---

## Confirmed Requirements

| Decision | Choice |
|---|---|
| Platforms | Android + iOS (v1) |
| UI framework | Compose Multiplatform (shared) |
| Storage | SQLDelight (KMP SQLite) |
| Severity levels | VERBOSE, DEBUG, INFO, WARN, ERROR |
| Exception param | `Throwable` (library serialises stack trace) |
| Android init | ContentProvider (auto) + ProcessLifecycleOwner |
| iOS init | Explicit `LogKeep.init(config)` call |
| Session caps | 1 000 entries/session, 5 sessions total (configurable) |
| Release no-op | `isEnabled` in config → false makes all calls no-op |
| Debug entry point | Auto-injected draggable bubble (vertical drag, left-edge default) |
| Viewer scope | Session list → session detail (entries) + share per session |
| Export format | `timestamp \| LEVEL \| tag \| message`, plain text |
| Config surface | `isEnabled`, `maxEntriesPerSession`, `maxSessions`, `bubblePosition` |

---

## Module Layout

```
shared/          ← KMP library (the publishable artifact)
  commonMain/    ← business logic, SQL schema, Compose UI
  androidMain/   ← SQLite driver, ContentProvider, ActivityCallbacks, share
  iosMain/       ← SQLite driver, UIWindow overlay, UIActivityViewController share
androidApp/      ← sample/demo app (not published)
iosApp/          ← sample/demo app (not published)
```

---

## Phase 0 — Foundation (prerequisite, not user-facing)

**Deliverable:** Project compiles with SQLDelight wired; a `LogKeep` stub exists; placeholder code removed.

- Add `sqldelight` to version catalog + `shared/build.gradle.kts` (runtime, android-driver, native-driver, coroutines-extensions)
- Define SQLDelight schema in `shared/src/commonMain/sqldelight/`:
    - `Session.sq` — id, startedAt, endedAt, endedCleanly (0/1), entryCount
    - `LogEntry.sq` — id, sessionId (FK CASCADE), timestamp, level, tag, message, stackTrace
- `expect class DatabaseDriverFactory` with `createDriver(): SqlDriver`; `actual` for Android + iOS
- `LogKeepConfig` data class (`isEnabled`, `maxEntriesPerSession`, `maxSessions`, `bubblePosition`)
- `LogLevel` enum (VERBOSE, DEBUG, INFO, WARN, ERROR)
- `BubblePosition` enum (LEFT, RIGHT)
- Stub `LogKeep` singleton object with `init()` and `log()` (bodies empty for now)
- Delete placeholder files: `App.kt`, `Greeting.kt`, `GreetingUtil.kt`, `Platform.kt`, `MainViewController.kt`

**Testable:** Project builds for Android and iOS targets. Unit tests can instantiate `DatabaseDriverFactory` and open the DB.

---

## Phase 1 — Log Capture + Session Lifecycle (Feature: "logs are silently captured")

**Deliverable:** Calling `LogKeep.log()` stores entries; sessions are created on launch and marked clean/interrupted on close. No UI yet.

### Shared (commonMain)
- `SessionRepository`: create session, mark clean, mark interrupted, delete oldest when `maxSessions` cap hit
- `LogEntryRepository`: insert entry, delete oldest entry for session when `maxEntriesPerSession` hit (FIFO)
- `LogKeep.init(driverFactory, config)`: instantiates DB, opens/creates session
- `LogKeep.log(level, tag, message, throwable?)`: guards on `isEnabled`; inserts `LogEntry` with timestamp + serialised stack trace; no-op when disabled

### Android (androidMain)
- `LogKeepInitProvider : ContentProvider` — `onCreate()`:
    1. Calls `LogKeep.init(AndroidDatabaseDriverFactory(context), config)`
    2. Observes `ProcessLifecycleOwner`: `ON_START` → start session, `ON_STOP` → mark session clean
- Registered in library's merged `AndroidManifest.xml` (`authorities="${applicationId}.logkeep"`)
- When `isEnabled = false`: skips all registration

### iOS (iosMain)
- `LogKeepIos.init(config)` — explicit call from Swift; starts session via `LogKeep.init()`
- Observes `UIApplication.didBecomeActiveNotification` / `willResignActiveNotification` for session clean/interrupted marking

### Session "clean vs interrupted" logic
- On session create: `endedCleanly = 0` (assumed interrupted until proven otherwise)
- On background/stop: update `endedCleanly = 1`, set `endedAt`
- On next fresh launch: previous session's flag is already persisted correctly; new session created

**Testable:** Unit tests (commonTest) verify: entry insertion, cap enforcement, session create/end, no-op behaviour when disabled. Integration test: log 5 entries, query DB, assert count.

---

## Phase 2 — Session List + Debug Bubble (Feature: "open the viewer, see sessions")

**Deliverable:** A draggable bubble appears on all screens. Tapping it opens a session list. Sessions show start time and clean/interrupted status.

### Shared (commonMain)
- `SessionListScreen` Composable: loads sessions via `SessionRepository` Flow, sorted most-recent-first; each row shows formatted start time, entry count, clean ✓ / interrupted ✗ badge
- `LogKeepViewer` Composable: root wrapper that handles navigation state between screens (no third-party nav lib — just a `MutableState<LogKeepDestination>`)
- `LogKeepBubble` Composable: circular 48 dp button with log icon; draggable only on Y-axis; snaps to LEFT or RIGHT edge per config; tap calls `LogKeep.openViewer()`
- `LogKeepDestination` sealed class: `SessionList` | `SessionDetail(sessionId)`

### Android (androidMain)
- `LogKeepActivityCallbacks : Application.ActivityLifecycleCallbacks`:
    - `onActivityResumed(activity)`: inflate a `ComposeView` into `activity.window.decorView` (as `FrameLayout` child at top); sets content to `LogKeepBubble` + `LogKeepViewer` overlay (hidden until bubble tapped)
    - `onActivityPaused(activity)`: remove the `ComposeView`
- Registered inside `LogKeepInitProvider` (when `isEnabled = true`)

### iOS (iosMain)
- On `LogKeepIos.init()`: create a `UIWindow` at `UIWindowLevel.alert + 1`; root VC is a `ComposeUIViewController` rendering `LogKeepBubble` + `LogKeepViewer` overlay
- Touch passthrough: regions outside the bubble and viewer pass events to the app's normal window

**Testable:** Run sample app → bubble visible on all screens → drag vertically → tap → session list appears with sessions created in Phase 1.

---

## Phase 3 — Session Detail (Feature: "inspect a session's entries")

**Deliverable:** Tapping a session in the list navigates to that session's log entries, displayed chronologically.

### Shared (commonMain)
- `SessionDetailScreen` Composable:
    - Top bar: session start timestamp + placeholder Share button (wired in Phase 4)
    - Entry list: chronological `LogEntry` rows loaded via `LogEntryRepository` Flow
    - Each row: coloured level badge (VERBOSE=grey, DEBUG=blue, INFO=green, WARN=yellow, ERROR=red), tag (bold), message text
    - Expandable row: tap shows stack trace when present
    - Back arrow: navigates back to `SessionList`
- `LogKeepViewer` updated to route `SessionDetail` destination to `SessionDetailScreen`

**Testable:** Tap a session → entries appear in order → tap an entry with a Throwable → stack trace expands. Back arrow returns to list.

---

## Phase 4 — Share / Export (Feature: "export a session log file")

**Deliverable:** Share button in session detail generates a pipe-delimited `.txt` file and invokes the platform share sheet.

### Shared (commonMain)
- `LogExporter.format(session, entries): String` — formats entries as:
  ```
  === LogKeep Session: 2024-01-15 14:23:01 | INTERRUPTED ===
  
  2024-01-15 14:23:01.456 | INFO  | AppStart | Application created
  2024-01-15 14:23:05.123 | ERROR | Network  | Request failed
      java.io.IOException: timeout
          at com.example.NetworkClient.fetch(NetworkClient.kt:42)
  ```
- File name: `logkeep_<sessionStartTimestamp>.txt`
- `expect fun shareLogFile(context: PlatformContext, fileName: String, content: String)`

### Android (androidMain)
- `actual fun shareLogFile(...)`: writes to `context.cacheDir/logkeep/`, uses `FileProvider` to get a content URI, fires `Intent.ACTION_SEND` with `text/plain` MIME type
- `FileProvider` authority + paths declared in library's merged manifest

### iOS (iosMain)
- `actual fun shareLogFile(...)`: writes to `NSTemporaryDirectory()/logkeep/`, presents `UIActivityViewController` from the topmost `UIViewController`

### Wire up Share button
- `SessionDetailScreen`: Share button calls `LogExporter.format()` then `shareLogFile()`

**Testable:** Tap Share → system share sheet appears → select "Save to Files" (or email) → file contains correct pipe-delimited content with stack traces.

---

## Phase 5 — Sample App Integration + Polish

**Deliverable:** Both sample apps demonstrate a realistic integration; library is ready for adoption.

### `androidApp` sample
- `SampleApplication : Application` — minimal; ContentProvider handles library init
- `MainActivity`: buttons calling `LogKeep.log()` at each severity level; one button simulates an exception log
- Demonstrates zero-setup Android integration

### `iosApp` sample
- `iOSApp.swift`: calls `LogKeepSDK.init(config: LogKeepConfig(isEnabled: true))` before scene setup
- `ContentView.swift`: buttons that call `LogKeepSDK.log(level:tag:message:throwable:)` from Swift

### Polish
- Confirm `isEnabled = false` → no bubble, no DB writes, no ContentProvider side effects
- Confirm > 1 000 entries → oldest dropped; > 5 sessions → oldest session + entries deleted
- Handle empty state: session list shows "No sessions yet" when DB is empty
- Handle empty session: detail screen shows "No entries in this session"

---

## Phase Dependencies

```
Phase 0 (foundation)
    └── Phase 1 (capture + sessions)   ← testable in isolation
            ├── Phase 2 (bubble + session list)  ← testable end-to-end
            │       └── Phase 3 (session detail) ← testable end-to-end
            │               └── Phase 4 (share)  ← testable end-to-end
            └── Phase 5 (sample apps)  ← can run in parallel with Phase 2–4
```

Each phase after Phase 1 can be reviewed and demoed before the next begins.

---

## Critical Files

| File | Phase | Purpose |
|---|---|---|
| `gradle/libs.versions.toml` | 0 | Add sqldelight version |
| `shared/build.gradle.kts` | 0 | Add SQLDelight plugin + deps |
| `shared/src/commonMain/sqldelight/*.sq` | 0 | DB schema |
| `shared/src/.../LogKeepConfig.kt`, `LogLevel.kt` | 0 | Config types |
| `shared/src/.../repository/SessionRepository.kt` | 1 | Session CRUD + cap |
| `shared/src/.../repository/LogEntryRepository.kt` | 1 | Entry CRUD + cap |
| `shared/src/.../LogKeep.kt` | 1 | Public API singleton |
| `shared/src/androidMain/.../LogKeepInitProvider.kt` | 1 | ContentProvider auto-init |
| `shared/src/iosMain/.../LogKeepIos.kt` | 1 | Explicit iOS init |
| `shared/src/.../ui/LogKeepBubble.kt` | 2 | Draggable bubble |
| `shared/src/.../ui/SessionListScreen.kt` | 2 | Session list UI |
| `shared/src/androidMain/.../LogKeepActivityCallbacks.kt` | 2 | Bubble injection |
| `shared/src/iosMain/.../LogKeepWindowOverlay.kt` | 2 | UIWindow bubble |
| `shared/src/.../ui/SessionDetailScreen.kt` | 3 | Entry list UI |
| `shared/src/.../LogExporter.kt` | 4 | File formatting |
| `shared/src/androidMain/.../ShareHelper.kt` | 4 | FileProvider share |
| `shared/src/iosMain/.../ShareHelper.kt` | 4 | UIActivityViewController |