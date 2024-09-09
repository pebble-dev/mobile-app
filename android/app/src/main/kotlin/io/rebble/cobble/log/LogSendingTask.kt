package io.rebble.cobble.log

import android.companion.CompanionDeviceManager
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.NotificationListenerService
import androidx.core.content.FileProvider
import io.rebble.cobble.BuildConfig
import io.rebble.cobble.CobbleApplication
import io.rebble.cobble.middleware.DeviceLogController
import io.rebble.cobble.util.hasNotificationAccessPermission
import io.rebble.libpebblecommon.metadata.WatchHardwarePlatform
import io.rebble.libpebblecommon.packets.LogDump
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


private fun generateDebugInfo(context: Context, rwsId: String): String {
    val commitHash = BuildConfig.COMMIT_HASH
    val branchName = BuildConfig.BRANCH_NAME
    val sdkVersion = Build.VERSION.SDK_INT
    val device = Build.DEVICE
    val model = Build.MODEL
    val product = Build.PRODUCT
    val manufacturer = Build.MANUFACTURER

    val inj = (context.applicationContext as CobbleApplication).component
    val connectionLooper = inj.createConnectionLooper()
    val watchMetadataStore = inj.createWatchMetadataStore()
    val connectionState = connectionLooper.connectionState.value

    val watchMeta = watchMetadataStore.lastConnectedWatchMetadata.value
    val watchModel = watchMeta?.running?.hardwarePlatform?.get()?.let {
        WatchHardwarePlatform.fromProtocolNumber(it)
    }
    val watchVersionTag = watchMeta?.running?.versionTag?.get()
    val watchIsRecovery = watchMeta?.running?.isRecovery?.get()


    val associatedDevices = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val deviceManager = context.getSystemService(CompanionDeviceManager::class.java)
        deviceManager.associations
    } else {
        null
    }
    val allowedPermissions = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
            .requestedPermissions.map {
                it to (context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED)
            }
    return """
    Commit Hash: $commitHash
    Branch Name: $branchName
    SDK Version: $sdkVersion
    Device: $device
    Model: $model
    Product: $product
    Manufacturer: $manufacturer
    Connection State: $connectionState
    Associated devices: $associatedDevices
    Watch Model: $watchModel
    Watch Version Tag: $watchVersionTag
    Watch Is Recovery: $watchIsRecovery
    RWS ID:
    $rwsId
    Allowed Permissions:
    ${allowedPermissions.joinToString("\n") { (permission, result) -> "$permission: $result" }}
    Notification listening enabled: ${context.hasNotificationAccessPermission()}
    """.trimIndent()
}

private suspend fun getDeviceLogs(deviceLogController: DeviceLogController): List<String>? {
    val flow = deviceLogController.requestLogDump()
    if (flow.first() is LogDump.NoLogs) {
        Timber.d("No logs available")
        return null
    }
    return flow.filterIsInstance<LogDump.LogLine>()
            .onEach {
                Timber.d("Log line: ${it.timestamp.get()} ${it.filename.get()}:${it.line.get()} ${it.level.get()} ${it.messageText.get()}")
            }
            .map {
                "${it.timestamp.get()} ${it.filename.get()}:${it.line.get()} ${it.level.get()} ${it.messageText.get()}"
            }.toList()
}

/**
 * This should be eventually moved to flutter. Written it in Kotlin for now so we can use it while
 * testing other things.
 */
fun collectAndShareLogs(context: Context, rwsId: String) = GlobalScope.launch(Dispatchers.IO) {
    val logsFolder = File(context.cacheDir, "logs")
    val date = LocalDateTime.now(ZoneId.of("UTC")).format(DateTimeFormatter.ISO_DATE_TIME)
    val targetFile = File(logsFolder, "logs-${date}.zip")

    var zipOutputStream: ZipOutputStream? = null
    val debugInfo = generateDebugInfo(context, rwsId)
    val deviceLogController = (context.applicationContext as CobbleApplication).component.createDeviceLogController()
    val deviceLogs = getDeviceLogs(deviceLogController)
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
        zipOutputStream.putNextEntry(ZipEntry("debug_info.txt"))
        zipOutputStream.write(debugInfo.toByteArray())
        zipOutputStream.closeEntry()
        deviceLogs?.let {
            zipOutputStream.putNextEntry(ZipEntry("device_logs.txt"))
            zipOutputStream.write(it.joinToString("\n").toByteArray())
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

    withContext(Dispatchers.Main.immediate) {
        val targetUri = FileProvider.getUriForFile(context, "io.rebble.cobble.files", targetFile)

        val activityIntent = Intent(Intent.ACTION_SEND)

        activityIntent.putExtra(Intent.EXTRA_STREAM, targetUri)
        activityIntent.setType("application/octet-stream")

        activityIntent.clipData = ClipData.newUri(context.contentResolver,
                "Cobble Logs",
                targetUri)

        activityIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val chooserIntent = Intent.createChooser(activityIntent, null)
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(chooserIntent)
    }
}