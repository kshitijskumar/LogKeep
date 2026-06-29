# LogKeep

**LogKeep** is an Android library for capturing, storing, viewing, and sharing in-app logs during debug and QA sessions — think Chucker, but for logs.

When QA reports a bug, LogKeep lets you ask for the log file from their device instead of trying to reproduce the issue yourself. Logs are captured automatically every session, persisted across crashes, and can be shared as a plain-text file directly from the in-app viewer.

---

## How it works

- **Automatic capture** — call `LogKeep.log()` anywhere in your app; entries are stored to a local SQLite database, batched for efficiency
- **Session tracking** — every fresh app launch starts a new session; sessions persist across restarts and crashes, so logs are never lost
- **In-app viewer** — browse sessions, filter by log level or tag, expand individual entries, and share a session's full log file via the system share sheet
- **Zero release impact** — disabled by default; all calls are no-ops when `isEnabled = false`, with no DB writes, no background work, and no UI

---

## Screenshots

> _Screenshots coming soon. Add them to `docs/screenshots/` and link them here._

<!-- Example layout once screenshots exist:
| Sessions list | Log entries | Share |
|---|---|---|
| ![Sessions](docs/screenshots/sessions.png) | ![Entries](docs/screenshots/entries.png) | ![Share](docs/screenshots/share.png) |
-->

---

## Platform support

| Platform | Status |
|---|---|
| Android | Supported |
| iOS | Planned |

---

## Installation

### 1. Add JitPack to your repositories

In `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. Add the dependency

In your app/module `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.kshitijskumar:LogKeep:0.1.0-alpha03")
}
```

> Use `implementation` (not `debugImplementation`) — `LogKeep.log()` must be on the classpath in all build types because your code calls it directly. When `isEnabled` is `false`, all calls are no-ops with no database, no background work, and no UI.

#### Enabling only in debug builds

Use `manifestPlaceholders` in your `build.gradle.kts` to toggle the flag per build type:

```kotlin
android {
    buildTypes {
        getByName("debug") {
            manifestPlaceholders["logkeepEnabled"] = "true"
        }
        getByName("release") {
            manifestPlaceholders["logkeepEnabled"] = "false"
        }
    }
}
```

Then reference the placeholder in `AndroidManifest.xml`:

```xml
<application>
    <meta-data
        android:name="logkeep.isEnabled"
        android:value="${logkeepEnabled}" />
</application>
```

This way LogKeep is live in debug builds and fully inert in release — no code changes needed between build types.

---

## Setup

### Enable LogKeep

LogKeep initializes automatically via a `ContentProvider` — no `Application` or `Activity` code required. The only required step is enabling it in your `AndroidManifest.xml`:

```xml
<application>
    <!-- Required: opt in to enable capture -->
    <meta-data
        android:name="logkeep.isEnabled"
        android:value="true" />
</application>
```

LogKeep is **disabled by default**. If `logkeep.isEnabled` is absent or `false`, no database is created, no logs are stored, and no UI is shown.

### Log from anywhere

```kotlin
LogKeep.log(LogLevel.DEBUG, "Network", "Request started")
LogKeep.log(LogLevel.INFO,  "Auth",    "User signed in")
LogKeep.log(LogLevel.ERROR, "Network", "Request failed", exception)
```

Available log levels: `VERBOSE`, `DEBUG`, `INFO`, `WARN`, `ERROR`

### Open the log viewer

When LogKeep is enabled, it automatically injects a floating **"Logs" button** in the bottom-right corner of every Activity. Tapping it opens the log viewer — no code required.

You can also launch it programmatically from your own debug menu:

```kotlin
startActivity(Intent(context, LogKeepActivity::class.java))
```

From the viewer, QA can browse sessions, filter by log level or tag, and share the full log file for any session.

### Route an existing logger into LogKeep

If you already have a logging abstraction, add a single line to route it:

```kotlin
// Example: Timber tree
class LogKeepTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val level = when (priority) {
            Log.VERBOSE -> LogLevel.VERBOSE
            Log.DEBUG   -> LogLevel.DEBUG
            Log.INFO    -> LogLevel.INFO
            Log.WARN    -> LogLevel.WARN
            else        -> LogLevel.ERROR
        }
        LogKeep.log(level, tag ?: "App", message, t)
    }
}
```

---

## Configuration

All configuration is optional. Set overrides via `<meta-data>` in `AndroidManifest.xml`:

```xml
<application>
    <meta-data android:name="logkeep.isEnabled"            android:value="true" />
    <meta-data android:name="logkeep.maxEntriesPerSession" android:value="1000" />
    <meta-data android:name="logkeep.maxSessions"          android:value="5" />
</application>
```

### Configuration reference

| Key | Type | Default | Description |
|---|---|---|---|
| `logkeep.isEnabled` | Boolean | `false` | Master switch. Set to `true` to enable capture and the in-app viewer. Keep absent or `false` in release builds. |
| `logkeep.maxEntriesPerSession` | Int | `1000` | Maximum log entries stored per session. When the limit is reached, the oldest entries in that session are dropped to make room for new ones. |
| `logkeep.maxSessions` | Int | `5` | Maximum number of sessions retained on device. When this limit is reached, the oldest session (and all its entries) is deleted entirely. |

> `maxBatchSize` (default `20`) and `batchWindowMs` (default `500 ms`) control how often entries are flushed to the database. These are not currently configurable via manifest and use their defaults.

---

## Log viewer features

- **Session list** — sessions shown most-recent-first, each labelled with its start time and a clean/interrupted status indicator
- **Entry list** — chronological log entries within a session, color-coded by level
- **Filter by level** — show only `ERROR`, `WARN`, etc.
- **Filter by tag** — narrow entries to a specific tag
- **Share** — export the complete, unfiltered session as a human-readable `.txt` file via the system share sheet (Slack, email, Drive, etc.)
- **Delete** — remove a session and all its entries from the device

---

## Exported log format

Each exported file is a plain-text file readable in any editor:

```
Session started at: 10:32:00 - Jun 29 2025
Total logs: 3


10:32:01 - DEBUG - Network - Request started

10:32:01 - INFO - Auth - User signed in

10:32:05 - ERROR - Network - Request failed
java.net.SocketTimeoutException: timeout
    at okhttp3.internal.connection.RealCall.execute(RealCall.kt:144)
    ...

```

---

## Building the project

```bash
# Build the shared library
./gradlew :shared:build

# Run all tests
./gradlew :shared:allTests

# Build the Android demo app
./gradlew :androidApp:assembleDebug

# Publish to local Maven (~/.m2)
./gradlew :shared:publishToMavenLocal
```

---

## License

[Apache 2.0](LICENSE)
