package org.example.logkeep.ui.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.logkeep.core.LogLevel
import org.example.logkeep.core.repository.LogEntryRepository
import org.example.logkeep.core.repository.SessionRepository

internal class LogsViewModel(
    private val sessionId: Long,
    private val logEntryRepo: LogEntryRepository,
    private val sessionRepo: SessionRepository,
    private val queryManager: LogsQueryManager = LogsQueryManager(sessionId, logEntryRepo)
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogsUiState())
    val uiState: StateFlow<LogsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            queryManager.logsFlow
                .collect { logs -> _uiState.update { it.copy(logs = logs) } }
        }
        viewModelScope.launch {
            queryManager.filterState
                .collect { filter -> _uiState.update { it.copy(selectedLevel = filter.level) } }
        }
        viewModelScope.launch {
            sessionRepo.observeSessionById(sessionId)
                .collect { session -> _uiState.update { it.copy(sessionStartedAt = session?.startedAt) } }
        }
    }

    fun deleteAllLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            logEntryRepo.deleteAllEntriesForSession(sessionId)
        }
    }

    fun setLevelFilter(level: LogLevel?) {
        val newLevel = if (level == _uiState.value.selectedLevel) null else level
        queryManager.setLevelFilter(newLevel)
        _uiState.update { it.copy(isFilterSheetVisible = false) }
    }

    fun openFilterSheet() {
        _uiState.update { it.copy(isFilterSheetVisible = true) }
    }

    fun dismissFilterSheet() {
        _uiState.update { it.copy(isFilterSheetVisible = false) }
    }
}
