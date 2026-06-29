package io.kshitij.logkeep.core.export

import android.content.Context
import android.content.Intent
import java.io.File

internal class AndroidSessionSharer(private val context: Context) : SessionSharer {
    override fun share(filePath: String) {
        val uri = LogKeepFileProvider.getUriForFile(
            context,
            "${context.packageName}.logkeep.fileprovider",
            File(filePath)
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share session logs").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
