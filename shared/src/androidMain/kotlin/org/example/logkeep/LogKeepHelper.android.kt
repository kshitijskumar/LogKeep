package org.example.logkeep

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.example.logkeep.core.LogKeepHelper
import org.example.logkeep.db.LogKeepDatabase

internal class LogKeepHelperAndroid(private val context: Context) : LogKeepHelper {
    override fun provideSqlDriver(): SqlDriver {
        return AndroidSqliteDriver(LogKeepDatabase.Schema, context, "logkeep.db")
    }
}
