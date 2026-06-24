package org.example.logkeep.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.logkeep.ui.logs.LogsDisplayScreen
import org.example.logkeep.ui.navigation.LogKeepNavViewModel
import org.example.logkeep.ui.navigation.Screen
import org.example.logkeep.ui.sessions.SessionsScreen

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
