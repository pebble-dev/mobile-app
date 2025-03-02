package io.rebble.cobble.shared.ui.view.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.rebble.cobble.shared.database.dao.LockerDao
import io.rebble.cobble.shared.domain.state.ConnectionStateManager
import io.rebble.cobble.shared.domain.state.watchOrNull
import io.rebble.cobble.shared.jobs.LockerSyncJob
import io.rebble.libpebblecommon.packets.blobdb.BlobCommand
import io.rebble.libpebblecommon.packets.blobdb.BlobResponse
import kotlinx.coroutines.launch
import org.koin.compose.getKoin
import kotlin.random.Random

@Composable
fun TestPage(onShowSnackbar: (String) -> Unit) {
    val watchConnection by ConnectionStateManager.connectionState.collectAsState()
    val koin = getKoin()
    Column {
        OutlinedButton(onClick = {
            watchConnection.watchOrNull?.connectionScope?.value?.launch {
                val res =
                    watchConnection.watchOrNull?.blobDBService?.send(
                        BlobCommand.ClearCommand(
                            token = Random.nextInt().toUShort(),
                            database = BlobCommand.BlobDatabase.App
                        )
                    ) ?: BlobResponse.BlobStatus.WatchDisconnected
                onShowSnackbar("Response: ${res::class.simpleName}")
                val lockerDao: LockerDao = koin.get()
                lockerDao.clearAll()
                LockerSyncJob.schedule(koin.get())
            }
        }) {
            Text("Clear locker")
        }

        OutlinedButton(onClick = {
            watchConnection.watchOrNull?.connectionScope?.value?.launch {
                LockerSyncJob.schedule(koin.get())
                onShowSnackbar("Syncing locker")
            }
        }) {
            Text("Sync locker")
        }
    }
}