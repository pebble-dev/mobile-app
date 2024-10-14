package io.rebble.cobble.shared.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.database.dao.LockerDao
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryWithPlatforms
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LockerViewModel(private val lockerDao: LockerDao): ViewModel() {
    open class LockerEntriesState {
        object Loading : LockerEntriesState()
        object Error : LockerEntriesState()
        data class Loaded(val entries: List<SyncedLockerEntryWithPlatforms>) : LockerEntriesState()
    }
    open class ModalSheetState {
        object Closed : ModalSheetState()
        data class Open(val viewModel: LockerItemViewModel) : ModalSheetState()
    }

    val entriesState = MutableStateFlow<LockerEntriesState>(LockerEntriesState.Loading)
    init {
        viewModelScope.launch(Dispatchers.IO + CoroutineName("LockerViewModelGet")) {
            lockerDao.getAllEntriesFlow().catch {
                Logging.e("Error loading locker entries", it)
                entriesState.value = LockerEntriesState.Error
            }.collect { entries ->
                entriesState.value = LockerEntriesState.Loaded(entries)
            }
        }
    }

    private var mutex = Mutex()
    private var lastJob: Job? = null
    private val _modalSheetState = MutableStateFlow<ModalSheetState>(ModalSheetState.Closed)
    val modalSheetState: StateFlow<ModalSheetState> = _modalSheetState
    val watchIsConnected = ConnectionStateManager.isConnected
    val searchQuery = MutableStateFlow<String?>(null)

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

    fun openModalSheet(viewModel: LockerItemViewModel) {
        _modalSheetState.value = ModalSheetState.Open(viewModel)
    }

    fun closeModalSheet() {
        _modalSheetState.value = ModalSheetState.Closed
    }
}