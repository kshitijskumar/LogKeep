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

    // --- pending level ---

    @Test
    fun initialPendingLevelIsNull() {
        val (sr, er, sessionId) = setup()
        val vm = buildVm(sessionId, er, sr)
        assertNull(vm.uiState.value.pendingLevel)
    }

    @Test
    fun setPendingLevelUpdatesPendingLevel() {
        val (sr, er, sessionId) = setup()
        val vm = buildVm(sessionId, er, sr)
        vm.setPendingLevel(LogLevel.INFO)
        assertEquals(LogLevel.INFO, vm.uiState.value.pendingLevel)
    }

    @Test
    fun setPendingLevelWithSameLevelClearsPending() {
        val (sr, er, sessionId) = setup()
        val vm = buildVm(sessionId, er, sr)
        vm.setPendingLevel(LogLevel.WARN)
        vm.setPendingLevel(LogLevel.WARN)
        assertNull(vm.uiState.value.pendingLevel)
    }

    @Test
    fun setPendingLevelWithDifferentLevelReplacesPending() {
        val (sr, er, sessionId) = setup()
        val vm = buildVm(sessionId, er, sr)
        vm.setPendingLevel(LogLevel.DEBUG)
        vm.setPendingLevel(LogLevel.ERROR)
        assertEquals(LogLevel.ERROR, vm.uiState.value.pendingLevel)
    }

    @Test
    fun setPendingLevelDoesNotChangeSelectedLevel() {
        val (sr, er, sessionId) = setup()
        val vm = buildVm(sessionId, er, sr)
        vm.setPendingLevel(LogLevel.INFO)
        assertNull(vm.uiState.value.selectedLevel)
    }

    // --- pending tag ---

    @Test
    fun initialPendingTagIsEmpty() {
        val (sr, er, sessionId) = setup()
        val vm = buildVm(sessionId, er, sr)
        assertEquals("", vm.uiState.value.pendingTag)
    }

    @Test
    fun setPendingTagUpdatesPendingTag() {
        val (sr, er, sessionId) = setup()
        val vm = buildVm(sessionId, er, sr)
        vm.setPendingTag("auth")
        assertEquals("auth", vm.uiState.value.pendingTag)
    }

    @Test
    fun setPendingTagDoesNotChangeSelectedTag() {
        val (sr, er, sessionId) = setup()
        val vm = buildVm(sessionId, er, sr)
        vm.setPendingTag("auth")
        assertEquals("", vm.uiState.value.selectedTag)
    }

    // --- open filter sheet initializes pending ---

    @Test
    fun openFilterSheetInitializesPendingFromApplied() {
        val (sr, er, sessionId) = setup()
        val vm = buildVm(sessionId, er, sr)
        vm.setPendingLevel(LogLevel.ERROR)
        vm.setPendingTag("net")
        vm.applyFilter()
        vm.dismissFilterSheet()

        vm.openFilterSheet()

        assertEquals(LogLevel.ERROR, vm.uiState.value.pendingLevel)
        assertEquals("net", vm.uiState.value.pendingTag)
    }

    // --- apply filter ---

    @Test
    fun applyFilterCommitsPendingLevelAndTag() {
        val (sr, er, sessionId) = setup()
        val vm = buildVm(sessionId, er, sr)
        vm.setPendingLevel(LogLevel.INFO)
        vm.setPendingTag("auth")
        vm.applyFilter()
        assertEquals(LogLevel.INFO, vm.uiState.value.selectedLevel)
        assertEquals("auth", vm.uiState.value.selectedTag)
    }

    @Test
    fun applyFilterClosesSheet() {
        val (sr, er, sessionId) = setup()
        val vm = buildVm(sessionId, er, sr)
        vm.openFilterSheet()
        vm.applyFilter()
        assertFalse(vm.uiState.value.isFilterSheetVisible)
    }

    // --- dismiss reverts ---

    @Test
    fun dismissFilterSheetDoesNotCommitPendingChanges() {
        val (sr, er, sessionId) = setup()
        val vm = buildVm(sessionId, er, sr)
        vm.openFilterSheet()
        vm.setPendingLevel(LogLevel.DEBUG)
        vm.setPendingTag("net")
        vm.dismissFilterSheet()
        assertNull(vm.uiState.value.selectedLevel)
        assertEquals("", vm.uiState.value.selectedTag)
    }
}
