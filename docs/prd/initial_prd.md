# PRD: In-App Log Viewer & Recorder Library

*Internal tool — "Chucker, but for app logs"*

## 1. Overview

This library gives mobile apps (Android and iOS, sharing a common Kotlin core) the ability to capture, store, view, and share the logs produced by the app's existing logging layer during a single app session. The goal is to make issue debugging faster by allowing developers to instrument a build with extensive logging, share that build with QA, and then request the captured log file from QA when an issue is reported — instead of first having to reproduce the issue themselves.

This document defines what is to be built for version 1 (v1) and what is explicitly out of scope. It does not describe implementation details, architecture, or technology choices.

## 2. Problem Statement

When QA reports an issue, the default debugging workflow is for a developer to try to reproduce it locally. This is often slow and unreliable — bugs can be environment-specific, steps to reproduce are frequently incomplete, or the issue happens only intermittently in QA's hands.

Developers already add log statements via the app's existing logging layer to trace behaviour and state. However, there is currently no way for those logs to leave the device without connecting it to a development machine. This means the logs a developer wrote specifically to understand the app's behaviour are inaccessible to anyone investigating an issue reported from a QA or test device.

The missing capability is a mechanism for:

- A developer to add extensive, meaningful logs to a build and share that build with QA

- QA to passively capture those logs over the course of a testing session, with no technical setup required on their part

- QA to hand the captured log file back to the developer when an issue is reported, so the developer can investigate using real session data instead of first having to reproduce the issue themselves

## 3. Goals

- Allow developers to instrument a debug build with extensive logging that is automatically captured during QA testing sessions

- Allow QA to export and share the captured logs from a session as a file, without needing to understand what the logs contain

- Produce a well-structured, human-readable log file that a developer can search and analyse using standard tooling

- Ensure the feature has zero impact on release builds by default

## 4. Non-Goals (Out of Scope for v1)

- Capturing crash reports, stack traces from uncaught exceptions, or any crash-analytics functionality (this is covered by existing crash reporting tools)

- Capturing structured/key-value metadata beyond a text message and an optional error/exception detail

- Searching or filtering across multiple sessions at once

- Exporting logs in formats other than plain text

- Uploading logs to a remote server or third-party service

- Redaction or masking of sensitive information within logs

- Any UI or entry point in production/release builds for end users (v1 is debug-only)

## 5. Definitions

| **Term** | **Definition** |
| --- | --- |
| **Session** | The period from when the app is freshly launched (after being fully killed, not merely backgrounded/minimised) until it is killed again. Each session is tracked and stored separately. |
| **Log entry** | A single record written via the app's existing logging layer, consisting of a timestamp, severity level, tag, message, and an optional error/exception detail. |
| **Clean session** | A session that ended through a normal app close, as opposed to being interrupted by a crash. |

## 6. User Stories

- As a developer, I want every log I write via our existing logging layer to be automatically captured and stored during a QA session, so that when QA reports an issue I have a real log trail to investigate rather than trying to reproduce the issue myself.

- As a QA tester, I want to export the logs from a session and share the file (e.g., via Slack or email) with a developer when I report an issue, without needing to understand what the logs mean or connect my device to a computer.

- As a developer receiving a log file from QA, I want logs to be grouped by session and ordered chronologically, so I can reconstruct what happened during the session in which the issue occurred.

- As a developer receiving a log file from QA, I want the file to be well-structured and readable so I can filter and search it in any text editor or tooling without needing a special viewer.

- As a developer, I want this feature to be active only in debug/internal builds, so that it has no presence or overhead in production.

## 7. Functional Requirements

### 7.1 Log Capture

- The library must expose a logging method that the consuming app calls directly to record a log entry. The library owns the capture interface — it is not responsible for hooking into any platform logging mechanism automatically.

- Adopters who already have an existing logging layer may route calls from that layer into the library's method. Adopters without one may call the library's method directly. Either integration path must be equally supported.

- The library's logging method must accept: severity level, tag, message text, and an optional error/exception detail.

- Each captured log entry must record: timestamp (set at the time the library method is called), severity level, tag, message text, and the optional error/exception detail.

