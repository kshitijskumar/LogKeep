package org.example.logkeep.core

data class LogKeepConfig(
    val isEnabled: Boolean = true,
    val maxEntriesPerSession: Int = 1_000,
    val maxSessions: Int = 5
)
