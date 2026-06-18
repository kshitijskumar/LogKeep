package org.example.logkeep.core

import app.cash.sqldelight.db.SqlDriver

/**
 * Implemented by each platform to supply platform-specific dependencies to LogKeep.
 *
 * Platform init code is responsible for creating an implementation and registering it
 * via [PlatformRegistry.setHelper] before [LogKeep.init] is called:
 * - **Android**: done automatically by the library's ContentProvider.
 * - **iOS**: done by the app calling `LogKeepIos.init(config)`.
 *
 * This interface is not intended to be implemented or called by app-level code.
 */
interface PlatformHelper {
    fun provideSqlDriver(): SqlDriver
}
