package org.example.logkeep

import org.example.logkeep.core.repository.SessionRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionRepositoryTest {

    @Test
    fun createSessionReturnsId() {
        val db = createTestDatabase()
        val repo = SessionRepository(db)
        val id = repo.createSession(startedAt = 1_000L)
        assertTrue(id > 0)
    }

    @Test
    fun sessionCountIncreasesOnCreate() {
        val db = createTestDatabase()
        val repo = SessionRepository(db)
        assertEquals(0L, repo.getSessionCount())
        repo.createSession(1_000L)
        assertEquals(1L, repo.getSessionCount())
        repo.createSession(2_000L)
        assertEquals(2L, repo.getSessionCount())
    }

    @Test
    fun markSessionEndedSetsCleanFlag() {
        val db = createTestDatabase()
        val repo = SessionRepository(db)
        val id = repo.createSession(1_000L)

        val before = db.sessionQueries.allSessions().executeAsList().first { it.id == id }
        assertEquals(false, before.endedCleanly)

        repo.markSessionEnded(id, cleanly = true)

        val after = db.sessionQueries.allSessions().executeAsList().first { it.id == id }
        assertEquals(true, after.endedCleanly)
        assertNotNull(after.endedAt)
    }

    @Test
    fun markSessionActiveResetsCleanFlag() {
        val db = createTestDatabase()
        val repo = SessionRepository(db)
        val id = repo.createSession(1_000L)
        repo.markSessionEnded(id, cleanly = true)
        repo.markSessionActive(id)

        val session = db.sessionQueries.allSessions().executeAsList().first { it.id == id }
        assertEquals(false, session.endedCleanly)
        assertNull(session.endedAt)
    }

    @Test
    fun oldestSessionReturnsEarliestByStartedAt() {
        val db = createTestDatabase()
        val repo = SessionRepository(db)
        repo.createSession(3_000L)
        val oldestId = repo.createSession(1_000L)
        repo.createSession(2_000L)

        assertEquals(oldestId, repo.getOldestSessionId())
    }

    @Test
    fun deleteSessionRemovesIt() {
        val db = createTestDatabase()
        val repo = SessionRepository(db)
        val id = repo.createSession(1_000L)
        assertEquals(1L, repo.getSessionCount())
        repo.deleteSession(id)
        assertEquals(0L, repo.getSessionCount())
    }

    @Test
    fun getOldestSessionIdNullWhenEmpty() {
        val repo = SessionRepository(createTestDatabase())
        assertNull(repo.getOldestSessionId())
    }
}
