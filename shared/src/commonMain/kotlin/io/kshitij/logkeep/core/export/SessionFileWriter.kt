package io.kshitij.logkeep.core.export

interface SessionFileWriter {
    fun getOrCreateFile(baseName: String, content: String): String?
}
