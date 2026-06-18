package org.example.logkeep

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.example.logkeep.core.PlatformHelper
import org.example.logkeep.db.LogKeepDatabase

internal class IosPlatformHelper : PlatformHelper {
    override fun provideSqlDriver(): SqlDriver =
        NativeSqliteDriver(LogKeepDatabase.Schema, "logkeep.db")
}
