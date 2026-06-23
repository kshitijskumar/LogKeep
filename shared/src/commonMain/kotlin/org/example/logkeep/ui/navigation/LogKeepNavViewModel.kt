package org.example.logkeep.ui.navigation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class LogKeepNavViewModel : ViewModel() {
    private val _backStack = MutableStateFlow<List<Screen>>(listOf(Screen.SessionsList))
    val backStack: StateFlow<List<Screen>> = _backStack.asStateFlow()

    fun navigateTo(screen: Screen) {
        _backStack.value += screen
    }

    fun navigateBack() {
        if (_backStack.value.size > 1) {
            _backStack.value = _backStack.value.dropLast(1)
        }
    }
}
