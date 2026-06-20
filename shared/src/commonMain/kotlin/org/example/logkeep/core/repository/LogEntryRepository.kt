package org.example.logkeep.core.repository

import org.example.logkeep.core.LogLevel
import org.example.logkeep.db.LogKeepDatabase

internal class LogEntryRepository(private val db: LogKeepDatabase) {

    fun insertEntry(
        sessionId: Long,
        timestamp: Long,
        level: LogLevel,
        tag: String,
        message: String,
        stackTrace: String?
    ) {
        db.logEntryQueries.insertEntry(
            sessionId = sessionId,
            timestamp = timestamp,
            level = level,
            tag = tag,
            message = message,
            stackTrace = stackTrace
        )
        db.sessionQueries.incrementEntryCount(sessionId)
    }

    fun getEntryCount(sessionId: Long): Long =
        db.logEntryQueries.entryCountForSession(sessionId).executeAsOne()

    fun deleteOldestEntry(sessionId: Long) {
        val oldest = db.logEntryQueries.oldestEntryForSession(sessionId).executeAsOneOrNull()
            ?: return
        db.logEntryQueries.deleteEntry(oldest.id)
        db.sessionQueries.decrementEntryCount(sessionId)
    }
}
