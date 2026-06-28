package io.kshitij.logkeep

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import io.kshitij.logkeep.core.PlatformHelper
import io.kshitij.logkeep.core.export.AndroidSessionFileWriter
import io.kshitij.logkeep.core.export.SessionFileWriter
import io.kshitij.logkeep.db.LogKeepDatabase

internal class AndroidPlatformHelper(private val context: Context) : PlatformHelper {
    override fun provideSqlDriver(): SqlDriver =
        AndroidSqliteDriver(LogKeepDatabase.Schema, context, "logkeep.db")

    override fun provideSessionFileWriter(): SessionFileWriter =
        AndroidSessionFileWriter(context)
}
