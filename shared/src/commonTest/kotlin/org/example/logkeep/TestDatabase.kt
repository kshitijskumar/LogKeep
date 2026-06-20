package org.example.logkeep

import org.example.logkeep.db.LogEntry
import org.example.logkeep.db.LogKeepDatabase
import org.example.logkeep.db.logLevelAdapter

internal fun createTestDatabase(): LogKeepDatabase = LogKeepDatabase(
    driver = createTestDriver(),
    LogEntryAdapter = LogEntry.Adapter(levelAdapter = logLevelAdapter)
)
