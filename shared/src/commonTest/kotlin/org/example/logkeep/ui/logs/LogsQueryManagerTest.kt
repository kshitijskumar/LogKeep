package org.example.logkeep.ui.logs

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.example.logkeep.core.LogLevel
import org.example.logkeep.core.repository.LogEntryRepository
import org.example.logkeep.core.repository.SessionRepository
import org.example.logkeep.createTestDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class LogsQueryManagerTest {

    private fun setup(): Triple<SessionRepository, LogEntryRepository, Long> {
        val db = createTestDatabase()
        val sr = SessionRepository(db)
        val er = LogEntryRepository(db)
        val sessionId = sr.createSession(1_000L)
        return Triple(sr, er, sessionId)
    }

    @Test
    fun initialFilterIsEmpty() {
        val (_, er, sessionId) = setup()
        val manager = LogsQueryManager(sessionId, er)
        assertEquals(LogsFilter(), manager.filterState.value)
        assertNull(manager.filterState.value.level)
    }

    @Test
    fun setLevelFilterUpdatesFilterState() {
        val (_, er, sessionId) = setup()
        val manager = LogsQueryManager(sessionId, er)
        manager.setLevelFilter(LogLevel.INFO)
        assertEquals(LogLevel.INFO, manager.filterState.value.level)
    }

    @Test
    fun setLevelFilterToNullClearsLevel() {
        val (_, er, sessionId) = setup()
        val manager = LogsQueryManager(sessionId, er)
        manager.setLevelFilter(LogLevel.WARN)
        manager.setLevelFilter(null)
        assertNull(manager.filterState.value.level)
    }

    @Test
    fun setLevelFilterReplacesExistingLevel() {
        val (_, er, sessionId) = setup()
        val manager = LogsQueryManager(sessionId, er)
        manager.setLevelFilter(LogLevel.DEBUG)
        manager.setLevelFilter(LogLevel.ERROR)
        assertEquals(LogLevel.ERROR, manager.filterState.value.level)
    }

    @Test
    fun logsFlowEmitsAllEntriesWithNoFilterSet() = runTest(UnconfinedTestDispatcher()) {
        val (_, er, sessionId) = setup()
        er.insertEntry(sessionId, 1_001L, LogLevel.INFO, "Tag", "info msg", null)
        er.insertEntry(sessionId, 1_002L, LogLevel.DEBUG, "Tag", "debug msg", null)
        er.insertEntry(sessionId, 1_003L, LogLevel.ERROR, "Tag", "error msg", null)
        val manager = LogsQueryManager(sessionId, er)

        val logs = manager.logsFlow.first()

        assertEquals(3, logs.size)
    }

    @Test
    fun logsFlowFiltersToMatchingLevelOnly() = runTest(UnconfinedTestDispatcher()) {
        val (_, er, sessionId) = setup()
        er.insertEntry(sessionId, 1_001L, LogLevel.INFO, "Tag", "info msg", null)
        er.insertEntry(sessionId, 1_002L, LogLevel.DEBUG, "Tag", "debug msg", null)
        er.insertEntry(sessionId, 1_003L, LogLevel.ERROR, "Tag", "error msg", null)
        val manager = LogsQueryManager(sessionId, er)
        manager.setLevelFilter(LogLevel.INFO)

        val logs = manager.logsFlow.first()

        assertEquals(1, logs.size)
        assertEquals(LogLevel.INFO, logs.single().level)
    }

    @Test
    fun logsFlowEmitsEmptyListWhenNoEntriesMatchFilter() = runTest(UnconfinedTestDispatcher()) {
        val (_, er, sessionId) = setup()
        er.insertEntry(sessionId, 1_001L, LogLevel.DEBUG, "Tag", "debug msg", null)
        val manager = LogsQueryManager(sessionId, er)
        manager.setLevelFilter(LogLevel.ERROR)

        val logs = manager.logsFlow.first()

        assertEquals(0, logs.size)
    }

    @Test
    fun logsFlowEmitsAllEntriesAfterFilterCleared() = runTest(UnconfinedTestDispatcher()) {
        val (_, er, sessionId) = setup()
        er.insertEntry(sessionId, 1_001L, LogLevel.INFO, "Tag", "info msg", null)
        er.insertEntry(sessionId, 1_002L, LogLevel.DEBUG, "Tag", "debug msg", null)
        val manager = LogsQueryManager(sessionId, er)
        manager.setLevelFilter(LogLevel.INFO)
        manager.setLevelFilter(null)

        val logs = manager.logsFlow.first()

        assertEquals(2, logs.size)
    }

    @Test
    fun logsFlowEmitsEmptyListForSessionWithNoEntries() = runTest(UnconfinedTestDispatcher()) {
        val (_, er, sessionId) = setup()
        val manager = LogsQueryManager(sessionId, er)

        val logs = manager.logsFlow.first()

        assertEquals(0, logs.size)
    }
}
