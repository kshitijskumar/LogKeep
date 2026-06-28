package io.kshitij.logkeep.core.export

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory

@OptIn(ExperimentalForeignApi::class)
internal class IosSessionFileWriter : SessionFileWriter {
    override fun getOrCreateFile(fileName: String, content: String): String? = try {
        val dir = NSTemporaryDirectory()
        val txtPath = dir + fileName
        val tmpPath = "$txtPath.tmp"
        val fm = NSFileManager.defaultManager

        if (fm.fileExistsAtPath(txtPath)) return txtPath

        val data = content.encodeToByteArray().toNSData() ?: return null
        fm.createFileAtPath(tmpPath, data, null)
        fm.moveItemAtPath(tmpPath, toPath = txtPath, error = null)
        txtPath
    } catch (_: Exception) { null }
}
