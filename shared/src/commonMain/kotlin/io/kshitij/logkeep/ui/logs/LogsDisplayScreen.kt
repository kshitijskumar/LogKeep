package io.kshitij.logkeep.ui.logs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import io.kshitij.logkeep.core.LogKeep
import io.kshitij.logkeep.core.LogLevel
import io.kshitij.logkeep.db.LogEntry
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
    var isOverflowExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(listState) {
        var prevSize = 0
        var capturedIsAtTop = true
        snapshotFlow {
            Pair(listState.firstVisibleItemIndex == 0, uiState.logs?.size ?: 0)
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.sessionStartedAt?.let(::formatSessionTitle) ?: "Logs",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text(
                            text = "←",
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { isOverflowExpanded = true }) {
                            Text(
                                text = "⋮",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = isOverflowExpanded,
                            onDismissRequest = { isOverflowExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Delete session",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    viewModel.deleteAllLogs()
                                    isOverflowExpanded = false
                                }
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.openFilterSheet() }) {
                        Text(
                            text = if (uiState.isFilterActive) "Filter*" else "Filter",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                logs == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                logs.isEmpty() && uiState.isFilterActive -> {
                    EmptyFilteredState(
                        modifier = Modifier.fillMaxSize(),
                        onClearFilter = { viewModel.clearFilter() }
                    )
                }
                logs.isEmpty() -> {
                    EmptyNoLogsState(
                        modifier = Modifier.fillMaxSize(),
                        sessionStartedAt = uiState.sessionStartedAt
                    )
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        items(items = logs, key = { it.id }) { entry ->
                            LogItem(entry = entry)
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
            onReset = { viewModel.resetPendingFilter() },
            onDismiss = { viewModel.dismissFilterSheet() }
        )
    }
}

@Composable
private fun LogItem(entry: LogEntry) {
    val style = entry.level.uiStyle()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, style.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(style.stripeColor)
            )
            SelectionContainer {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LevelBadge(level = entry.level, style = style)
                        Text(
                            text = formatLogTimestamp(entry.timestamp),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = entry.tag,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                    Text(
                        text = entry.message,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    entry.stackTrace?.let { stackTrace ->
                        Text(
                            text = stackTrace,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.error,
                            lineHeight = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelBadge(level: LogLevel, style: LogLevelUiStyle) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(style.badgeBg)
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            text = level.displayName(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = style.badgeText
        )
    }
}

@Composable
private fun EmptyFilteredState(modifier: Modifier = Modifier, onClearFilter: () -> Unit) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⊘",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No matching logs",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Try adjusting your filters",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(onClick = onClearFilter)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Clear filters",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun EmptyNoLogsState(modifier: Modifier = Modifier, sessionStartedAt: Long?) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "≡",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No logs yet",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        sessionStartedAt?.let {
            Text(
                text = "Session started at ${formatSessionTitle(it)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun SessionStartFooter(sessionStartedAt: Long) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Session started at ${formatSessionTitle(sessionStartedAt)}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FilterBottomSheet(
    pendingLevel: LogLevel?,
    pendingTag: String,
    onLevelSelected: (LogLevel) -> Unit,
    onTagChanged: (String) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter logs",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onReset) {
                    Text(
                        text = "Reset",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Text(
                text = "LOG LEVEL",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.7.sp
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LogLevel.entries.forEach { level ->
                    LevelFilterChip(
                        level = level,
                        selected = level == pendingLevel,
                        onClick = { onLevelSelected(level) }
                    )
                }
            }

            Text(
                text = "TAG",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.7.sp
            )
            OutlinedTextField(
                value = pendingTag,
                onValueChange = onTagChanged,
                placeholder = {
                    Text(
                        text = "Filter by tag",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 20.dp),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Apply",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun LevelFilterChip(level: LogLevel, selected: Boolean, onClick: () -> Unit) {
    val style = level.uiStyle()
    val bg = if (selected) style.badgeBg else Color.Transparent
    val borderColor = if (selected) style.stripeColor else MaterialTheme.colorScheme.outline
    val borderWidth = if (selected) 1.5.dp else 1.dp
    val textColor = if (selected) style.badgeText else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(borderWidth, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = level.displayName(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

private data class LogLevelUiStyle(
    val stripeColor: Color,
    val badgeBg: Color,
    val badgeText: Color,
    val cardBorder: Color
)

private fun LogLevel.uiStyle(): LogLevelUiStyle = when (this) {
    LogLevel.VERBOSE -> LogLevelUiStyle(
        stripeColor = Color(0xFFB4B2A9),
        badgeBg = Color(0xFFF1EFE8),
        badgeText = Color(0xFF444441),
        cardBorder = Color(0xFFE5E3F0)
    )
    LogLevel.DEBUG -> LogLevelUiStyle(
        stripeColor = Color(0xFF5DCAA5),
        badgeBg = Color(0xFFE1F5EE),
        badgeText = Color(0xFF085041),
        cardBorder = Color(0xFFB9E4D5)
    )
    LogLevel.INFO -> LogLevelUiStyle(
        stripeColor = Color(0xFF378ADD),
        badgeBg = Color(0xFFE6F1FB),
        badgeText = Color(0xFF0C447C),
        cardBorder = Color(0xFFC5DCF2)
    )
    LogLevel.WARN -> LogLevelUiStyle(
        stripeColor = Color(0xFFEF9F27),
        badgeBg = Color(0xFFFAEEDA),
        badgeText = Color(0xFF633806),
        cardBorder = Color(0xFFF0D5A8)
    )
    LogLevel.ERROR -> LogLevelUiStyle(
        stripeColor = Color(0xFFE24B4A),
        badgeBg = Color(0xFFFCEBEB),
        badgeText = Color(0xFF791F1F),
        cardBorder = Color(0xFFF7C1C1)
    )
}

private fun LogLevel.displayName(): String = when (this) {
    LogLevel.VERBOSE -> "Verbose"
    LogLevel.DEBUG -> "Debug"
    LogLevel.INFO -> "Info"
    LogLevel.WARN -> "Warn"
    LogLevel.ERROR -> "Error"
}

@OptIn(ExperimentalTime::class)
private fun formatSessionTitle(epochMillis: Long): String {
    val dt = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val monthAbbr = dt.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    val h = dt.hour.toString().padStart(2, '0')
    val m = dt.minute.toString().padStart(2, '0')
    val s = dt.second.toString().padStart(2, '0')
    return "${monthAbbr} ${dt.dayOfMonth} · $h:$m:$s"
}

@OptIn(ExperimentalTime::class)
private fun formatLogTimestamp(epochMillis: Long): String {
    val dt = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val h = dt.hour.toString().padStart(2, '0')
    val m = dt.minute.toString().padStart(2, '0')
    val s = dt.second.toString().padStart(2, '0')
    return "$h:$m:$s"
}
