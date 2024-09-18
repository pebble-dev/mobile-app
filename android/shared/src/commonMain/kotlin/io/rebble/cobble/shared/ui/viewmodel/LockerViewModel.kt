package io.rebble.cobble.shared.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.rebble.cobble.shared.database.dao.LockerDao
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryWithPlatforms
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LockerViewModel(private val lockerDao: LockerDao): ViewModel() {
    open class LockerEntriesState {
        object Loading : LockerEntriesState()
        data class Loaded(val entries: List<SyncedLockerEntryWithPlatforms>) : LockerEntriesState()
    }

    private val _entriesState: MutableStateFlow<LockerEntriesState> = MutableStateFlow(LockerEntriesState.Loading)
    val entriesState = _entriesState.asStateFlow()
    private var mutex = Mutex()
    private var lastJob: Job? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _entriesState.value = LockerEntriesState.Loaded(lockerDao.getAllEntries())
        }
    }

    suspend fun updateOrder(entries: List<SyncedLockerEntryWithPlatforms>) {
        lastJob?.cancel()
        lastJob = viewModelScope.launch(Dispatchers.IO) {
            mutex.withLock {
                entries.forEachIndexed { i, e ->
                    lockerDao.updateOrder(e.entry.id, i+1)
                }
            }
        }
        _entriesState.value = LockerEntriesState.Loaded(entries)
    }
}