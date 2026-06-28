package io.kshitij.logkeep.core.export

import io.kshitij.logkeep.core.repository.LogEntryRepository
import io.kshitij.logkeep.core.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

internal class SessionFileExporter(
    private val sessionRepo: SessionRepository,
    private val logEntryRepo: LogEntryRepository,
    private val fileWriter: SessionFileWriter,
) {
    suspend fun getOrCreateSessionFile(sessionId: Long): String? = withContext(Dispatchers.IO) {
        val session = sessionRepo.observeSessionById(sessionId).firstOrNull() ?: return@withContext null
        val entries = logEntryRepo.observeEntriesForSession(sessionId).firstOrNull() ?: listOf()
        val latestLogId = entries.firstOrNull()?.id ?: 0L
        val fileName = "lk_${sessionId}_${latestLogId}.txt"
        val content = SessionFileFormatter.format(session, entries)
        fileWriter.getOrCreateFile(fileName, content)
    }
}
