package io.kshitij.logkeep.ui.logs

import io.kshitij.logkeep.core.LogLevel

internal data class LogsFilter(
    val level: LogLevel? = null,
    val tag: String = ""
)
