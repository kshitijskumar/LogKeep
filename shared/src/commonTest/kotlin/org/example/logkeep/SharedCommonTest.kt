package org.example.logkeep

import org.example.logkeep.core.LogKeepConfig
import org.example.logkeep.core.LogLevel
import kotlin.test.Test
import kotlin.test.assertTrue

class SharedCommonTest {

    @Test
    fun logLevelValuesExist() {
        assertTrue(LogLevel.entries.isNotEmpty())
    }

    @Test
    fun configDefaults() {
        val config = LogKeepConfig()
        assertTrue(config.isEnabled)
        assertTrue(config.maxEntriesPerSession > 0)
        assertTrue(config.maxSessions > 0)
    }
}
