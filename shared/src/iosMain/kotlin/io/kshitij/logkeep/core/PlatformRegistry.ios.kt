package io.kshitij.logkeep.core

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import io.kshitij.logkeep.IosPlatformHelper
import kotlin.concurrent.Volatile

actual object PlatformRegistry : SynchronizedObject() {

    @Volatile
    private var _helper: PlatformHelper? = null

    actual fun getHelper(): PlatformHelper {
        val helper = _helper
        if (helper != null) {
            return helper
        }
        // in case of ios nothing like context is required, so can be set automatically if not present
        setHelper(IosPlatformHelper())
        return _helper ?: throw IllegalStateException(
            "LogKeep not initialised — call LogKeepIos.init() before using LogKeep"
        )
    }

    actual fun setHelper(helper: PlatformHelper) {
        if (_helper != null) return
        synchronized(this) {
            if (_helper != null) return
            _helper = helper
        }
    }
}
