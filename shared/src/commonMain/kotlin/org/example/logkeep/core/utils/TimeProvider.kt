package org.example.logkeep.core.utils

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
internal object TimeProvider {
    fun now(): Long = Clock.System.now().toEpochMilliseconds()
}
