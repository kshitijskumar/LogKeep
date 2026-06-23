package org.example.logkeep.ui.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.logkeep.core.LogKeep

internal class LogsViewModel(sessionId: Long) : ViewModel() {

    private val _uiState = MutableStateFlow(LogsUiState())
    val uiState: StateFlow<LogsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            LogKeep.observeLogsForSession(sessionId)
                .collect { logs -> _uiState.update { it.copy(logs = logs) } }
        }
        viewModelScope.launch {
            LogKeep.observeSessionById(sessionId)
                .collect { session -> _uiState.update { it.copy(sessionStartedAt = session?.startedAt) } }
        }
    }
}
