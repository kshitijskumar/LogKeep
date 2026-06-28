package io.kshitij.logkeep

import android.util.Log
import io.kshitij.logkeep.core.LogKeep
import io.kshitij.logkeep.core.LogLevel

object Logger {

    fun logDebug(tag: String, msg: String, throwable: Throwable? = null) {
        LogKeep.log(
            level = LogLevel.entries.random(),
            tag = tag,
            message = msg,
            throwable = throwable
        )
        Log.d(tag, msg)
    }

}