package org.example.logkeep.core.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import org.example.logkeep.core.LogLevel
import org.example.logkeep.db.LogEntry
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
        db.transaction {
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
    }

    fun getEntryCount(sessionId: Long): Long =
        db.logEntryQueries.entryCountForSession(sessionId).executeAsOne()

    fun deleteOldestEntry(sessionId: Long) {
        db.transaction {
            val oldest = db.logEntryQueries.oldestEntryForSession(sessionId).executeAsOneOrNull()
                ?: return@transaction
            db.logEntryQueries.deleteEntry(oldest.id)
            db.sessionQueries.decrementEntryCount(sessionId)
        }
    }

    fun observeEntriesForSession(sessionId: Long): Flow<List<LogEntry>> =
        db.logEntryQueries.entriesForSession(sessionId).asFlow().mapToList(Dispatchers.IO)
}
