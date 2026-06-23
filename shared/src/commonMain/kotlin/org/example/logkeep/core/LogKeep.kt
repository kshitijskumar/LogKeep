package org.example.logkeep.core

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.example.logkeep.core.repository.LogEntryRepository
import org.example.logkeep.core.repository.SessionRepository
import org.example.logkeep.db.LogEntry
import org.example.logkeep.db.LogKeepDatabase
import org.example.logkeep.db.Session
import org.example.logkeep.db.logLevelAdapter
import kotlin.concurrent.Volatile

object LogKeep {
    private val lock = SynchronizedObject()

    @Volatile private var engine: LogKeepEngine? = null
    @Volatile internal var sessionRepo: SessionRepository? = null
    @Volatile internal var logEntryRepo: LogEntryRepository? = null

    internal fun init(config: LogKeepConfig) {
        println("LogStuff: init called: $engine")
        synchronized(lock) {
            if (engine != null) return
            if (!config.isEnabled) return

            val driver = PlatformRegistry.getHelper().provideSqlDriver()
            val db = LogKeepDatabase(
                driver = driver,
                LogEntryAdapter = LogEntry.Adapter(levelAdapter = logLevelAdapter)
            )
            val repo = SessionRepository(db)
            val logRepo = LogEntryRepository(db)
            sessionRepo = repo
            logEntryRepo = logRepo
            engine = LogKeepEngine(config, repo, logRepo)
        }
    }

    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) {
        println("LogStuff: log called: $level - $tag - $message - $engine")
        engine?.log(level, tag, message, throwable)
    }

    internal fun markSessionClean() = engine?.markSessionClean()

    internal fun markSessionActive() = engine?.markSessionActive()

    internal fun observeAllSessions(): Flow<List<Session>> =
        sessionRepo?.observeAllSessions() ?: emptyFlow()

    internal fun observeLogsForSession(sessionId: Long): Flow<List<LogEntry>> =
        logEntryRepo?.observeEntriesForSession(sessionId) ?: emptyFlow()

    internal fun observeSessionById(id: Long): Flow<Session?> =
        sessionRepo?.observeSessionById(id) ?: emptyFlow()
}
