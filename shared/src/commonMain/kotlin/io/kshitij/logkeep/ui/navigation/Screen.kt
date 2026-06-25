package io.kshitij.logkeep.ui.navigation

internal sealed class Screen {
    data object SessionsList : Screen()
    data class LogsDisplay(val sessionId: Long) : Screen()
}
