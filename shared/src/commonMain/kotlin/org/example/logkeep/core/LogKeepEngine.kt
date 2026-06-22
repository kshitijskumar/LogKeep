package org.example.logkeep.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.example.logkeep.core.repository.LogEntryInsert
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
            startProcessingLogRequests()
        }
    }

    private suspend fun startProcessingLogRequests() {
        for (firstRequest in channel) {
            val batch = mutableListOf(firstRequest)
            withTimeoutOrNull(config.batchWindowMs) {
                while (batch.size < config.maxBatchSize) {
                    batch.add(channel.receive())
                }
            }

            entryRepo.insertEntriesBatch(
                sessionId = currentSessionId,
                entries = batch.map {
                    LogEntryInsert(
                        timestamp = it.timestamp,
                        level = it.level,
                        tag = it.tag,
                        message = it.message,
                        stackTrace = it.stackTrace
                    )
                }
            )
            val count = entryRepo.getEntryCount(currentSessionId)
            val overflow = count - config.maxEntriesPerSession
            if (overflow > 0) {
                entryRepo.deleteOldestEntries(currentSessionId, overflow)
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
