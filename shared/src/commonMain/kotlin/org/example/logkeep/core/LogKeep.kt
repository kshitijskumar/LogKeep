package org.example.logkeep.core

object LogKeep {

    internal fun init(config: LogKeepConfig = LogKeepConfig()) {
        val driver = PlatformRegistry.getHelper().provideSqlDriver()
        // Phase 1: create database, open session, etc.
    }

    fun log(
        level: LogLevel,
        tag: String,
        message: String,
        throwable: Throwable? = null
    ) {
        // Phase 1
    }
}
