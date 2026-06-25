package io.kshitij.logkeep.core

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import io.kshitij.logkeep.core.repository.LogEntryRepository
import io.kshitij.logkeep.core.repository.SessionRepository
import io.kshitij.logkeep.db.LogEntry
import io.kshitij.logkeep.db.LogKeepDatabase
import io.kshitij.logkeep.db.logLevelAdapter
import kotlin.concurrent.Volatile

object LogKeep {
    private val lock = SynchronizedObject()

    @Volatile private var engine: LogKeepEngine? = null
    @Volatile private var _sessionRepo: SessionRepository? = null
    @Volatile private var _logEntryRepo: LogEntryRepository? = null

    internal val sessionRepository: SessionRepository? get() = _sessionRepo
    internal val logEntryRepository: LogEntryRepository? get() = _logEntryRepo

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
            _sessionRepo = repo
            _logEntryRepo = logRepo
            engine = LogKeepEngine(config, repo, logRepo)
        }
    }

    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) {
        println("LogStuff: log called: $level - $tag - $message - $engine")
        engine?.log(level, tag, message, throwable)
    }

    internal fun markSessionClean() = engine?.markSessionClean()

    internal fun markSessionActive() = engine?.markSessionActive()
}
