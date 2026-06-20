package org.example.logkeep.core

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import org.example.logkeep.core.repository.LogEntryRepository
import org.example.logkeep.core.repository.SessionRepository
import org.example.logkeep.db.LogEntry
import org.example.logkeep.db.LogKeepDatabase
import org.example.logkeep.db.logLevelAdapter
import kotlin.concurrent.Volatile

object LogKeep {
    private val lock = SynchronizedObject()

    @Volatile private var engine: LogKeepEngine? = null

    internal fun init(config: LogKeepConfig) {
        synchronized(lock) {
            if (engine != null) return
            if (!config.isEnabled) return

            val driver = PlatformRegistry.getHelper().provideSqlDriver()
            val db = LogKeepDatabase(
                driver = driver,
                LogEntryAdapter = LogEntry.Adapter(levelAdapter = logLevelAdapter)
            )
            engine = LogKeepEngine(config, SessionRepository(db), LogEntryRepository(db))
        }
    }

    fun log(level: LogLevel, tag: String, message: String, throwable: Throwable? = null) {
        engine?.log(level, tag, message, throwable)
    }

    internal fun markSessionClean() = engine?.markSessionClean()

    internal fun markSessionActive() = engine?.markSessionActive()
}
