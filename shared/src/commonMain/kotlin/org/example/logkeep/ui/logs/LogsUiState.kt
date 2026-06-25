package org.example.logkeep.ui.logs

import org.example.logkeep.core.LogLevel
import org.example.logkeep.db.LogEntry

internal data class LogsUiState(
    val logs: List<LogEntry>? = null,
    val sessionStartedAt: Long? = null,
    val selectedLevel: LogLevel? = null,
    val selectedTag: String = "",
    val isFilterSheetVisible: Boolean = false,
    val pendingLevel: LogLevel? = null,
    val pendingTag: String = ""
) {
    val isFilterActive: Boolean get() = selectedLevel != null || selectedTag.isNotEmpty()
}
