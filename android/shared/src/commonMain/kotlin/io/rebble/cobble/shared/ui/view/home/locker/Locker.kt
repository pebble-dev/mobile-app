package io.rebble.cobble.shared.ui.view.home.locker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.rebble.cobble.shared.database.dao.LockerDao
import io.rebble.cobble.shared.database.entity.SyncedLockerEntryWithPlatforms
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.koin.compose.getKoin

enum class LockerTabs(val label: String) {
    Apps("Apps"),
    Watchfaces("Watchfaces"),
}

@Composable
fun Locker(lockerDao: LockerDao = getKoin().get()) {
    val scope = rememberCoroutineScope()
    val (entries, setEntries) = remember { mutableStateOf<List<SyncedLockerEntryWithPlatforms>?>(null) }
    val tab = remember { mutableStateOf(LockerTabs.Apps) }

    scope.launch(Dispatchers.IO) {
        val entries = lockerDao.getAllEntries()
        setEntries(entries)
    }

    Column {
        Surface(
            modifier = Modifier.fillMaxWidth().height(100.dp)
        ) {
            Row {
                LockerTabs.entries.forEachIndexed { index, it ->
                    NavigationBarItem(
                            selected = tab.value == it,
                            onClick = { tab.value = it },
                            icon = { Text(it.label) },
                    )
                }
            }
        }

        entries?.let {
            when (tab.value) {
                LockerTabs.Apps -> {
                    LockerAppList(it.filter { it.entry.type == "watchapp" })
                }

                LockerTabs.Watchfaces -> {
                    TODO()
                }
            }
        } ?: run {
            CircularProgressIndicator(modifier = Modifier.align(CenterHorizontally))
        }
    }
}