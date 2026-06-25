package io.kshitij.logkeep.db

import app.cash.sqldelight.ColumnAdapter
import io.kshitij.logkeep.core.LogLevel

val logLevelAdapter: ColumnAdapter<LogLevel, String> =
    object : ColumnAdapter<LogLevel, String> {
        override fun decode(databaseValue: String) =
            LogLevel.valueOf(databaseValue)
        override fun encode(value: LogLevel) = value.name
    }

val booleanAsIntAdapter: ColumnAdapter<Boolean, Long> =
    object : ColumnAdapter<Boolean, Long> {
        override fun decode(databaseValue: Long) = databaseValue != 0L
        override fun encode(value: Boolean) = if (value) 1L else 0L
    }
