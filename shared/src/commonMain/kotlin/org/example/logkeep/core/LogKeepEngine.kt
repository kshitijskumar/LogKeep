package org.example.logkeep.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
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
    private data class LogRequest(
        val timestamp: Long,
        val level: LogLevel,
        val tag: String,
        val message: String,
        val stackTrace: String?
    )

    private val channel = Channel<LogRequest>(
        capacity = config.maxEntriesPerSession,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val currentSessionId: Long

    init {
        while (sessionRepo.getSessionCount() >= config.maxSessions) {
            sessionRepo.getOldestSessionId()?.let { sessionRepo.deleteSession(it) } ?: break
        }
        currentSessionId = sessionRepo.createSession(TimeProvider.now())
        println("LogStuff: session created: $currentSessionId")

        scope.launch {
            for (request in channel) {
                entryRepo.insertEntry(
                    sessionId = currentSessionId,
                    timestamp = request.timestamp,
                    level = request.level,
                    tag = request.tag,
                    message = request.message,
                    stackTrace = request.stackTrace
                )
                if (entryRepo.getEntryCount(currentSessionId) > config.maxEntriesPerSession) {
                    entryRepo.deleteOldestEntry(currentSessionId)
                }
            }
        }
    }

    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        channel.trySend(
            LogRequest(
                timestamp = TimeProvider.now(),
                level = level,
                tag = tag,
                message = message,
                stackTrace = throwable?.stackTraceToString()
            )
        )
    }

    fun markSessionClean() {
        scope.launch { sessionRepo.markSessionEnded(currentSessionId, cleanly = true) }
    }

    fun markSessionActive() {
        scope.launch { sessionRepo.markSessionActive(currentSessionId) }
    }
}
