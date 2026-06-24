package org.example.logkeep.ui.sessions

import org.example.logkeep.db.Session

internal data class SessionsUiState(
    val sessions: List<Session>? = null
)
