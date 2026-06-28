package io.kshitij.logkeep.core.export

interface SessionFileWriter {
    fun getOrCreateFile(fileName: String, content: String): String?
}
