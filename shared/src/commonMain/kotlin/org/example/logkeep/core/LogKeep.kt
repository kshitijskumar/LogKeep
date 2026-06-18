package org.example.logkeep.core

object LogKeep {

    private val helper by lazy { LogKeepHelperSetter.helper }

    internal fun init(config: LogKeepConfig = LogKeepConfig()) {
        // Phase 1
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
