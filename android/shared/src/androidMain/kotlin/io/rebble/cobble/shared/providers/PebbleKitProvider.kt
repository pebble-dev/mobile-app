package io.rebble.cobble.shared.providers

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.getpebble.android.kit.Constants
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.cobble.shared.domain.state.watchOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PebbleKitProvider : ContentProvider() {
    private var initialized = false

    override fun onCreate(): Boolean {
        // Do not initialize anything here as this gets called before Application.onCreate

        return true
    }

    @Synchronized
    private fun initializeIfNeeded() {
        if (initialized) {
            return
        }

        val context =
            context
                ?: throw IllegalStateException("Context should not be null when initializing")

        initialized = true

        GlobalScope.launch(Dispatchers.Main.immediate) {
            ConnectionStateManager.connectionState.collect {
                context.contentResolver.notifyChange(Constants.URI_CONTENT_BASALT, null)
            }
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        if (uri != Constants.URI_CONTENT_BASALT) {
            return null
        }

        initializeIfNeeded()

        val cursor = MatrixCursor(CURSOR_COLUMN_NAMES)

        val metadata = ConnectionStateManager.connectionState.value.watchOrNull?.metadata?.value

        if (ConnectionStateManager.connectionState.value is ConnectionState.Connected &&
            metadata != null
        ) {
            val parsedVersion = FIRMWARE_VERSION_REGEX.find(metadata.running.versionTag.get())
            val groupValues = parsedVersion?.groupValues
            val majorVersion = groupValues?.elementAtOrNull(1)?.toIntOrNull() ?: 0
            val minorVersion = groupValues?.elementAtOrNull(2)?.toIntOrNull() ?: 0
            val pointVersion = groupValues?.elementAtOrNull(3)?.toIntOrNull() ?: 0
            val tag = groupValues?.elementAtOrNull(4) ?: ""

            cursor.addRow(
                listOf(
                    1, // Connected
                    1, // App Message support
                    0, // Data Logging support
                    majorVersion, // Major version support
                    minorVersion, // Minor version support
                    pointVersion, // Point version support
                    tag // Version Tag
                )
            )
        } else {
            cursor.addRow(
                listOf(
                    0, // Connected
                    0, // App Message support
                    0, // Data Logging support
                    0, // Major version support
                    0, // Minor version support
                    0, // Point version support
                    "" // Version Tag
                )
            )
        }

        return cursor
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(
        uri: Uri,
        values: ContentValues?
    ): Uri? {
        // This provider is read-only
        return null
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        // This provider is read-only
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        // This provider is read-only
        return 0
    }
}

private val CURSOR_COLUMN_NAMES =
    arrayOf(
        Constants.KIT_STATE_COLUMN_CONNECTED.toString(),
        Constants.KIT_STATE_COLUMN_APPMSG_SUPPORT.toString(),
        Constants.KIT_STATE_COLUMN_DATALOGGING_SUPPORT.toString(),
        Constants.KIT_STATE_COLUMN_VERSION_MAJOR.toString(),
        Constants.KIT_STATE_COLUMN_VERSION_MINOR.toString(),
        Constants.KIT_STATE_COLUMN_VERSION_POINT.toString(),
        Constants.KIT_STATE_COLUMN_VERSION_TAG.toString()
    )

private val FIRMWARE_VERSION_REGEX = Regex("([0-9]+)\\.([0-9]+)\\.([0-9]+)(?:-(.*))?")