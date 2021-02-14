package io.rebble.cobble.providers

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import com.getpebble.android.kit.Constants
import io.rebble.cobble.CobbleApplication
import io.rebble.cobble.transport.bluetooth.ConnectionLooper
import io.rebble.cobble.transport.bluetooth.ConnectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PebbleKitProvider : ContentProvider() {
    private var initialized = false

    private lateinit var connectionLooper: ConnectionLooper

    override fun onCreate(): Boolean {
        // Do not initialize anything here as this gets called before Application.onCreate

        return true
    }

    private fun initializeIfNeeded() {
        if (initialized) {
            return
        }

        val context = context
                ?: throw IllegalStateException("Context should not be null when initializing")

        initialized = true

        val injectionComponent = (context as CobbleApplication)
                .component

        connectionLooper = injectionComponent.createConnectionLooper()

        GlobalScope.launch(Dispatchers.Main.immediate) {
            connectionLooper.connectionState.collect {
                context.contentResolver.notifyChange(Constants.URI_CONTENT_BASALT, null)
            }
        }
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        if (uri != Constants.URI_CONTENT_BASALT) {
            return null
        }

        initializeIfNeeded()

        val cursor = MatrixCursor(CURSOR_COLUMN_NAMES)

        if (connectionLooper.connectionState.value is ConnectionState.Connected) {
            // Hardcode latest 4.4.0 firmware for now until watch status packets are
            // implemented

            cursor.addRow(
                    listOf(
                            1, // Connected
                            1, // App Message support
                            0, // Data Logging support
                            4, // Major version support
                            4, // Minor version support
                            0, // Point version support
                            "", // Version Tag
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
                            "", // Version Tag
                    )
            )
        }

        return cursor
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // This provider is read-only
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        // This provider is read-only
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        // This provider is read-only
        return 0
    }
}

private val CURSOR_COLUMN_NAMES = arrayOf(
        Constants.KIT_STATE_COLUMN_CONNECTED.toString(),
        Constants.KIT_STATE_COLUMN_APPMSG_SUPPORT.toString(),
        Constants.KIT_STATE_COLUMN_DATALOGGING_SUPPORT.toString(),
        Constants.KIT_STATE_COLUMN_VERSION_MAJOR.toString(),
        Constants.KIT_STATE_COLUMN_VERSION_MINOR.toString(),
        Constants.KIT_STATE_COLUMN_VERSION_POINT.toString(),
        Constants.KIT_STATE_COLUMN_VERSION_TAG.toString()
)