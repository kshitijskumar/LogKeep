package io.kshitij.logkeep.ui.logs

import io.kshitij.logkeep.core.LogLevel
import io.kshitij.logkeep.db.LogEntry

internal data class LogsUiState(
    val logs: List<LogEntry>? = null,
    val sessionStartedAt: Long? = null,
    val selectedLevel: LogLevel? = null,
    val selectedTag: String = "",
    val isFilterSheetVisible: Boolean = false,
    val pendingLevel: LogLevel? = null,
    val pendingTag: String = "",
    val isExportingFile: Boolean = false,
) {
    val isFilterActive: Boolean get() = selectedLevel != null || selectedTag.isNotEmpty()
}
