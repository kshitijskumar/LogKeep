package org.example.logkeep.core

import kotlin.concurrent.Volatile

actual object PlatformRegistry {

    @Volatile
    private var _helper: PlatformHelper? = null

    actual fun getHelper(): PlatformHelper =
        _helper ?: throw IllegalStateException(
            "LogKeep not initialised — ensure the library's ContentProvider is registered in your manifest"
        )

    actual fun setHelper(helper: PlatformHelper) {
        if (_helper != null) return
        synchronized(this) {
            if (_helper != null) return@synchronized
            _helper = helper
        }
    }
}
