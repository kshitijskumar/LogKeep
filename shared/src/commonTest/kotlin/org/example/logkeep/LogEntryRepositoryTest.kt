package org.example.logkeep

import org.example.logkeep.core.LogLevel
import org.example.logkeep.core.repository.LogEntryRepository
import org.example.logkeep.core.repository.SessionRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class LogEntryRepositoryTest {

    private fun setup(): Pair<SessionRepository, LogEntryRepository> {
        val db = createTestDatabase()
        return SessionRepository(db) to LogEntryRepository(db)
    }

    @Test
    fun insertEntryIncrementsCount() {
        val (sr, er) = setup()
        val sid = sr.createSession(1_000L)
        assertEquals(0L, er.getEntryCount(sid))
        er.insertEntry(sid, 1_001L, LogLevel.INFO, "Tag", "msg", null)
        assertEquals(1L, er.getEntryCount(sid))
    }

    @Test
    fun insertEntryIncrementsSessionEntryCount() {
        val db = createTestDatabase()
        val sr = SessionRepository(db)
        val er = LogEntryRepository(db)
        val sid = sr.createSession(1_000L)
        er.insertEntry(sid, 1_001L, LogLevel.DEBUG, "T", "m", null)
        val session = db.sessionQueries.allSessions().executeAsList().first { it.id == sid }
        assertEquals(1L, session.entryCount)
    }

    @Test
    fun deleteOldestEntryRemovesEarliestAndDecrementsCount() {
        val db = createTestDatabase()
        val sr = SessionRepository(db)
        val er = LogEntryRepository(db)
        val sid = sr.createSession(1_000L)
        er.insertEntry(sid, 1_001L, LogLevel.VERBOSE, "T", "first", null)
        er.insertEntry(sid, 1_002L, LogLevel.INFO, "T", "second", null)

        er.deleteOldestEntry(sid)

        assertEquals(1L, er.getEntryCount(sid))
        val remaining = db.logEntryQueries.entriesForSession(sid).executeAsList()
        assertEquals("second", remaining.single().message)

        val session = db.sessionQueries.allSessions().executeAsList().first { it.id == sid }
        assertEquals(1L, session.entryCount)
    }

    @Test
    fun deleteOldestEntryNoOpOnEmptySession() {
        val (sr, er) = setup()
        val sid = sr.createSession(1_000L)
        er.deleteOldestEntry(sid)
        assertEquals(0L, er.getEntryCount(sid))
    }

    @Test
    fun stackTraceStoredAndRetrieved() {
        val db = createTestDatabase()
        val sr = SessionRepository(db)
        val er = LogEntryRepository(db)
        val sid = sr.createSession(1_000L)
        er.insertEntry(sid, 1_001L, LogLevel.ERROR, "T", "err", "some\nstack\ntrace")
        val entry = db.logEntryQueries.entriesForSession(sid).executeAsList().single()
        assertEquals("some\nstack\ntrace", entry.stackTrace)
    }
}
