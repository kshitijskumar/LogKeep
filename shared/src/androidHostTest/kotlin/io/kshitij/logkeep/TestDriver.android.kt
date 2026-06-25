package io.kshitij.logkeep

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.kshitij.logkeep.db.LogKeepDatabase

internal actual fun createTestDriver(): SqlDriver =
    JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        .also { LogKeepDatabase.Schema.create(it) }
