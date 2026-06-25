package io.kshitij.logkeep

import android.util.Log
import io.kshitij.logkeep.core.LogKeep
import io.kshitij.logkeep.core.LogLevel

object Logger {

    fun logDebug(tag: String, msg: String) {
        LogKeep.log(
            level = LogLevel.entries.random(),
            tag = tag,
            message = msg
        )
        Log.d(tag, msg)
    }

}