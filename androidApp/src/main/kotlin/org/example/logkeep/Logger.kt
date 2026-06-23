package org.example.logkeep

import android.util.Log
import org.example.logkeep.core.LogKeep
import org.example.logkeep.core.LogLevel

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