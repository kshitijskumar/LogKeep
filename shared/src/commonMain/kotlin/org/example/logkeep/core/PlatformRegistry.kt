package org.example.logkeep.core

/**
 * Holds the platform-specific [PlatformHelper] for the lifetime of the process.
 *
 * [setHelper] must be called exactly once by platform init code before any call to
 * [LogKeep.init]. Calling [getHelper] before [setHelper] throws [IllegalStateException].
 *
 * This registry is not intended to be accessed by app-level code.
 */
expect object PlatformRegistry {
    fun getHelper(): PlatformHelper
    fun setHelper(helper: PlatformHelper)
}
