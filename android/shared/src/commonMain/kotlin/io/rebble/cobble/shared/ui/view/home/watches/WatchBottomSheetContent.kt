package io.rebble.cobble.shared.ui.view.home.watches

import android.shared.generated.resources.*
import android.shared.generated.resources.Res
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.rebble.cobble.shared.data.WatchItem
import io.rebble.cobble.shared.ui.common.AppIconContainer
import io.rebble.cobble.shared.ui.common.RebbleIcons
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

val CONNECTED_BACKGROUND = Color(121,249,205)
val UPDATE_FOREGROUND = Color(0, 108, 81)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchBottomSheetContent(watch: WatchItem,
                            onToggleConnection: () -> Unit,
                            onForgetWatch: () -> Unit,
                            onCheckForUpdates: () -> Unit,
                            clearSelection: () -> Unit) {

    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()  // First, hide the sheet smoothly
                }.invokeOnCompletion {
                    clearSelection()  // Then, reset selected watch
                }
            },
            sheetState = sheetState
    ){
        Column(
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
        ) {
            Row(
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                AppIconContainer(color = if (watch.isConnected) {
                                            CONNECTED_BACKGROUND
                                        } else {
                                            MaterialTheme.colorScheme.primaryContainer
                                        },
                                content = { RebbleIcons.deadWatchGhost80() } ) //TODO Switch With Watch Icon

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(text = watch.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge)
                    Text(
                            text = if (watch.isConnected && watch.updateAvailable){
                                        "${watch.softwareVersion} - ${stringResource(Res.string.update_available)}!"
                                    } else if(watch.isConnected) {
                                        "${watch.softwareVersion} - ${stringResource(Res.string.connected)}!"
                                    } else {
                                        stringResource(Res.string.disconnected)
                                    },

                            color = if (watch.isConnected && watch.updateAvailable){
                                        UPDATE_FOREGROUND
                                    } else {
                                        MaterialTheme.colorScheme.secondary
                                    },

                            fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.secondary)

            Row(
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable { onToggleConnection() },
                    verticalAlignment = Alignment.CenterVertically
            ) {

                if (watch.isConnected){
                    RebbleIcons.disconnectFromWatch(tint = MaterialTheme.colorScheme.secondary)
                } else {
                    RebbleIcons.connectToWatch(tint = MaterialTheme.colorScheme.secondary)
                }

                Spacer(modifier = Modifier.width(12.dp))
                Text(
                        text = if (watch.isConnected){
                                    stringResource(Res.string.disconnect_watch)
                                } else {
                                    stringResource(Res.string.connect_watch)
                                },

                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                )
            }
            Row(
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .alpha(if (watch.isConnected) {
                                        1.0f
                                    } else{
                                        0.7f
                                    })
                            .clickable(enabled = watch.isConnected,
                                        onClick = { onCheckForUpdates() }),
                    verticalAlignment = Alignment.CenterVertically
            ) {

                if (watch.updateAvailable && watch.isConnected){
                    RebbleIcons.applyUpdate(tint = UPDATE_FOREGROUND)
                }
                else {
                    RebbleIcons.checkForUpdates(tint = MaterialTheme.colorScheme.secondary)
                }

                Spacer(modifier = Modifier.width(12.dp))
                Text(
                        text = if (watch.updateAvailable && watch.isConnected){
                            stringResource(Res.string.download_update)
                        } else {
                            stringResource(Res.string.check_for_updates)
                        },

                        color = if (watch.updateAvailable && watch.isConnected){
                                    UPDATE_FOREGROUND
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                },

                        fontWeight = FontWeight.SemiBold,
                )
            }

            HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.secondary)

            Row(
                    modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable { onForgetWatch() },
                    verticalAlignment = Alignment.CenterVertically
            ) {
                RebbleIcons.unpairFromWatch(tint = Color.Red)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                        text = stringResource(Res.string.forget_watch),
                        color = Color.Red
                )
            }
        }
    }
}