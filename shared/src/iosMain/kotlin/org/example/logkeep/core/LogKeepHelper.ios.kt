package org.example.logkeep.core

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.concurrent.Volatile

actual object LogKeepHelperSetter : SynchronizedObject() {

    @Volatile
    private var _helper: LogKeepHelper? = null

    actual val helper: LogKeepHelper
        get() = _helper ?: throw IllegalStateException("LogHelper not provided")

    actual fun setHelper(helper: LogKeepHelper) {
        if (_helper != null) {
            return
        }
        synchronized(this) {
            if (_helper != null) {
                return
            }
            _helper = helper
        }
    }
}