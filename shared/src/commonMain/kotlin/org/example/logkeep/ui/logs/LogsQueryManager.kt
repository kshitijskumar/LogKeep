package org.example.logkeep.ui.logs

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import org.example.logkeep.core.LogLevel
import org.example.logkeep.core.repository.LogEntryRepository
import org.example.logkeep.db.LogEntry

internal class LogsQueryManager(
    sessionId: Long,
    logEntryRepo: LogEntryRepository
) {
    private val _filter = MutableStateFlow(LogsFilter())
    val filterState: StateFlow<LogsFilter> = _filter.asStateFlow()

    val logsFlow: Flow<List<LogEntry>> = combine(
        logEntryRepo.observeEntriesForSession(sessionId),
        _filter
    ) { logs, filter ->
        val levelFilter = filter.level ?: return@combine logs
        logs.filter { it.level == levelFilter }
    }

    fun setLevelFilter(level: LogLevel?) {
        _filter.update { it.copy(level = level) }
    }
}
