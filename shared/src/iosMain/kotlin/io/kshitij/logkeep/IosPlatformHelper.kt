package io.kshitij.logkeep

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import io.kshitij.logkeep.core.PlatformHelper
import io.kshitij.logkeep.db.LogKeepDatabase

internal class IosPlatformHelper : PlatformHelper {
    override fun provideSqlDriver(): SqlDriver =
        NativeSqliteDriver(LogKeepDatabase.Schema, "logkeep.db")
}
