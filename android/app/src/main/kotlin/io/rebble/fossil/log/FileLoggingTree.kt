package io.rebble.fossil.log

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class FileLoggingTree(context: Context, appTag: String) : AppTaggedDebugTree(appTag) {
    private val loggingDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    val logsFolder = File(context.cacheDir, "logs")

    private var writer: BufferedWriter? = null
    private val currentDayOfYear: Int = -1

    @Suppress("BlockingMethodInNonBlockingContext")
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        GlobalScope.launch(loggingDispatcher) {
            Thread.currentThread().priority = Thread.MIN_PRIORITY

            openFileIfNeeded()
            val writer = writer ?: return@launch

            try {
                val calendar = Calendar.getInstance()
                writer.write(
                        getLogPriorityAbbreviation(priority).toString() + " " +
                                LOG_DATE_FORMAT.format(calendar.time) + " [" +
                                tag + "Z] " +
                                message
                )
                writer.newLine()

                t?.printStackTrace(PrintWriter(writer))

                writer.flush()
            } catch (ignored: IOException) {
                try {
                    writer.close()
                } catch (ignored2: IOException) {
                }
                this@FileLoggingTree.writer = null
            }
        }

    }

    private fun openFileIfNeeded() {
        val today = Calendar.getInstance()
        if (writer != null && today.get(Calendar.DAY_OF_YEAR) == currentDayOfYear) {
            // Latest file is already open. No need to do anything.
            return
        }

        if (!logsFolder.exists()) {
            if (!logsFolder.mkdir()) {
                throw RuntimeException("Cannot create logging folder!")
            }
        }

        val fileNameTimeStamp = SimpleDateFormat("yyyy-dd-MM", Locale.getDefault())
                .format(Date())

        val filename = "log_$fileNameTimeStamp.log"
        val file = File(logsFolder, filename)

        try {
            writer = BufferedWriter(FileWriter(file, true))
        } catch (ignored: IOException) {
            writer = null
        }
    }


    private fun getLogPriorityAbbreviation(priority: Int): Char {
        return when (priority) {
            Log.ASSERT -> 'A'
            Log.DEBUG -> 'D'
            Log.ERROR -> 'E'
            Log.INFO -> 'I'
            Log.WARN -> 'W'
            else -> 'V'
        }
    }
}

@SuppressLint("ConstantLocale")
private val LOG_DATE_FORMAT: DateFormat = SimpleDateFormat(
        "HH:mm:ss.SSS", Locale.getDefault()
).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}