package io.kshitij.logkeep.core.export

import io.kshitij.logkeep.core.repository.LogEntryRepository
import io.kshitij.logkeep.core.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal class SessionFileExporter(
    private val sessionRepo: SessionRepository,
    private val logEntryRepo: LogEntryRepository,
    private val fileWriter: SessionFileWriter,
) {
    private val mutex = Mutex()

    suspend fun getOrCreateSessionFile(sessionId: Long): String? = mutex.withLock {
        withContext(Dispatchers.IO) {
            val session = sessionRepo.observeSessionById(sessionId).firstOrNull() ?: return@withContext null
            val entries = logEntryRepo.observeEntriesForSession(sessionId).firstOrNull() ?: listOf()
            val latestLogId = entries.firstOrNull()?.id ?: 0L
            val baseName = "lk_${sessionId}_${latestLogId}"
            val content = SessionFileFormatter.format(session, entries)
            fileWriter.getOrCreateFile(baseName, content)
        }
    }
}
