package org.example.logkeep.core

import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import org.example.logkeep.AndroidPlatformHelper

class LogKeepInitProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        val ctx = context?.applicationContext ?: return false

        val meta = ctx.packageManager
            .getApplicationInfo(ctx.packageName, PackageManager.GET_META_DATA)
            .metaData ?: Bundle()

        val isDebug = (ctx.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (!isDebug) {
            return false
        }
        val config = LogKeepConfig(
            isEnabled = meta.getBoolean("logkeep.isEnabled", false),
            maxEntriesPerSession = meta.getInt("logkeep.maxEntriesPerSession", 1_000),
            maxSessions = meta.getInt("logkeep.maxSessions", 5)
        )

        if (!config.isEnabled) return false

        PlatformRegistry.setHelper(AndroidPlatformHelper(ctx))
        LogKeep.init(config)

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) { LogKeep.markSessionClean() }
            override fun onStart(owner: LifecycleOwner) { LogKeep.markSessionActive() }
        })

        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?) = null
    override fun getType(uri: Uri) = null
    override fun insert(uri: Uri, values: ContentValues?) = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?) = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?) = 0
}
