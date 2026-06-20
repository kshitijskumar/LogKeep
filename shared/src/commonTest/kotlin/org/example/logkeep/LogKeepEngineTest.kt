package org.example.logkeep

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.example.logkeep.core.LogKeepConfig
import org.example.logkeep.core.LogKeepEngine
import org.example.logkeep.core.LogLevel
import org.example.logkeep.core.repository.LogEntryRepository
import org.example.logkeep.core.repository.SessionRepository
import org.example.logkeep.db.LogKeepDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private data class EngineSetup(
    val engine: LogKeepEngine,
    val sessionRepo: SessionRepository,
    val entryRepo: LogEntryRepository,
    val db: LogKeepDatabase
)

private fun buildEngine(maxEntries: Int = 1_000, maxSessions: Int = 5): EngineSetup {
    val db = createTestDatabase()
    val sr = SessionRepository(db)
    val er = LogEntryRepository(db)
    val config = LogKeepConfig(isEnabled = true, maxEntriesPerSession = maxEntries, maxSessions = maxSessions)
    // Dispatchers.Unconfined makes launched coroutines run synchronously — safe for tests
    val eng = LogKeepEngine(config, sr, er, CoroutineScope(Dispatchers.Unconfined))
    return EngineSetup(eng, sr, er, db)
}

class LogKeepEngineTest {

    @Test
    fun sessionCreatedOnConstruction() {
        val (eng, sr) = buildEngine()
        assertEquals(1L, sr.getSessionCount())
        assertTrue(eng.currentSessionId > 0)
    }

    @Test
    fun logInsertsEntry() {
        val (eng, _, er) = buildEngine()
        eng.log(LogLevel.INFO, "Tag", "hello", null)
        assertEquals(1L, er.getEntryCount(eng.currentSessionId))
    }

    @Test
    fun logPreservesThrowableAsStackTrace() {
        val (eng, _, er, db) = buildEngine()
        eng.log(LogLevel.ERROR, "T", "err", RuntimeException("boom"))
        val entries = db.logEntryQueries.entriesForSession(eng.currentSessionId).executeAsList()
        assertEquals(1, entries.size)
        assertTrue(entries.single().stackTrace?.contains("RuntimeException") == true)
    }

    @Test
    fun capEnforcementDropsOldestWhenLimitExceeded() {
        val maxEntries = 10
        val (eng, _, er) = buildEngine(maxEntries = maxEntries)
        repeat(maxEntries + 1) { i ->
            eng.log(LogLevel.DEBUG, "T", "msg-$i", null)
        }
        assertEquals(maxEntries.toLong(), er.getEntryCount(eng.currentSessionId))
    }

    @Test
    fun oldestSessionEvictedWhenCapReached() {
        val maxSessions = 3
        val db = createTestDatabase()
        val sr = SessionRepository(db)
        val er = LogEntryRepository(db)
        // Pre-populate sessions equal to the cap
        repeat(maxSessions) { sr.createSession(it.toLong()) }
        assertEquals(maxSessions.toLong(), sr.getSessionCount())

        // Engine should evict oldest (3 → 2), then create its own (2 → 3) — stays at cap
        val config = LogKeepConfig(isEnabled = true, maxEntriesPerSession = 1_000, maxSessions = maxSessions)
        LogKeepEngine(config, sr, er, CoroutineScope(Dispatchers.Unconfined))

        assertEquals(maxSessions.toLong(), sr.getSessionCount())
    }

    @Test
    fun markSessionCleanUpdatesFlag() {
        val (eng, _, _, db) = buildEngine()
        eng.markSessionClean()
        val session = db.sessionQueries.allSessions().executeAsList()
            .first { it.id == eng.currentSessionId }
        assertEquals(true, session.endedCleanly)
    }

    @Test
    fun markSessionActiveResetsFlag() {
        val (eng, _, _, db) = buildEngine()
        eng.markSessionClean()
        eng.markSessionActive()
        val session = db.sessionQueries.allSessions().executeAsList()
            .first { it.id == eng.currentSessionId }
        assertEquals(false, session.endedCleanly)
    }
}
