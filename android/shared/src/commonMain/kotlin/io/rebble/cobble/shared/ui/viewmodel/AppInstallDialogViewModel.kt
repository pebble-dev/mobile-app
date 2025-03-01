package io.rebble.cobble.shared.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benasher44.uuid.uuidFrom
import io.rebble.cobble.shared.domain.pbw.PbwApp
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.cobble.shared.domain.state.watchOrNull
import io.rebble.cobble.shared.middleware.PutBytesController
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class AppInstallDialogViewModel(val uri: String) : ViewModel() {
    open class AppInstallState {
        class Installing(val progress: Double) : AppInstallState()

        object Success : AppInstallState()

        class Error(val message: String) : AppInstallState()
    }

    private val _state = MutableStateFlow<AppInstallState?>(null)
    val state = _state.asStateFlow()

    val app = PbwApp(uri)

    fun installApp() {
        viewModelScope.launch {
            _state.value = AppInstallState.Installing(0.0)
            try {
                app.installToLockerCache()
                val device = ConnectionStateManager.connectionState.value.watchOrNull
                if (device == null) {
                    _state.value = AppInstallState.Error("No connected device")
                    return@launch
                }
                device.appRunStateService.startApp(uuidFrom(app.info.uuid))
                viewModelScope.launch {
                    try {
                        device.putBytesController.status.drop(1).takeWhile {
                            it.state != PutBytesController.State.IDLE
                        }.timeout(15.seconds).collect {
                            _state.value = AppInstallState.Installing(it.progress)
                        }
                        _state.value = AppInstallState.Success
                    } catch (e: TimeoutCancellationException) {
                        _state.value = AppInstallState.Error("Timed out while installing")
                    }
                }
            } catch (e: Exception) {
                _state.value = AppInstallState.Error(e.message ?: "Unknown error")
            }
        }
    }
}