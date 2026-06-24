package org.example.logkeep.ui.logs

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.example.logkeep.core.LogLevel
import org.example.logkeep.core.repository.LogEntryRepository
import org.example.logkeep.core.repository.SessionRepository
import org.example.logkeep.createTestDatabase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LogsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildVm(
        sessionId: Long,
        er: LogEntryRepository,
        sr: SessionRepository
    ) = LogsViewModel(sessionId, er, sr)

    private fun setup(): Triple<SessionRepository, LogEntryRepository, Long> {
        val db = createTestDatabase()
        val sr = SessionRepository(db)
        val er = LogEntryRepository(db)
        val sessionId = sr.createSession(1_000L)
        return Triple(sr, er, sessionId)
    }

    // --- filter sheet visibility ---

    @Test
    fun filterSheetIsInitiallyHidden() {
        val (sr, er, sessionId) = setup()
        val vm = buildVm(sessionId, er, sr)
        assertFalse(vm.uiState.value.isFilterSheetVisible)
    }

    @Test
    fun openFilterSheetMakesSheetVisible() {
        val (sr, er, sessionId) = setup()
        val vm = buildVm(sessionId, er, sr)
        vm.openFilterSheet()
        assertTrue(vm.uiState.value.isFilterSheetVisible)
    }

    @Test
    fun dismissFilterSheetHidesSheet() {
        val (sr, er, sessionId) = setup()
        val vm = buildVm(sessionId, er, sr)
        vm.openFilterSheet()
        vm.dismissFilterSheet()
        assertFalse(vm.uiState.value.isFilterSheetVisible)
    }

    @Test
    fun dismissFilterSheetIsIdempotentWhenAlreadyHidden() {
        val (sr, er, sessionId) = setup()
        val vm = buildVm(sessionId, er, sr)
        vm.dismissFilterSheet()
        assertFalse(vm.uiState.value.isFilterSheetVisible)
    }

    // --- level filter ---

    @Test
    fun initialSelectedLevelIsNull() {
        val (sr, er, sessionId) = setup()
        val vm = buildVm(sessionId, er, sr)
        assertNull(vm.uiState.value.selectedLevel)
    }

    @Test
    fun setLevelFilterUpdatesSelectedLevel() {
        val (sr, er, sessionId) = setup()
        val vm = buildVm(sessionId, er, sr)
        vm.setLevelFilter(LogLevel.INFO)
        assertEquals(LogLevel.INFO, vm.uiState.value.selectedLevel)
    }

    @Test
    fun setLevelFilterHidesFilterSheet() {
        val (sr, er, sessionId) = setup()
        val vm = buildVm(sessionId, er, sr)
        vm.openFilterSheet()
        vm.setLevelFilter(LogLevel.DEBUG)
        assertFalse(vm.uiState.value.isFilterSheetVisible)
    }

    @Test
    fun setLevelFilterWithSameLevelClearsSelection() {
        val (sr, er, sessionId) = setup()
        val vm = buildVm(sessionId, er, sr)
        vm.setLevelFilter(LogLevel.WARN)
        vm.setLevelFilter(LogLevel.WARN)
        assertNull(vm.uiState.value.selectedLevel)
    }

    @Test
    fun setLevelFilterWithDifferentLevelReplacesSelection() {
        val (sr, er, sessionId) = setup()
        val vm = buildVm(sessionId, er, sr)
        vm.setLevelFilter(LogLevel.DEBUG)
        vm.setLevelFilter(LogLevel.ERROR)
        assertEquals(LogLevel.ERROR, vm.uiState.value.selectedLevel)
    }

    @Test
    fun setLevelFilterNullClearsSelection() {
        val (sr, er, sessionId) = setup()
        val vm = buildVm(sessionId, er, sr)
        vm.setLevelFilter(LogLevel.VERBOSE)
        vm.setLevelFilter(null)
        assertNull(vm.uiState.value.selectedLevel)
    }

    @Test
    fun setLevelFilterNullWhenAlreadyClearedKeepsNull() {
        val (sr, er, sessionId) = setup()
        val vm = buildVm(sessionId, er, sr)
        vm.setLevelFilter(null)
        assertNull(vm.uiState.value.selectedLevel)
    }
}
