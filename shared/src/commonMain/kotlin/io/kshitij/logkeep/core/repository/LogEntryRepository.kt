package io.kshitij.logkeep.core.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import io.kshitij.logkeep.core.LogLevel
import io.kshitij.logkeep.db.LogEntry
import io.kshitij.logkeep.db.LogKeepDatabase

internal data class LogEntryInsert(
    val timestamp: Long,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val stackTrace: String?
)

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

    fun insertEntriesBatch(sessionId: Long, entries: List<LogEntryInsert>) {
        db.transaction {
            for (entry in entries) {
                db.logEntryQueries.insertEntry(
                    sessionId = sessionId,
                    timestamp = entry.timestamp,
                    level = entry.level,
                    tag = entry.tag,
                    message = entry.message,
                    stackTrace = entry.stackTrace
                )
            }
            db.sessionQueries.incrementEntryCountBy(entries.size.toLong(), sessionId)
        }
    }

    fun deleteOldestEntries(sessionId: Long, count: Long) {
        db.transaction {
            db.logEntryQueries.deleteOldestEntries(sessionId = sessionId, count = count)
            db.sessionQueries.decrementEntryCountBy(count, sessionId)
        }
    }

    fun deleteAllEntriesForSession(sessionId: Long) {
        db.transaction {
            db.logEntryQueries.deleteAllEntriesForSession(sessionId)
            db.sessionQueries.resetEntryCount(sessionId)
        }
    }

    fun observeEntriesForSession(sessionId: Long): Flow<List<LogEntry>> =
        db.logEntryQueries.entriesForSession(sessionId).asFlow().mapToList(Dispatchers.IO)
}
