package org.example.logkeep.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.example.logkeep.core.repository.LogEntryRepository
import org.example.logkeep.core.repository.SessionRepository
import org.example.logkeep.core.utils.TimeProvider

internal class LogKeepEngine(
    private val config: LogKeepConfig,
    private val sessionRepo: SessionRepository,
    private val entryRepo: LogEntryRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    val currentSessionId: Long

    init {
        while (sessionRepo.getSessionCount() >= config.maxSessions) {
            sessionRepo.getOldestSessionId()?.let { sessionRepo.deleteSession(it) } ?: break
        }
        currentSessionId = sessionRepo.createSession(TimeProvider.now())
        println("LogStuff: session created: $currentSessionId")
    }

    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        scope.launch {
            entryRepo.insertEntry(
                sessionId = currentSessionId,
                timestamp = TimeProvider.now(),
                level = level,
                tag = tag,
                message = message,
                stackTrace = throwable?.stackTraceToString()
            )
            if (entryRepo.getEntryCount(currentSessionId) > config.maxEntriesPerSession) {
                entryRepo.deleteOldestEntry(currentSessionId)
            }
        }
    }

    fun markSessionClean() {
        scope.launch { sessionRepo.markSessionEnded(currentSessionId, cleanly = true) }
    }

    fun markSessionActive() {
        scope.launch { sessionRepo.markSessionActive(currentSessionId) }
    }
}
