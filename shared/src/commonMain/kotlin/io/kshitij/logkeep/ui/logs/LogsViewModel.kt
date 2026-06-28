package io.kshitij.logkeep.ui.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import io.kshitij.logkeep.core.LogLevel
import io.kshitij.logkeep.core.repository.LogEntryRepository
import io.kshitij.logkeep.core.repository.SessionRepository

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
                .collect { filter ->
                    _uiState.update { it.copy(selectedLevel = filter.level, selectedTag = filter.tag) }
                }
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

    fun openFilterSheet() {
        _uiState.update {
            it.copy(
                isFilterSheetVisible = true,
                pendingLevel = it.selectedLevel,
                pendingTag = it.selectedTag
            )
        }
    }

    fun dismissFilterSheet() {
        _uiState.update { it.copy(isFilterSheetVisible = false) }
    }

    fun setPendingLevel(level: LogLevel?) {
        val newLevel = if (level == _uiState.value.pendingLevel) null else level
        _uiState.update { it.copy(pendingLevel = newLevel) }
    }

    fun setPendingTag(tag: String) {
        _uiState.update { it.copy(pendingTag = tag) }
    }

    fun applyFilter() {
        val current = _uiState.value
        queryManager.setFilter(LogsFilter(level = current.pendingLevel, tag = current.pendingTag))
        _uiState.update {
            it.copy(
                selectedLevel = it.pendingLevel,
                selectedTag = it.pendingTag,
                isFilterSheetVisible = false
            )
        }
    }

    fun resetPendingFilter() {
        _uiState.update { it.copy(pendingLevel = null, pendingTag = "") }
    }

    fun clearFilter() {
        queryManager.setFilter(LogsFilter())
        resetPendingFilter()
    }
}
