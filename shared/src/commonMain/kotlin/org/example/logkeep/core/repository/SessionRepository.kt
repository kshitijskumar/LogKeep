package org.example.logkeep.core.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import org.example.logkeep.core.utils.TimeProvider
import org.example.logkeep.db.LogKeepDatabase
import org.example.logkeep.db.Session

internal class SessionRepository(private val db: LogKeepDatabase) {

    fun createSession(startedAt: Long): Long {
        db.sessionQueries.insertSession(
            startedAt = startedAt,
            endedAt = null,
            endedCleanly = false,
            entryCount = 0
        )
        return db.sessionQueries.lastInsertRowId().executeAsOne()
    }

    fun markSessionEnded(id: Long, cleanly: Boolean) {
        db.sessionQueries.updateSessionEnded(
            endedAt = TimeProvider.now(),
            endedCleanly = cleanly,
            id = id
        )
    }

    fun markSessionActive(id: Long) {
        db.sessionQueries.markSessionActive(id)
    }

    fun getSessionCount(): Long = db.sessionQueries.sessionCount().executeAsOne()

    fun getOldestSessionId(): Long? = db.sessionQueries.oldestSession().executeAsOneOrNull()?.id

    fun deleteSession(id: Long) {
        db.sessionQueries.deleteSession(id)
    }

    fun observeAllSessions(): Flow<List<Session>> =
        db.sessionQueries.allSessions().asFlow().mapToList(Dispatchers.IO)
}
