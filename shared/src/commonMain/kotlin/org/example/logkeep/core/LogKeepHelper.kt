package org.example.logkeep.core

import app.cash.sqldelight.db.SqlDriver

interface LogKeepHelper {

    fun provideSqlDriver(): SqlDriver

}

expect object LogKeepHelperSetter {

    val helper: LogKeepHelper

    fun setHelper(helper: LogKeepHelper)

}