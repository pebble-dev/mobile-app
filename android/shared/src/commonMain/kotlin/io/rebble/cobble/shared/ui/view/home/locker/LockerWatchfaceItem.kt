package io.rebble.cobble.shared.ui.view.home.locker

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryWithPlatforms
import io.rebble.cobble.shared.ui.common.RebbleIcons
import io.rebble.cobble.shared.ui.viewmodel.LockerItemViewModel
import org.koin.compose.getKoin

@Composable
fun LockerWatchfaceItem(entry: SyncedLockerEntryWithPlatforms) {
    val koin = getKoin()
    val viewModel: LockerItemViewModel = viewModel(key = "locker-watchface-${entry.entry.id}") { LockerItemViewModel(koin.get(), entry) }
    val imageState: LockerItemViewModel.ImageState by viewModel.imageState.collectAsState()
    Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth().padding(8.dp).height(250.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            when (imageState) {
                is LockerItemViewModel.ImageState.Loaded -> {
                    Image(
                            modifier = Modifier
                                    .height(200.dp)
                                    .align(Alignment.CenterHorizontally),
                            bitmap = (imageState as LockerItemViewModel.ImageState.Loaded).image,
                            contentDescription = "Watchface screenshot",
                    )
                }

                is LockerItemViewModel.ImageState.Error -> {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        RebbleIcons.unknownApp(modifier = Modifier.size(56.dp).align(Alignment.Center))
                    }
                }

                is LockerItemViewModel.ImageState.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(56.dp).align(Alignment.Center))
                    }
                }
            }
            Text(viewModel.title, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Text(viewModel.developerName, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}