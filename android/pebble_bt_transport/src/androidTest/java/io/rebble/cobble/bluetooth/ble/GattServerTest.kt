package io.rebble.cobble.bluetooth.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.test.filters.RequiresDevice
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import io.rebble.cobble.bluetooth.ProtocolIO
import io.rebble.libpebblecommon.ProtocolHandlerImpl
import io.rebble.libpebblecommon.disk.PbwBinHeader
import io.rebble.libpebblecommon.metadata.WatchType
import io.rebble.libpebblecommon.metadata.pbw.appinfo.PbwAppInfo
import io.rebble.libpebblecommon.metadata.pbw.manifest.PbwManifest
import io.rebble.libpebblecommon.packets.*
import io.rebble.libpebblecommon.packets.blobdb.BlobCommand
import io.rebble.libpebblecommon.packets.blobdb.BlobResponse
import io.rebble.libpebblecommon.protocolhelpers.PebblePacket
import io.rebble.libpebblecommon.protocolhelpers.ProtocolEndpoint
import io.rebble.libpebblecommon.services.AppFetchService
import io.rebble.libpebblecommon.services.PutBytesService
import io.rebble.libpebblecommon.services.SystemService
import io.rebble.libpebblecommon.services.app.AppRunStateService
import io.rebble.libpebblecommon.services.blobdb.BlobDBService
import io.rebble.libpebblecommon.services.notification.NotificationService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import timber.log.Timber
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.util.TimeZone
import java.util.UUID
import java.util.zip.ZipInputStream
import kotlin.random.Random

/**
 * These tests are intended as long-running integration tests for the GATT server, to debug issues, not as unit tests.
 */
