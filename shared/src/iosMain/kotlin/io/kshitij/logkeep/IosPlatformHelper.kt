package io.kshitij.logkeep

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import io.kshitij.logkeep.core.PlatformHelper
import io.kshitij.logkeep.core.export.IosSessionFileWriter
import io.kshitij.logkeep.core.export.SessionFileWriter
import io.kshitij.logkeep.core.export.SessionSharer
import io.kshitij.logkeep.db.LogKeepDatabase

internal class IosPlatformHelper : PlatformHelper {
    override fun provideSqlDriver(): SqlDriver =
        NativeSqliteDriver(LogKeepDatabase.Schema, "logkeep.db")

    override fun provideSessionFileWriter(): SessionFileWriter =
        IosSessionFileWriter()

    // TODO::KSHITIJ-come back for this
    override fun provideSessionSharer(): SessionSharer =
        object : SessionSharer { override fun share(filePath: String) = Unit }
}
