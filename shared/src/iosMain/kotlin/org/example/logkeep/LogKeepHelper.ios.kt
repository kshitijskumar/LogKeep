package org.example.logkeep

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.example.logkeep.core.LogKeepHelper
import org.example.logkeep.db.LogKeepDatabase

internal class LogKeepHelperIos : LogKeepHelper {
    override fun provideSqlDriver(): SqlDriver {
        return NativeSqliteDriver(LogKeepDatabase.Schema, "logkeep.db")
    }
}
