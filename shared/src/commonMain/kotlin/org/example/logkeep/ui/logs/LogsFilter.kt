package org.example.logkeep.ui.logs

import org.example.logkeep.core.LogLevel

internal data class LogsFilter(
    val level: LogLevel? = null,
    val tag: String = ""
)
