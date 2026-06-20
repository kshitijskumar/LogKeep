package org.example.logkeep

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.example.logkeep.db.LogKeepDatabase

internal actual fun createTestDriver(): SqlDriver =
    NativeSqliteDriver(LogKeepDatabase.Schema, "test_logkeep.db")
