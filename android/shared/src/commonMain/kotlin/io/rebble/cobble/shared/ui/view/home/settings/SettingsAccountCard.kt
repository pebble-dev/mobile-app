package io.rebble.cobble.shared.ui.view.home.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.rebble.cobble.shared.ui.common.RebbleIcons
import io.rebble.cobble.shared.ui.viewmodel.SettingsAccountCardViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SettingsAccountCard(
    viewModel: SettingsAccountCardViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    SettingsAccountCard(
        state = state,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

@Composable
private fun SettingsAccountCard(
    state: SettingsAccountCardViewModel.State,
    onAction: (SettingsAccountCardViewModel.Action) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier) {
        when (state) {
            is SettingsAccountCardViewModel.State.Loading -> LoadingAccountCard()
            is SettingsAccountCardViewModel.State.SignedOut -> SignedOutAccountCard(onClick = {
                onAction(
                    SettingsAccountCardViewModel.Action.SignIn
                )
            })

            is SettingsAccountCardViewModel.State.UntrustedBootUrl -> UntrustedAccountCard(
                onReset = { onAction(SettingsAccountCardViewModel.Action.Reset) },
                onCopyUrl = { onAction(SettingsAccountCardViewModel.Action.CopyUrl) }
            )

            is SettingsAccountCardViewModel.State.SignedIn -> SignedInAccountCard(
                state = state,
                onSignOut = { onAction(SettingsAccountCardViewModel.Action.SignOut) },
                onManageAccount = { onAction(SettingsAccountCardViewModel.Action.ManageAccount) },
            )
        }
    }
}

@Composable
private fun LoadingAccountCard(modifier: Modifier = Modifier) {
    Column(modifier) {
        ListItem(
            headlineContent = { Text("Loading...") },
        )
    }
}


@Composable
private fun SignedOutAccountCard(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(modifier) {
        ListItem(
            modifier = Modifier.clickable(onClick = onClick),
            leadingContent = { RebbleIcons.rebbleStore() },
            headlineContent = { Text("Sign in to Rebble") },
            trailingContent = { RebbleIcons.caretRight() }
        )
    }
}

@Composable
private fun UntrustedAccountCard(
    onReset: () -> Unit,
    onCopyUrl: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        ListItem(
            headlineContent = { Text("Untrusted boot URL") },
            leadingContent = { RebbleIcons.rocket() },
        )
        ListItem(
            headlineContent = { Text("Tap to reveal...") },
            leadingContent = { RebbleIcons.aboutApp() },
        )
        Row {
            Text("Reset", modifier = Modifier.clickable(onClick = onReset))
            Text("Copy URL", modifier = Modifier.clickable(onClick = onCopyUrl))
        }
    }
}

@Composable
private fun SignedInAccountCard(
    state: SettingsAccountCardViewModel.State.SignedIn,
    onSignOut: () -> Unit,
    onManageAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        ListItem(
            headlineContent = { Text("Rebble account") },
            supportingContent = { Text(state.accountName) },
            leadingContent = {
                RebbleIcons.rebbleStore()
            }
        )
        ListItem(
            headlineContent = { Text("Voice and weather subscription") },
            supportingContent = { Text(state.weatherVoiceSubscriptionStatus) },
            leadingContent = {
                RebbleIcons.dictationMicrophone()
            }
        )
        ListItem(
            headlineContent = { Text("Timeline sync") },
            supportingContent = { Text(state.timelineSyncInterval) },
            leadingContent = {
                RebbleIcons.timelinePin()
            }
        )
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(12.dp)
        ) {
            Text("Sign Out", modifier = Modifier.clickable(onClick = onSignOut))
            Spacer(Modifier.width(40.dp))
            Text("Manage account", Modifier.clickable(onClick = onManageAccount))
        }
    }
}