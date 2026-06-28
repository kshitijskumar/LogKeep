package io.kshitij.logkeep.core.export

import android.content.Context
import java.io.File

internal class AndroidSessionFileWriter(private val context: Context) : SessionFileWriter {
    override fun getOrCreateFile(fileName: String, content: String): String? {
        return try {
            val txtFile = File(context.cacheDir, fileName)
            if (txtFile.exists()) return txtFile.absolutePath

            val tmpFile = File(context.cacheDir, "$fileName.tmp")
            tmpFile.writeText(content)
            tmpFile.renameTo(txtFile)
            txtFile.absolutePath
        } catch (_: Exception) { null }
    }
}
