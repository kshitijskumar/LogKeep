package io.kshitij.logkeep.ui.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class LogKeepNavViewModelTest {

    @Test
    fun initialStateIsSessionsList() {
        val vm = LogKeepNavViewModel()
        assertEquals(listOf(Screen.SessionsList), vm.backStack.value)
    }

    @Test
    fun navigateToAddsScreenToStack() {
        val vm = LogKeepNavViewModel()
        vm.navigateTo(Screen.LogsDisplay(sessionId = 42L))
        assertEquals(
            listOf(Screen.SessionsList, Screen.LogsDisplay(42L)),
            vm.backStack.value
        )
    }

    @Test
    fun currentScreenIsLastInStack() {
        val vm = LogKeepNavViewModel()
        vm.navigateTo(Screen.LogsDisplay(sessionId = 1L))
        assertEquals(Screen.LogsDisplay(1L), vm.backStack.value.last())
    }

    @Test
    fun navigateBackPopsLastScreen() {
        val vm = LogKeepNavViewModel()
        vm.navigateTo(Screen.LogsDisplay(sessionId = 1L))
        vm.navigateBack()
        assertEquals(listOf(Screen.SessionsList), vm.backStack.value)
    }

    @Test
    fun navigateBackOnRootDoesNothing() {
        val vm = LogKeepNavViewModel()
        vm.navigateBack()
        assertEquals(listOf(Screen.SessionsList), vm.backStack.value)
    }

    @Test
    fun multipleNavigationsStackCorrectly() {
        val vm = LogKeepNavViewModel()
        vm.navigateTo(Screen.LogsDisplay(sessionId = 1L))
        vm.navigateTo(Screen.LogsDisplay(sessionId = 2L))
        assertEquals(
            listOf(Screen.SessionsList, Screen.LogsDisplay(1L), Screen.LogsDisplay(2L)),
            vm.backStack.value
        )
    }

    @Test
    fun navigateBackRestoresPreviousScreen() {
        val vm = LogKeepNavViewModel()
        vm.navigateTo(Screen.LogsDisplay(sessionId = 1L))
        vm.navigateTo(Screen.LogsDisplay(sessionId = 2L))
        vm.navigateBack()
        assertEquals(Screen.LogsDisplay(1L), vm.backStack.value.last())
    }
}
