package org.example.logkeep.ui.sessions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import org.example.logkeep.db.Session
import kotlin.time.ExperimentalTime

@Composable
internal fun SessionsScreen(
    viewModel: SessionsViewModel = viewModel(
        initializer = {
            SessionsViewModel(
                sessionClickedDelegate = { println("LogStuff: session clicked: $it") }
            )
        }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sessions = uiState.sessions

    when {
        sessions == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        sessions.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No sessions yet")
            }
        }
        else -> {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = sessions, key = { it.id }) { session ->
                    SessionItem(session = session, onClick = { viewModel.onSessionClicked(session.id) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun SessionItem(session: Session, onClick: () -> Unit) {
    Text(
        text = formatSessionTime(session.startedAt),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@OptIn(ExperimentalTime::class)
private fun formatSessionTime(epochMillis: Long): String {
    val dt = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val month = dt.monthNumber.toString().padStart(2, '0')
    val day = dt.dayOfMonth.toString().padStart(2, '0')
    val hour = dt.hour.toString().padStart(2, '0')
    val minute = dt.minute.toString().padStart(2, '0')
    return "${dt.year}-$month-$day $hour:$minute"
}
