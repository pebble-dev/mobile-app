package io.rebble.cobble.shared.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import io.rebble.cobble.shared.data.WatchItem


class WatchesListViewModel : ViewModel(){
    private val _watches = mutableStateListOf(
            WatchItem("Pebble Time 2747","v4.4.0-rbl", false, false),
            WatchItem("Asterix ABXY", "v4.4.0-rbl", false, false),
            WatchItem("Rebble Steel", "v4.4.0-rbl", true, true)
    )
    val watches: List<WatchItem> = _watches

    private val _selectedWatch = mutableStateOf<WatchItem?>(null)
    val selectedWatch: State<WatchItem?> = _selectedWatch

    val connectedWatch: WatchItem? by derivedStateOf {
        _watches.find { it.isConnected }
    }

    val disconnectedWatches: List<WatchItem> by derivedStateOf {
        _watches.filter { !it.isConnected }
    }

    fun selectWatch(watch: WatchItem) {
        _selectedWatch.value = watch
    }

    fun clearSelection() {
        _selectedWatch.value = null
    }

    fun toggleConnection(watch: WatchItem, updateSheet: Boolean = false) {
        val index = _watches.indexOfFirst { it.name == watch.name }
        if (index != -1) {
            // TODO Actually connecting and disconnecting logic here
            for (i in 0..<_watches.count()) { // Disconnect any other watches
                if (i != index) { //Make sure that the connected watch is unaffected
                    _watches[i] = _watches[i].copy(isConnected = false)
                }
            }
            _watches[index] = _watches[index].copy(isConnected = !_watches[index].isConnected)
            if (updateSheet){ // Sometimes we don't want the sheet to pop up
                _selectedWatch.value = _watches[index]  // Ensure the bottom sheet updates
            }
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