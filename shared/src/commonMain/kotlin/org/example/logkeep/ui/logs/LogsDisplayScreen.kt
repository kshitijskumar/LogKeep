package org.example.logkeep.ui.logs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.logkeep.core.LogLevel
import org.example.logkeep.db.LogEntry
import kotlin.time.ExperimentalTime

@Composable
internal fun LogsDisplayScreen(sessionId: Long) {
    val viewModel: LogsViewModel = viewModel(
        initializer = { LogsViewModel(sessionId) }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val logs = uiState.logs

    when {
        logs == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        logs.isEmpty() -> {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No logs yet")
                    }
                }
                item(key = "session_start_footer") {
                    uiState.sessionStartedAt?.let { SessionStartFooter(it) }
                }
            }
        }
        else -> {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = logs, key = { it.id }) { entry ->
                    LogItem(entry = entry)
                    HorizontalDivider()
                }
                item(key = "session_start_footer") {
                    uiState.sessionStartedAt?.let { SessionStartFooter(it) }
                }
            }
        }
    }
}

@Composable
private fun LogItem(entry: LogEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text("${entry.timestamp}  ${entry.level.toChar()}  ${entry.tag}  ${entry.message}")
        entry.stackTrace?.let { Text(it) }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun SessionStartFooter(sessionStartedAt: Long) {
    Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Session started: ${formatSessionTime(sessionStartedAt)}")
    }
}

@OptIn(ExperimentalTime::class)
private fun formatSessionTime(epochMillis: Long): String {
    val dt = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val month = dt.monthNumber.toString().padStart(2, '0')
    val day = dt.dayOfMonth.toString().padStart(2, '0')
    val hour = dt.hour.toString().padStart(2, '0')
    val minute = dt.minute.toString().padStart(2, '0')
    val second = dt.second.toString().padStart(2, '0')
    return "${dt.year}-$month-$day $hour:$minute:$second"
}

private fun LogLevel.toChar(): Char = when (this) {
    LogLevel.VERBOSE -> 'V'
    LogLevel.DEBUG -> 'D'
    LogLevel.INFO -> 'I'
    LogLevel.WARN -> 'W'
    LogLevel.ERROR -> 'E'
}
