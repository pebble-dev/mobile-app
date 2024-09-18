package io.rebble.cobble.shared.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.rebble.cobble.shared.database.dao.LockerDao
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryWithPlatforms
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LockerViewModel(private val lockerDao: LockerDao): ViewModel() {
    open class LockerEntriesState {
        object Loading : LockerEntriesState()
        data class Loaded(val entries: List<SyncedLockerEntryWithPlatforms>) : LockerEntriesState()
    }
    val entriesState = lockerDao.getAllEntriesFlow().map {
        LockerEntriesState.Loaded(it)
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.Eagerly, LockerEntriesState.Loading)

    private var mutex = Mutex()
    private var lastJob: Job? = null

    suspend fun updateOrder(entries: List<SyncedLockerEntryWithPlatforms>) {
        lastJob?.cancel()
        lastJob = viewModelScope.launch(Dispatchers.IO) {
            mutex.withLock {
                entries.forEachIndexed { i, e ->
                    if (e.entry.type == "watchapp") {
                        lockerDao.updateOrder(e.entry.id, i+1)
                    }
                }
            }
        }
    }
}