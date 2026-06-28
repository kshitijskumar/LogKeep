package io.kshitij.logkeep.core.export

import android.content.Context
import java.io.File

internal class AndroidSessionFileWriter(private val context: Context) : SessionFileWriter {
    override fun getOrCreateFile(baseName: String, content: String): String? {
        return try {
            val txtFile = File(context.cacheDir, "$baseName.txt")
            if (txtFile.exists()) return txtFile.absolutePath

            val tmpFile = File(context.cacheDir, "$baseName.tmp")
            tmpFile.writeText(content)
            tmpFile.renameTo(txtFile)
            txtFile.absolutePath
        } catch (_: Exception) { null }
    }
}
