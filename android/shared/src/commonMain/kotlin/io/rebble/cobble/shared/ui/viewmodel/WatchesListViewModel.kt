package io.rebble.cobble.shared.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import io.rebble.cobble.shared.data.WatchItem


class WatchesListViewModel : ViewModel(){
    private val _watches = mutableStateListOf(
            WatchItem("Pebble Time 2747", false),
            WatchItem("Asterix ABXY", false),
            WatchItem("Rebble Steel", true)
    )
    val watches: List<WatchItem> = _watches

    private val _selectedWatch = mutableStateOf<WatchItem?>(null)
    val selectedWatch: State<WatchItem?> = _selectedWatch

    fun selectWatch(watch: WatchItem) {
        _selectedWatch.value = watch
    }

    fun clearSelection() {
        _selectedWatch.value = null
    }

    fun toggleConnection(watch: WatchItem) {
        val index = _watches.indexOfFirst { it.name == watch.name }
        if (index != -1) {
            // TODO Actually connecting and disconnecting logic here
            _watches[index] = _watches[index].copy(isConnected = !_watches[index].isConnected)
            _selectedWatch.value = _watches[index]  // Ensure the bottom sheet updates
        }
    }

    fun forgetWatch(watch: WatchItem) {
        val index = _watches.indexOfFirst { it.name == watch.name }
        if (index != -1) {
            // TODO Remove the watch from the database here
        }
    }

    fun checkForUpdates(watch: WatchItem) {
        val index = _watches.indexOfFirst { it.name == watch.name }
        if (index != -1) {
            // TODO Check for Updates logic here
        }
    }
}