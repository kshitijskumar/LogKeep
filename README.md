# LogKeep

**LogKeep** is a Kotlin Multiplatform library for capturing, storing, and viewing in-app logs during debug sessions — think Chucker, but for logs.

- Automatically captures logs to a local SQLite database organized by session
- In-app viewer: browse sessions, filter by level or tag, delete old sessions
- Detects clean vs crashed sessions so you never lose logs after a crash
- No-op in release builds when `isEnabled = false` — all calls are skipped, no DB writes, no UI shown

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
    implementation("com.github.kshitijskumar:logkeep-android:0.1.0-alpha01")
}
```

> Use `implementation` (not `debugImplementation`) — `LogKeep.log()` must be on the classpath in all build types since your app code calls it. Disable capture in release by setting `isEnabled = false` via manifest meta-data (see Configuration below).

---

## Usage

### Android

LogKeep initializes itself automatically via a `ContentProvider` — no code required in `Application` or `Activity`.

```kotlin
// Log from anywhere in your app
LogKeep.log(LogLevel.DEBUG, "Network", "Request started")
LogKeep.log(LogLevel.ERROR, "Network", "Request failed", exception)
```

Available log levels: `VERBOSE`, `DEBUG`, `INFO`, `WARN`, `ERROR`

To open the LogKeep viewer, launch `LogKeepActivity`:

```kotlin
startActivity(Intent(context, LogKeepActivity::class.java))
```

### Configuration (optional)

Override defaults via `<meta-data>` in your `AndroidManifest.xml`:

```xml
<application>
    <meta-data android:name="logkeep.isEnabled"          android:value="true" />
    <meta-data android:name="logkeep.maxEntriesPerSession" android:value="1000" />
    <meta-data android:name="logkeep.maxSessions"        android:value="5" />
</application>
```

### iOS

Call `LogKeepIos.start()` before logging, typically in your app's entry point:

```kotlin
// In iOSApp.init() or SwiftUI @main body
LogKeepIos.start(LogKeepConfig(isEnabled = true))
```

Then log the same way:

```kotlin
LogKeep.log(LogLevel.INFO, "AppStart", "App launched")
```

---

## Configuration reference

| Property | Default | Description |
|---|---|---|
| `isEnabled` | `true` | Set to `false` to disable all logging (no-op mode) |
| `maxEntriesPerSession` | `1000` | Oldest entries are dropped when this limit is reached |
| `maxSessions` | `5` | Oldest sessions are deleted when this limit is reached |
| `maxBatchSize` | `20` | Number of entries to batch before flushing to DB |
| `batchWindowMs` | `500` | Max time (ms) to wait before flushing a partial batch |

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

Apache 2.0
