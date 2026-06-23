package org.example.logkeep.ui.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.logkeep.core.LogLevel
import org.example.logkeep.db.LogEntry
import kotlin.time.ExperimentalTime

@Composable
internal fun LogsDisplayScreen(sessionId: Long) {
    val viewModel: LogsViewModel = viewModel(
        key = sessionId.toString(),
        initializer = { LogsViewModel(sessionId) }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val logs = uiState.logs
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        var prevSize = 0
        var capturedIsAtTop = true

        snapshotFlow {
            Pair(
                listState.firstVisibleItemIndex == 0,
                uiState.logs?.size ?: 0
            )
        }.collectLatest { (atTop, size) ->
            val hadNewItems = size > prevSize
            if (hadNewItems && capturedIsAtTop) {
                listState.animateScrollToItem(0)
            }
            capturedIsAtTop = atTop
            prevSize = size
        }
    }

    when {
        logs == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        logs.isEmpty() -> {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
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
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                items(items = logs, key = { it.id }) { entry ->
                    LogItem(entry = entry)
                    HorizontalDivider(
                        modifier = Modifier.padding(8.dp),
                        color = DividerDefaults.color.copy(alpha = 0.5f)
                    )
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
    SelectionContainer {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            Column(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight()
                    .background(entry.level.panelColor())
                    .padding(8.dp)
            ) {
                Text(entry.level.displayName(), fontSize = 12.sp)
                Text(entry.timestamp.toString(), fontSize = 12.sp)
                Text(entry.tag, fontSize = 12.sp)
            }
            Column(
                modifier = Modifier
                    .weight(0.8f)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(entry.message, fontSize = 12.sp)
                entry.stackTrace?.let { Text(it) }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun SessionStartFooter(sessionStartedAt: Long) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
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

private fun LogLevel.displayName(): String = when (this) {
    LogLevel.VERBOSE -> "Verbose"
    LogLevel.DEBUG -> "Debug"
    LogLevel.INFO -> "Info"
    LogLevel.WARN -> "Warn"
    LogLevel.ERROR -> "Error"
}

private fun LogLevel.panelColor(): Color = when (this) {
    LogLevel.DEBUG -> Color(0x80ADD8E6)
    LogLevel.VERBOSE -> Color(0x80CAD3D9)
    LogLevel.ERROR -> Color(0x80FFB3B3)
    LogLevel.INFO -> Color(0x80FFFF99)
    LogLevel.WARN -> Color(0x80FFCC99)
}
