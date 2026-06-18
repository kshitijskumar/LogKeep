package org.example.logkeep.core

import kotlin.concurrent.Volatile

actual object LogKeepHelperSetter {

    @Volatile
    private var _helper: LogKeepHelper? = null

    actual val helper: LogKeepHelper
        get() = _helper ?: throw IllegalStateException("Log helper not provided")

    actual fun setHelper(helper: LogKeepHelper) {
        if (_helper != null) {
            return
        }
        synchronized(this) {
            if (_helper != null) {
                return@synchronized
            }

            _helper = helper
        }
    }
}