@RequiresDevice
class GattServerTest {
    @JvmField
    @Rule
    val mGrantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.BLUETOOTH
    )

    companion object {
        private const val DEVICE_ADDRESS_LE = "71:D2:AE:CE:30:C1"
        val appVersionSent = CompletableDeferred<Unit>()

        suspend fun appVersionRequestHandler(): PhoneAppVersion.AppVersionResponse {
            Timber.d("App version request received")
            coroutineScope {
                launch {
                    appVersionSent.complete(Unit)
                }
            }
            return PhoneAppVersion.AppVersionResponse(
                    UInt.MAX_VALUE,
                    0u,
                    PhoneAppVersion.PlatformFlag.makeFlags(
                            PhoneAppVersion.OSType.Android,
                            listOf(
                                    PhoneAppVersion.PlatformFlag.BTLE,
                            )
                    ),
                    2u,
                    4u,
                    4u,
                    2u,
                    ProtocolCapsFlag.makeFlags(
                            listOf(
                                    ProtocolCapsFlag.Supports8kAppMessage,
                                    ProtocolCapsFlag.SupportsExtendedMusicProtocol,
                                    ProtocolCapsFlag.SupportsAppRunStateProtocol
                            )
                    )

            )
        }
    }

    lateinit var context: Context
    lateinit var bluetoothAdapter: BluetoothAdapter

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        Timber.plant(Timber.DebugTree())
        val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
        bluetoothAdapter = bluetoothManager.adapter ?: error("Bluetooth adapter not available")
    }

    suspend fun makeSession(clientConn: BlueGATTConnection, connectionScope: CoroutineScope) {
        val connector = PebbleLEConnector(clientConn, context, connectionScope)
        connector.connect().onEach {
            Timber.d("Connector state: $it")
        }.first { it == PebbleLEConnector.ConnectorState.CONNECTED }
        Timber.d("Connected to watch")
        PPoGLinkStateManager.updateState(clientConn.device.address, PPoGLinkState.ReadyForSession)
        PPoGLinkStateManager.getState(clientConn.device.address).first { it == PPoGLinkState.SessionOpen }
    }

    @OptIn(FlowPreview::class)
    @Test
    fun connectToWatchAndPing() = runBlocking {
        withTimeout(10000) {
            restartBluetooth(bluetoothAdapter)
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val connectionScope = CoroutineScope(Dispatchers.IO) + CoroutineName("ConnectionScope")
        val server = NordicGattServer(
                context = context
        )
        server.open()
        assertTrue(server.isOpened)

        val device = bluetoothAdapter.getRemoteLeDevice(DEVICE_ADDRESS_LE, BluetoothDevice.ADDRESS_TYPE_RANDOM)
        assertNotNull(device)

        val clientConn = device.connectGatt(context, false)
        assertNotNull(clientConn)

        makeSession(clientConn!!, connectionScope)

        val serverRx = server.rxFlowFor(device.address)
        assertNotNull(serverRx)

        val protocolHandler = ProtocolHandlerImpl()
        val systemService = SystemService(protocolHandler)
        val blobService = BlobDBService(protocolHandler)
        val notifService = NotificationService(blobService)
        systemService.appVersionRequestHandler = Companion::appVersionRequestHandler

        val protocolInputStream = PipedInputStream()
        val protocolOutputStream = PipedOutputStream()
        val rxStream = PipedOutputStream(protocolInputStream)

        val protocolIO = ProtocolIO(
                protocolInputStream.buffered(8192),
                protocolOutputStream.buffered(8192),
                protocolHandler,
                MutableSharedFlow()
        )
        val sendLoop = connectionScope.launch {
            protocolHandler.startPacketSendingLoop {
                server.sendMessageToDevice(device.address, it.asByteArray())
                return@startPacketSendingLoop true
            }
        }

        serverRx!!.onEach {
            withContext(Dispatchers.IO) {
                rxStream.write(it)
            }
        }.launchIn(connectionScope)

        val ping = PingPong.Ping(1337u)
        val completeable = CompletableDeferred<PebblePacket>()
        protocolHandler.registerReceiveCallback(ProtocolEndpoint.PING) {
            completeable.complete(it)
        }
        launch {
            protocolHandler.send(ping)
        }

        val pong = completeable.await() as? PingPong.Pong
        assertNotNull(pong)
        assertEquals(1337u, pong!!.cookie.get())

        server.close()
        assertFalse(server.isOpened)

        clientConn.close()
        connectionScope.cancel()
    }

    @OptIn(FlowPreview::class)
    @Test
    fun connectToWatchAndInstallApp() = runBlocking {
        withTimeout(10000) {
            restartBluetooth(bluetoothAdapter)
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val connectionScope = CoroutineScope(Dispatchers.IO) + CoroutineName("ConnectionScope")
        val server = NordicGattServer(
                context = context
        )
        server.open()
        assertTrue(server.isOpened)

        val device = bluetoothAdapter.getRemoteLeDevice(DEVICE_ADDRESS_LE, BluetoothDevice.ADDRESS_TYPE_RANDOM)
        assertNotNull(device)

        val clientConn = device.connectGatt(context, false)
        assertNotNull(clientConn)

        makeSession(clientConn!!, connectionScope)

        val serverRx = server.rxFlowFor(device.address)
        assertNotNull(serverRx)

        val protocolHandler = ProtocolHandlerImpl()
        val systemService = SystemService(protocolHandler)
        val putBytesService = PutBytesService(protocolHandler)
        val appFetchService = AppFetchService(protocolHandler)
        val blobDBService = BlobDBService(protocolHandler)
        val appRunStateService = AppRunStateService(protocolHandler)
        var watchVersion: WatchVersion.WatchVersionResponse? = null

        /* -- Load app from resources -- */
        Timber.d("Loading app from resources")
        systemService.appVersionRequestHandler = ::appVersionRequestHandler
        val json = Json { ignoreUnknownKeys = true }
        var pbwAppInfo: PbwAppInfo? = null
        var pbwManifest: PbwManifest? = null
        var pbwResBlob: ByteArray? = null
        var pbwBinaryBlob: ByteArray? = null
        context.assets.open("pixel-miner.pbw").use {
            val zipInputStream = ZipInputStream(it)
            while (true) {
                val entry = zipInputStream.nextEntry ?: break
                when (entry.name) {
                    "appinfo.json" -> {
                        pbwAppInfo = json.decodeFromStream(zipInputStream)
                    }

                    "manifest.json" -> {
                        pbwManifest = json.decodeFromStream(zipInputStream)
                    }

                    "app_resources.pbpack" -> {
                        pbwResBlob = zipInputStream.readBytes()
                    }

                    "pebble-app.bin" -> {
                        pbwBinaryBlob = zipInputStream.readBytes()
                    }
                }
            }
        }
        assertNotNull(pbwAppInfo)
        assertNotNull(pbwManifest)
        assertNotNull(pbwResBlob)
        assertNotNull(pbwBinaryBlob)


        /* -- Setup app fetch service -- */
        appFetchService.receivedMessages.receiveAsFlow().onEach { message ->
            Timber.d("Received appfetch message: $message")
            if (message is AppFetchRequest) {
                val appUuid = message.uuid.get().toString()

                appFetchService.send(AppFetchResponse(AppFetchResponseStatus.START))

                putBytesService.sendAppPart(
                        message.appId.get(),
                        pbwBinaryBlob!!,
                        WatchType.BASALT,
                        watchVersion!!,
                        pbwManifest!!.application,
                        ObjectType.APP_EXECUTABLE
                )

                if (pbwManifest!!.resources != null) {
                    putBytesService.sendAppPart(
                            message.appId.get(),
                            pbwResBlob!!,
                            WatchType.BASALT,
                            watchVersion!!,
                            pbwManifest!!.resources!!,
                            ObjectType.APP_RESOURCE
                    )
                }
            }
        }

        val sendLoop = connectionScope.launch {
            protocolHandler.startPacketSendingLoop {
                Timber.d("Sending packet")
                server.sendMessageToDevice(device.address, it.asByteArray())
            }
        }

        serverRx!!.onEach {
            Timber.d("Received packet")
            protocolHandler.receivePacket(it.asUByteArray())
        }.launchIn(connectionScope)

        val timezone = TimeZone.getDefault()
        val now = System.currentTimeMillis()

        val updateTimePacket = TimeMessage.SetUTC(
                (now / 1000).toUInt(),
                timezone.getOffset(now).toShort(),
                timezone.id
        )
        systemService.send(updateTimePacket)

        Timber.d("Requesting watch version")
        val watchVersionResponse = systemService.requestWatchVersion()
        assertNotNull(watchVersionResponse)
        Timber.d("Watch version: ${watchVersionResponse.running.versionTag.get()}")
        watchVersion = watchVersionResponse
        val watchModel = systemService.requestWatchModel()
        Timber.d("Watch model: $watchModel")

        /* -- Insert app into BlobDB -- */
        Timber.d("Clearing App BlobDB")
        val clearResult = blobDBService.send(BlobCommand.ClearCommand(
                Random.nextInt(0, UShort.MAX_VALUE.toInt()).toUShort(),
                BlobCommand.BlobDatabase.App
        )).responseValue
        assertEquals(BlobResponse.BlobStatus.Success, clearResult)
        Timber.d("Cleared App BlobDB")
        val headerData = pbwBinaryBlob!!.copyOfRange(0, PbwBinHeader.SIZE)

        val parsedHeader = PbwBinHeader.parseFileHeader(headerData.asUByteArray())
        Timber.d("Inserting app into BlobDB")
        val insertResult = blobDBService.send(
                BlobCommand.InsertCommand(
                        Random.nextInt(0, UShort.MAX_VALUE.toInt()).toUShort(),
                        BlobCommand.BlobDatabase.App,
                        parsedHeader.uuid.toBytes(),
                        parsedHeader.toBlobDbApp().toBytes()
                )
        )
        assertEquals(BlobResponse.BlobStatus.Success, insertResult)
        Timber.d("Inserted app into BlobDB")

        val runStateStart = connectionScope.async {
            appRunStateService.receivedMessages.receiveAsFlow().first { it is AppRunStateMessage.AppRunStateStart }
        }

        /* -- Send launch app message -- */
        Timber.d("Sending launch app message")
        appRunStateService.send(AppRunStateMessage.AppRunStateStart(
                UUID.fromString(pbwAppInfo!!.uuid))
        )

        withTimeout(3000) {
            runStateStart.await()
        }

        server.close()
        assertFalse(server.isOpened)

        clientConn.close()
        connectionScope.cancel()
    }
}