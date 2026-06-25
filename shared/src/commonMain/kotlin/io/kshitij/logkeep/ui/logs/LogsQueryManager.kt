package io.kshitij.logkeep.ui.logs

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import io.kshitij.logkeep.core.LogLevel
import io.kshitij.logkeep.core.repository.LogEntryRepository
import io.kshitij.logkeep.db.LogEntry

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
        var result = logs
        filter.level?.let { level -> result = result.filter { it.level == level } }

        val tag = filter.tag.trim()
        if (tag.isNotEmpty()) result = result.filter { it.tag.contains(tag, ignoreCase = true) }

        result
    }

    fun setFilter(filter: LogsFilter) {
        _filter.value = filter
    }

    fun setLevelFilter(level: LogLevel?) {
        _filter.update { it.copy(level = level) }
    }
}