- Crash-related logs (uncaught exceptions / crash reports) are explicitly not part of this capture — only entries explicitly passed to the library's logging method.

### 7.2 Sessions

- A new session must begin every time the app is freshly launched after being fully killed (not when resumed from background/minimised state).

- All log entries captured during a session must be associated with that session.

- Each session must record at minimum: a start time, and whether it ended cleanly or appears to have been interrupted (i.e., the app did not shut down normally, such as after a crash).

- Sessions and their logs must persist across app restarts (i.e., stored on device, not only in memory), so that a previous session's logs remain viewable after the app is relaunched.

### 7.3 Session Retention & Limits

- Each session's log storage must be capped at a defined maximum number of entries (or size). Once the cap is reached, the oldest entries within that session are discarded to make room for new ones.

- The total number of sessions retained on device must be capped. Once the cap is reached, the oldest sessions (and their logs) are discarded entirely.

- Exact cap values are configurable but must have sensible defaults suitable for typical debugging needs without consuming excessive device storage.

### 7.4 Log Viewer UI

- The entry point (debug menu item) must present a list of recorded sessions, most recent first, showing at minimum the session start time and whether it ended cleanly or was interrupted.

- Selecting a session must expose a share action that allows the user to export and share that session's full log file (see 7.5).

- Within a session, log entries must be presented in chronological order.

> **Open question for TRD:** Given that the primary consumer of log content is the developer (not QA), it is worth evaluating whether an in-app log viewer — showing individual log entries, filtering, and per-entry detail — is necessary in v1, or whether the session list + share action alone is sufficient. A full viewer adds UI surface that QA does not need; developers will read the shared file. The TRD should assess the trade-off and decide whether to include the viewer, defer it to v2, or omit it entirely.

### 7.5 Sharing / Export

- From the session view, the user must be able to share the full log file for that session via the device's standard share mechanism (e.g., Slack, email, or any other share target on the device).

- The export always contains the complete, unfiltered log for that session. Exporting a filtered or partial subset is explicitly out of scope for v1.

- The exported file must be human-readable as plain text, with each log entry showing its timestamp, severity level, tag, and message (and exception detail if present) in a clear, consistent layout.

### 7.6 Access & Entry Point

- The log viewer must be reachable via an entry point in the app's existing debug menu.

- The library's capture and UI must be active only in debug builds. In release/production builds, the feature must be fully disabled by default — no capture overhead, no UI, no entry point — controllable via a single configuration toggle, so that a team can opt in for a specific internal/external test build if desired.

## 8. Platform Scope

- The library must support both Android and iOS apps.

- The core capture, storage, session, and filtering logic must be shared between both platforms; only the UI presentation may differ to fit each platform's conventions.

- On Android, the entry point is the existing debug menu.

- On iOS, an equivalent debug-only entry point must be defined (exact mechanism to be confirmed, e.g., a debug menu/screen if one exists, or another debug-only trigger) — but it must follow the same "debug build only" principle as Android.

## 9. Success Criteria

- When QA reports an issue, a developer can ask for the session log file and use it to investigate without first having to reproduce the issue locally.

- A QA tester can export and share a session log file using only in-app actions, with no technical knowledge or device setup required.

- Logs from a previous session (including one that ended in a crash) remain available and shareable after the app is relaunched.

- Production builds are entirely unaffected — no capture overhead, no UI, no entry point — unless explicitly enabled via a configuration flag.

## 10. Open Questions

- Exact default values for per-session entry cap and total session retention cap.

- Exact iOS entry point mechanism for the debug-only log viewer.

- **In-app log viewer scope (for TRD):** Should v1 include a full in-app viewer (browseable log entries, filtering by tag/level, expandable entry detail), or is a session list with a share action sufficient? The viewer adds meaningful UI complexity for a feature that QA will use without understanding the content, and developers will read the exported file anyway. The TRD should decide.

- Whether a future version should support exporting logs in formats other than plain text, or correlating a session's "interrupted" flag with crash reporting data — flagged for v2 consideration, not required now.