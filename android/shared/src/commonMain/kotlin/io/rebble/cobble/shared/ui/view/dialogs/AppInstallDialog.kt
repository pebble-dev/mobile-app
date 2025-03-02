package io.rebble.cobble.shared.ui.view.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.rebble.cobble.shared.ui.LocalTheme
import io.rebble.cobble.shared.ui.common.RebbleIcons
import io.rebble.cobble.shared.ui.viewmodel.AppInstallDialogViewModel

@Composable
fun AppInstallDialog(
    uri: String,
    onDismissRequest: () -> Unit
) {
    val viewModel = viewModel { AppInstallDialogViewModel(uri) }
    val appInstallState by viewModel.state.collectAsState()
    AlertDialog(
        onDismissRequest = onDismissRequest,
        iconContentColor = LocalTheme.current.materialColors.primary,
        icon = {
            when (appInstallState) {
                null -> RebbleIcons.warning()
                is AppInstallDialogViewModel.AppInstallState.Installing -> RebbleIcons.appsCrate()
                is AppInstallDialogViewModel.AppInstallState.Error -> RebbleIcons.warning()
                is AppInstallDialogViewModel.AppInstallState.Success -> RebbleIcons.sendToWatchChecked()
            }
        },
        title = {
            when (appInstallState) {
                null -> Text("Install external app?")
                is AppInstallDialogViewModel.AppInstallState.Installing -> Text("Installing...")
                is AppInstallDialogViewModel.AppInstallState.Error -> Text("Error")
                is AppInstallDialogViewModel.AppInstallState.Success ->
                    Text(
                        "Successfully Installed"
                    )
            }
        },
        text = {
            when (appInstallState) {
                null ->
                    Text(
                        buildAnnotatedString {
                            append("Do you want to install the app ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(
                                    viewModel.app.info.longName.ifBlank { viewModel.app.info.shortName }
                                )
                            }
                            append(" by ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(
                                    viewModel.app.info.companyName.ifBlank { "<Not Specified>" }
                                )
                            }
                            append(
                                "?\nPlease note there's no verification of the app's authenticity or safety."
                            )
                        }
                    )
                is AppInstallDialogViewModel.AppInstallState.Installing -> {
                    val progress = (appInstallState as AppInstallDialogViewModel.AppInstallState.Installing).progress.toFloat()
                    LinearProgressIndicator(
                        progress = { progress }
                    )
                }
                is AppInstallDialogViewModel.AppInstallState.Success -> {}
                is AppInstallDialogViewModel.AppInstallState.Error -> {
                    val message = (appInstallState as AppInstallDialogViewModel.AppInstallState.Error).message
                    Text(message)
                }
            }
        },
        confirmButton = {
            if (appInstallState == null) {
                TextButton(
                    content = { Text("Install") },
                    onClick = {
                        viewModel.installApp()
                    }
                )
            } else if (appInstallState is AppInstallDialogViewModel.AppInstallState.Success || appInstallState is AppInstallDialogViewModel.AppInstallState.Error) {
                TextButton(
                    content = { Text("Close") },
                    onClick = { onDismissRequest() }
                )
            }
        },
        dismissButton = {
            if (appInstallState == null) {
                TextButton(
                    content = { Text("Cancel") },
                    onClick = { onDismissRequest() }
                )
            }
        }
    )
}