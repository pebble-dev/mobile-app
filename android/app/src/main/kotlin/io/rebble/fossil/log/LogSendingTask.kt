package io.rebble.fossil.log

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


/**
 * This should be eventually moved to flutter. Written it in Kotlin for now so we can use it while
 * testing other things.
 */
fun collectAndShareLogs(context: Context) = GlobalScope.launch(Dispatchers.IO) {
    val logsFolder = File(context.cacheDir, "logs")

    val targetFile = File(logsFolder, "logs.zip")

    var zipOutputStream: ZipOutputStream? = null
    try {
        zipOutputStream = ZipOutputStream(FileOutputStream(targetFile))
        for (file in logsFolder.listFiles() ?: emptyArray()) {
            if (!file.name.endsWith(".log")) {
                continue
            }
            val zipEntry = ZipEntry(file.name)
            zipOutputStream.putNextEntry(zipEntry)
            val buffer = ByteArray(1024)
            val inputStream = FileInputStream(file)
            var readBytes: Int
            while (inputStream.read(buffer).also { readBytes = it } > 0) {
                zipOutputStream.write(buffer, 0, readBytes)
            }
            inputStream.close()
            zipOutputStream.closeEntry()
        }
    } catch (e: Exception) {
        Timber.e(e, "Zip writing error")
    } finally {
        if (zipOutputStream != null) {
            try {
                zipOutputStream.close()
            } catch (ignored: IOException) {
            }
        }
    }

    withContext(Dispatchers.Main) {
        val targetUri = FileProvider.getUriForFile(context, "io.rebble.fossil.files", targetFile)

        val activityIntent = Intent(Intent.ACTION_SEND)

        activityIntent.putExtra(Intent.EXTRA_STREAM, targetUri)
        activityIntent.setType("application/octet-stream")

        activityIntent.setClipData(ClipData.newUri(context.getContentResolver(),
                "Fossil Logs",
                targetUri))

        activityIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val chooserIntent = Intent.createChooser(activityIntent, null)
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(chooserIntent)
    }
}