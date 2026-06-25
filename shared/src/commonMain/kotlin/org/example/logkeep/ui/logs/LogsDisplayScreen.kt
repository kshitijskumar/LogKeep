package org.example.logkeep.ui.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.logkeep.core.LogKeep
import org.example.logkeep.core.LogLevel
import org.example.logkeep.db.LogEntry
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LogsDisplayScreen(
    sessionId: Long,
    onBack: () -> Unit,
    viewModel: LogsViewModel = viewModel(
        key = sessionId.toString(),
        initializer = {
            LogsViewModel(sessionId, LogKeep.logEntryRepository!!, LogKeep.sessionRepository!!)
        }
    )
) {
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.sessionStartedAt?.let(::formatSessionTime) ?: "Logs"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("Back") }
                },
                actions = {
                    IconButton(onClick = { viewModel.deleteAllLogs() }) {
                        Text("Delete")
                    }
                    IconButton(onClick = { viewModel.openFilterSheet() }) {
                        Text(if (uiState.isFilterActive) "Filter*" else "Filter")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
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
    }

    if (uiState.isFilterSheetVisible) {
        FilterBottomSheet(
            pendingLevel = uiState.pendingLevel,
            pendingTag = uiState.pendingTag,
            onLevelSelected = { viewModel.setPendingLevel(it) },
            onTagChanged = { viewModel.setPendingTag(it) },
            onApply = { viewModel.applyFilter() },
            onDismiss = { viewModel.dismissFilterSheet() }
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    pendingLevel: LogLevel?,
    pendingTag: String,
    onLevelSelected: (LogLevel) -> Unit,
    onTagChanged: (String) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(bottom = 32.dp)
        ) {
            Text(
                "Filter by level",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            LogLevel.entries.forEach { level ->
                FilterLevelRow(
                    level = level,
                    isSelected = level == pendingLevel,
                    onClick = { onLevelSelected(level) }
                )
            }
            OutlinedTextField(
                value = pendingTag,
                onValueChange = onTagChanged,
                label = { Text("Filter by tag") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )
            Button(
                onClick = onApply,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Apply")
            }
        }
    }
}

@Composable
private fun FilterLevelRow(level: LogLevel, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color = level.panelColor(), shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(level.displayName(), modifier = Modifier.weight(1f), fontSize = 14.sp)
        if (isSelected) {
            Text("✓", fontSize = 14.sp)
        }
    }
}
