package org.example.logkeep

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.example.logkeep.core.PlatformHelper
import org.example.logkeep.db.LogKeepDatabase

internal class AndroidPlatformHelper(private val context: Context) : PlatformHelper {
    override fun provideSqlDriver(): SqlDriver =
        AndroidSqliteDriver(LogKeepDatabase.Schema, context, "logkeep.db")
}
