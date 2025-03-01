package io.rebble.cobble.shared.ui.viewmodel

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benasher44.uuid.uuidFrom
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.database.NextSyncAction
import io.rebble.cobble.shared.database.dao.LockerDao
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryWithPlatforms
import io.rebble.cobble.shared.database.entity.getBestPlatformForDevice
import io.rebble.cobble.shared.database.entity.getSdkVersion
import io.rebble.cobble.shared.database.entity.getVersion
import io.rebble.cobble.shared.domain.state.ConnectionState
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.cobble.shared.domain.state.watchOrNull
import io.rebble.libpebblecommon.metadata.WatchHardwarePlatform
import io.rebble.libpebblecommon.packets.blobdb.AppMetadata
import io.rebble.libpebblecommon.packets.blobdb.BlobCommand
import io.rebble.libpebblecommon.packets.blobdb.BlobResponse
import io.rebble.libpebblecommon.structmapper.SUUID
import io.rebble.libpebblecommon.structmapper.StructMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.random.Random

class LockerItemViewModel(private val httpClient: HttpClient, val entry: SyncedLockerEntryWithPlatforms) : ViewModel(), KoinComponent {
    private val lockerDao: LockerDao by inject()

    open class ImageState {
        object Loading : ImageState()

        object Error : ImageState()

        data class Loaded(val image: ImageBitmap) : ImageState()
    }

    private var _imageState = MutableStateFlow<ImageState>(ImageState.Loading)
    val imageState = _imageState.asStateFlow()
    val supportedState =
        ConnectionStateManager.connectionState.flatMapConcat {
            it.watchOrNull?.metadata?.mapNotNull { meta ->
                meta?.running?.let { running ->
                    val platform = WatchHardwarePlatform.fromProtocolNumber(running.hardwarePlatform.get())
                    val compatibleVariants =
                        platform?.watchType?.getCompatibleAppVariants()?.map {
                            it.codename
                        } ?: emptyList()
                    entry.platforms.any { compatibleVariants.contains(it.name) }
                }
            } ?: flowOf(true)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    val title: String
        get() = entry.entry.title

    val developerName: String
        get() = entry.entry.developerName

    val circleWatchface: Boolean
        get() =
            entry.platforms.any {
                it.name == "chalk"
            } && entry.platforms.size == 1 // TODO: also display when chalk connected

    val hasSettings: Boolean
        get() = entry.entry.configurable

    val version: String
        get() = entry.entry.version

    val hearts: Int
        get() = entry.entry.hearts

    fun applyWatchface() {
        check(entry.entry.type == "watchface") { "Only watchfaces can be applied" }
        val watch = ConnectionStateManager.connectionState.value.watchOrNull
        viewModelScope.launch(Dispatchers.IO) {
            if (entry.entry.nextSyncAction == NextSyncAction.Ignore) {
                Logging.i("Watchface is offloaded and never synced, adding to locker")
                val blobDBService =
                    watch?.blobDBService ?: run {
                        Logging.e("No watch connected")
                        return@launch
                    }
                val platform =
                    entry.getBestPlatformForDevice(WatchHardwarePlatform.fromProtocolNumber(watch.metadata.value?.running?.hardwarePlatform?.get() ?: 0u)!!.watchType) ?: run {
                        Logging.e("No platform found for watch")
                        return@launch
                    }
                val syncedEntries = lockerDao.getSyncedEntries()
                val totalWatchfacesSynced = syncedEntries.count { it.type == "watchface" }
                if (totalWatchfacesSynced >= 10) {
                    val oldest =
                        lockerDao.getSyncedEntries().filter {
                            it.type == "watchface" && it.nextSyncAction == NextSyncAction.Nothing
                        }.minByOrNull { it.lastOpened ?: Instant.DISTANT_PAST }
                    oldest?.let {
                        Logging.d("Removing oldest entry ${it.title} ${it.uuid}")
                        val res =
                            blobDBService.send(
                                BlobCommand.DeleteCommand(
                                    Random.nextInt(0, UShort.MAX_VALUE.toInt()).toUShort(),
                                    BlobCommand.BlobDatabase.App,
                                    SUUID(StructMapper(), uuidFrom(oldest.uuid)).toBytes()
                                )
                            )
                        if (res.responseValue != BlobResponse.BlobStatus.Success) {
                            Logging.e("Failed to delete oldest entry (${res.responseValue})")
                        } else {
                            lockerDao.setNextSyncAction(it.id, NextSyncAction.Ignore)
                        }
                    } ?: Logging.w("No oldest entry found")
                }
                Logging.d("Inserting watchface ${entry.entry.uuid}")
                val (appVersionMajor, appVersionMinor) = entry.getVersion()
                val (sdkVersionMajor, sdkVersionMinor) = platform.getSdkVersion()
                val res =
                    blobDBService.send(
                        BlobCommand.InsertCommand(
                            Random.nextInt(0, UShort.MAX_VALUE.toInt()).toUShort(),
                            BlobCommand.BlobDatabase.App,
                            SUUID(StructMapper(), uuidFrom(entry.entry.uuid)).toBytes(),
                            AppMetadata().also { meta ->
                                meta.uuid.set(uuidFrom(entry.entry.uuid))
                                meta.flags.set(platform.processInfoFlags.toUInt())
                                meta.icon.set(entry.entry.pbwIconResourceId.toUInt())
                                meta.appVersionMajor.set(appVersionMajor)
                                meta.appVersionMinor.set(appVersionMinor)
                                meta.sdkVersionMajor.set(sdkVersionMajor)
                                meta.sdkVersionMinor.set(sdkVersionMinor)
                                meta.appName.set(entry.entry.title)
                            }.toBytes()
                        )
                    )
                if (res.responseValue == BlobResponse.BlobStatus.Success) {
                    lockerDao.setNextSyncAction(entry.entry.id, NextSyncAction.Nothing)
                } else {
                    Logging.e("Failed to insert watchface (${res.responseValue})")
                }
            }
            watch?.appRunStateService?.startApp(uuidFrom(entry.entry.uuid))
        }
    }

    init {
        ConnectionStateManager.connectionState.filterIsInstance<ConnectionState.Connected>().onEach {
            val platform =
                it.watch.metadata.value?.running?.hardwarePlatform?.get()?.let { platformId ->
                    WatchHardwarePlatform.fromProtocolNumber(platformId)
                }
            val availablePlatform =
                platform?.let {
                    entry.platforms.firstOrNull { it.name == platform.watchType.codename }
                }
            val imgPlatform =
                availablePlatform ?: entry.platforms.firstOrNull() ?: run {
                    _imageState.value = ImageState.Error
                    return@onEach
                }
            val imgUrl =
                if (entry.entry.type == "watchapp") {
                    imgPlatform.images.icon
                } else {
                    imgPlatform.images.screenshot
                } ?: run {
                    _imageState.value = ImageState.Error
                    return@onEach
                }
            withContext(Dispatchers.IO) {
                val image = httpClient.get(imgUrl)
                when (image.status) {
                    HttpStatusCode.OK -> {
                        _imageState.value = ImageState.Loaded(image.body<ByteArray>().decodeToImageBitmap())
                    }
                    else -> {
                        _imageState.value = ImageState.Error
                    }
                }
            }
        }.catch { e ->
            _imageState.value = ImageState.Error
            Logging.e("Error loading image for ${entry.entry.id}", e)
        }.launchIn(viewModelScope)
    }
}