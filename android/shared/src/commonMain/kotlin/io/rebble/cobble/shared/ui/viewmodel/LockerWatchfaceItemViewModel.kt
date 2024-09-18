package io.rebble.cobble.shared.ui.viewmodel

import androidx.lifecycle.ViewModel
import io.rebble.cobble.shared.database.entity.SyncedLockerEntry
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryWithPlatforms
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.skia.Image

class LockerWatchfaceItemViewModel(val entry: SyncedLockerEntryWithPlatforms): ViewModel() {
    open class ImageState {
        object Loading : ImageState()
        data class Loaded(val image: Image) : ImageState()
    }
    private var _imageState = MutableStateFlow(ImageState.Loading)
    val imageState = _imageState.asStateFlow()

    val title: String
        get() = entry.entry.title

    val developerName: String
        get() = entry.entry.developerName

    val circleWatchface: Boolean
        get() = entry.platforms.any { it.name == "chalk" } && entry.platforms.size == 1 //TODO: also display when chalk connected

    init {
        //TODO: Load image
    }
}