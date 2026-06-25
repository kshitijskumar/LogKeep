package io.kshitij.logkeep.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.kshitij.logkeep.ui.logs.LogsDisplayScreen
import io.kshitij.logkeep.ui.navigation.LogKeepNavViewModel
import io.kshitij.logkeep.ui.navigation.Screen
import io.kshitij.logkeep.ui.sessions.SessionsScreen

@Composable
internal fun LogKeepNavigationHost(navViewModel: LogKeepNavViewModel) {
    val backStack by navViewModel.backStack.collectAsStateWithLifecycle()
    val currentScreen = backStack.last()

    BackHandler(enabled = backStack.size > 1) { navViewModel.navigateBack() }

    when (val screen = currentScreen) {
        is Screen.SessionsList -> SessionsScreen(
            onSessionClicked = { sessionId ->
                navViewModel.navigateTo(Screen.LogsDisplay(sessionId))
            }
        )
        is Screen.LogsDisplay -> LogsDisplayScreen(
            sessionId = screen.sessionId,
            onBack = { navViewModel.navigateBack() }
        )
    }
}
