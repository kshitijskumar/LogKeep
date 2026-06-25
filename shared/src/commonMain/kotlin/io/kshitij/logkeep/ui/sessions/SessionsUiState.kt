package io.kshitij.logkeep.ui.sessions

import io.kshitij.logkeep.db.Session

internal data class SessionsUiState(
    val sessions: List<Session>? = null
)
