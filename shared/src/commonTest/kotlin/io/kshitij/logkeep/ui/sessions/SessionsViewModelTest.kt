package io.kshitij.logkeep.ui.sessions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import io.kshitij.logkeep.core.repository.SessionRepository
import io.kshitij.logkeep.createTestDatabase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class SessionsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialUiStateHasNullSessions() {
        val sr = SessionRepository(createTestDatabase())
        val vm = SessionsViewModel(sr) { }
        // SharingStarted.WhileSubscribed — no collector yet, so stays at initialValue
        assertNull(vm.uiState.value.sessions)
    }

    @Test
    fun onSessionClickedInvokesDelegateWithCorrectId() {
        val sr = SessionRepository(createTestDatabase())
        var receivedId: Long? = null
        val vm = SessionsViewModel(sr) { id -> receivedId = id }

        vm.onSessionClicked(42L)

        assertEquals(42L, receivedId)
    }

    @Test
    fun onSessionClickedForwardsEachIdInOrder() {
        val sr = SessionRepository(createTestDatabase())
        val received = mutableListOf<Long>()
        val vm = SessionsViewModel(sr) { id -> received.add(id) }

        vm.onSessionClicked(1L)
        vm.onSessionClicked(5L)
        vm.onSessionClicked(3L)

        assertEquals(listOf(1L, 5L, 3L), received)
    }

    @Test
    fun onSessionClickedDelegateIsNotCalledBeforeInteraction() {
        val sr = SessionRepository(createTestDatabase())
        var invoked = false
        SessionsViewModel(sr) { invoked = true }

        assertEquals(false, invoked)
    }
}
