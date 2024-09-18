package io.rebble.cobble.shared.ui.viewmodel

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryWithPlatforms
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.libpebblecommon.metadata.WatchHardwarePlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap

class LockerItemViewModel(private val httpClient: HttpClient, val entry: SyncedLockerEntryWithPlatforms): ViewModel() {
    open class ImageState {
        object Loading : ImageState()
        object Error : ImageState()
        data class Loaded(val image: ImageBitmap) : ImageState()
    }
    private var _imageState = MutableStateFlow<ImageState>(ImageState.Loading)
    val imageState = _imageState.asStateFlow()

    val title: String
        get() = entry.entry.title

    val developerName: String
        get() = entry.entry.developerName

    val circleWatchface: Boolean
        get() = entry.platforms.any { it.name == "chalk" } && entry.platforms.size == 1 //TODO: also display when chalk connected

    init {
        ConnectionStateManager.connectedWatchMetadata.onEach {
            val platform = it?.running?.hardwarePlatform?.get()?.let { platformId ->
                WatchHardwarePlatform.fromProtocolNumber(platformId)
            }
            val availablePlatform = platform?.let { entry.platforms.firstOrNull { it.name == platform.watchType.codename } }
            val imgPlatform = availablePlatform ?: entry.platforms.firstOrNull() ?: run {
                _imageState.value = ImageState.Error
                return@onEach
            }
            val imgUrl = if (entry.entry.type == "watchapp") {
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