package io.kshitij.logkeep.core

import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import io.kshitij.logkeep.AndroidPlatformHelper
import io.kshitij.logkeep.ui.LogKeepActivityWatcher

class LogKeepInitProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        println("LogStuff: provider oncreate")
        val ctx = context?.applicationContext ?: return false

        val meta = ctx.packageManager
            .getApplicationInfo(ctx.packageName, PackageManager.GET_META_DATA)
            .metaData ?: Bundle()

        val config = LogKeepConfig(
            isEnabled = meta.getBoolean("logkeep.isEnabled", false),
            maxEntriesPerSession = meta.getInt("logkeep.maxEntriesPerSession", 1_000),
            maxSessions = meta.getInt("logkeep.maxSessions", 5)
        )

        println("LogStuff: provider config: $config")

        if (!config.isEnabled) return false

        PlatformRegistry.setHelper(AndroidPlatformHelper(ctx))
        LogKeep.init(config)
        (ctx as android.app.Application).registerActivityLifecycleCallbacks(LogKeepActivityWatcher)

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                println("LogStuff: provider stop")
                LogKeep.markSessionClean()
            }
            override fun onStart(owner: LifecycleOwner) {
                println("LogStuff: provider start")
                LogKeep.markSessionActive()
            }
        })

        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?) = null
    override fun getType(uri: Uri) = null
    override fun insert(uri: Uri, values: ContentValues?) = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?) = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?) = 0
}
