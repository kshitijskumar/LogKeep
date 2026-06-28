package io.kshitij.logkeep.ui.sessions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import io.kshitij.logkeep.core.LogKeep
import io.kshitij.logkeep.db.Session
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SessionsScreen(
    onSessionClicked: (Long) -> Unit = {},
    viewModel: SessionsViewModel = viewModel(
        initializer = {
            SessionsViewModel(LogKeep.sessionRepository!!, sessionClickedDelegate = onSessionClicked)
        }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sessions = uiState.sessions

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Log Keep",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                sessions == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                sessions.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No sessions yet",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    val grouped = remember(sessions) {
                        sessions.groupBy { sessionDate(it.startedAt) }
                    }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            SessionsStatsStrip(sessions = sessions)
                        }
                        grouped.forEach { (date, dateSessions) ->
                            item(key = "header-$date") {
                                DateGroupHeader(date = date)
                            }
                            items(items = dateSessions, key = { it.id }) { session ->
                                SessionCard(
                                    session = session,
                                    onClick = { viewModel.onSessionClicked(session.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionsStatsStrip(sessions: List<Session>) {
    val totalLogs = sessions.sumOf { it.entryCount }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(value = sessions.size.toString(), label = "sessions")
        Box(
            modifier = Modifier
                .width(0.5.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f))
        )
        StatItem(value = totalLogs.toString(), label = "total logs")
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun DateGroupHeader(date: LocalDate) {
    val today = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
    val label = when (date) {
        today -> "Today - ${monthAbbr(date.monthNumber)} ${date.dayOfMonth}"
        today.minus(DatePeriod(days = 1)) -> "Yesterday - ${monthAbbr(date.monthNumber)} ${date.dayOfMonth}"
        else -> "${monthAbbr(date.monthNumber)} ${date.dayOfMonth}, ${date.year}"
    }
    Text(
        text = label.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.7.sp,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp, bottom = 6.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionCard(session: Session, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatTimeOnly(session.startedAt),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = buildSessionMeta(session),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "›",
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun sessionDate(epochMillis: Long): LocalDate =
    Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

@OptIn(ExperimentalTime::class)
private fun formatTimeOnly(epochMillis: Long): String {
    val dt = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val h = dt.hour.toString().padStart(2, '0')
    val m = dt.minute.toString().padStart(2, '0')
    val s = dt.second.toString().padStart(2, '0')
    return "$h:$m:$s"
}

private fun buildSessionMeta(session: Session): String {
    val logsPart = "${session.entryCount} logs"
    val durationPart = session.endedAt?.let { endedAt ->
        val diffSeconds = (endedAt - session.startedAt) / 1000
        when {
            diffSeconds < 60 -> "${diffSeconds}s"
            else -> "${diffSeconds / 60}m ${diffSeconds % 60}s"
        }
    }
    return if (durationPart != null) "$logsPart - $durationPart" else logsPart
}

private fun monthAbbr(monthNumber: Int): String = when (monthNumber) {
    1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
    5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"
    9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; else -> "Dec"
}
