package io.kshitij.logkeep

import io.kshitij.logkeep.db.LogEntry
import io.kshitij.logkeep.db.LogKeepDatabase
import io.kshitij.logkeep.db.logLevelAdapter

internal fun createTestDatabase(): LogKeepDatabase = LogKeepDatabase(
    driver = createTestDriver(),
    LogEntryAdapter = LogEntry.Adapter(levelAdapter = logLevelAdapter)
)
