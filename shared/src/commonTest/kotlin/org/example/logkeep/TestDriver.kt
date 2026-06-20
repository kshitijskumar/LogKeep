package org.example.logkeep

import app.cash.sqldelight.db.SqlDriver

internal expect fun createTestDriver(): SqlDriver
