package io.kshitij.logkeep.core

data class LogKeepConfig(
    val isEnabled: Boolean = true,
    val maxEntriesPerSession: Int = 1_000,
    val maxSessions: Int = 5,
    val maxBatchSize: Int = 20,
    val batchWindowMs: Long = 500L
)
