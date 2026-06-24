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
import org.example.logkeep.core.repository.LogEntryRepository
import org.example.logkeep.core.repository.SessionRepository

internal class LogsViewModel(
    private val sessionId: Long,
    private val logEntryRepo: LogEntryRepository,
    private val sessionRepo: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogsUiState())
    val uiState: StateFlow<LogsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            logEntryRepo.observeEntriesForSession(sessionId)
                .collect { logs -> _uiState.update { it.copy(logs = logs) } }
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
}
