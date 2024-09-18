package io.rebble.cobble.shared.ui.view.home.locker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.rebble.cobble.shared.database.entity.SyncedLockerEntry
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryWithPlatforms
import io.rebble.cobble.shared.ui.common.RebbleIcons
import io.rebble.cobble.shared.ui.viewmodel.LockerWatchfaceItemViewModel

@Composable
fun LockerWatchfaceItem(entry: SyncedLockerEntryWithPlatforms) {
    val viewModel: LockerWatchfaceItemViewModel = viewModel { LockerWatchfaceItemViewModel(entry) }
    Surface(tonalElevation = 1.dp) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.size(width = 92.dp, height = if (viewModel.circleWatchface) 92.dp else 108.dp)) { RebbleIcons.unknownApp() }
            Text(viewModel.title)
            Text(viewModel.developerName)
        }
    }
}