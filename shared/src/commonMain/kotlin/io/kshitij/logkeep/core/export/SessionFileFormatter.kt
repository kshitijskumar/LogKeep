package io.kshitij.logkeep.core.export

import io.kshitij.logkeep.db.LogEntry
import io.kshitij.logkeep.db.Session
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

internal object SessionFileFormatter {

    @OptIn(ExperimentalTime::class)
    fun format(session: Session, entries: List<LogEntry>): String {
        val tz = TimeZone.currentSystemDefault()
        val start = Instant.fromEpochMilliseconds(session.startedAt).toLocalDateTime(tz)

        return buildString {
            appendLine("Session started at: ${start.formatTime()} - ${start.formatDate()}")
            appendLine("Total logs: ${entries.size}")
            appendLine()
            appendLine()
            entries.forEach { entry ->
                val t = Instant.fromEpochMilliseconds(entry.timestamp).toLocalDateTime(tz)
                appendLine("${t.formatTime()} - ${entry.level.name} - ${entry.tag} - ${entry.message}")
                entry.stackTrace?.let { appendLine(it) }
                appendLine()
            }
        }
    }

    private fun LocalDateTime.formatTime() =
        "${hour.pad()}:${minute.pad()}:${second.pad()}"

    private fun LocalDateTime.formatDate(): String {
        val mon = month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        return "$mon ${dayOfMonth.pad()} $year"
    }

    private fun Int.pad() = toString().padStart(2, '0')
}
