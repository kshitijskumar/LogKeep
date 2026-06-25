package io.kshitij.logkeep

import app.cash.sqldelight.db.SqlDriver

internal expect fun createTestDriver(): SqlDriver
