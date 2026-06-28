package io.kshitij.logkeep.core.export

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory

@OptIn(ExperimentalForeignApi::class)
internal class IosSessionFileWriter : SessionFileWriter {
    override fun getOrCreateFile(baseName: String, content: String): String? {
        return try {
            val dir = NSTemporaryDirectory()
            val txtPath = dir + "$baseName.txt"
            val tmpPath = dir + "$baseName.tmp"
            val fm = NSFileManager.defaultManager

            if (fm.fileExistsAtPath(txtPath)) return txtPath

            val data = content.encodeToByteArray().toNSData() ?: return null
            fm.createFileAtPath(tmpPath, data, null)
            fm.moveItemAtPath(tmpPath, toPath = txtPath, error = null)
            txtPath
        } catch (_: Exception) { null }
    }
}
