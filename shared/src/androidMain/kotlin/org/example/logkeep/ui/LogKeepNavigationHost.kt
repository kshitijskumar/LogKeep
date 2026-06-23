package org.example.logkeep.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.example.logkeep.ui.logs.LogsDisplayScreen
import org.example.logkeep.ui.navigation.LogKeepNavViewModel
import org.example.logkeep.ui.navigation.Screen
import org.example.logkeep.ui.sessions.SessionsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LogKeepNavigationHost(navViewModel: LogKeepNavViewModel) {
    val backStack by navViewModel.backStack.collectAsStateWithLifecycle()
    val currentScreen = backStack.last()

    BackHandler(enabled = backStack.size > 1) { navViewModel.navigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle(currentScreen)) },
                navigationIcon = {
                    if (backStack.size > 1) {
                        IconButton(onClick = { navViewModel.navigateBack() }) {
                            Text("Back")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val screen = currentScreen) {
                is Screen.SessionsList -> SessionsScreen(
                    onSessionClicked = { sessionId ->
                        navViewModel.navigateTo(Screen.LogsDisplay(sessionId))
                    }
                )
                is Screen.LogsDisplay -> LogsDisplayScreen(sessionId = screen.sessionId)
            }
        }
    }
}

private fun screenTitle(screen: Screen) = when (screen) {
    is Screen.SessionsList -> "Log Keep"
    is Screen.LogsDisplay -> "Logs"
}
