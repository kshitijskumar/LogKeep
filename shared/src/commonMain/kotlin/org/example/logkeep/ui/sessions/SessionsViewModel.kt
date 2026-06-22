package org.example.logkeep.ui.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.example.logkeep.core.LogKeep

internal class SessionsViewModel(
    private val sessionClickedDelegate: (sessionId: Long) -> Unit
) : ViewModel() {

    val uiState: StateFlow<SessionsUiState> = LogKeep.observeAllSessions()
        .map { sessions -> SessionsUiState(sessions = sessions) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SessionsUiState()
        )

    fun onSessionClicked(sessionId: Long) {
        sessionClickedDelegate.invoke(sessionId)
    }
}
