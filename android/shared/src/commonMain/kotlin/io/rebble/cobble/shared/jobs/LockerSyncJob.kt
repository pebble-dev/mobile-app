package io.rebble.cobble.shared.jobs

import io.rebble.cobble.shared.Logging
import io.rebble.cobble.shared.api.RWS
import io.rebble.cobble.shared.database.NextSyncAction
import io.rebble.cobble.shared.database.dao.LockerDao
import io.rebble.cobble.shared.database.entity.dataEqualTo
import io.rebble.cobble.shared.domain.api.appstore.toEntity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LockerSyncJob: KoinComponent {
    private val lockerDao: LockerDao by inject()
    suspend fun beginSync(): Boolean {
        val locker = RWS.appstoreClient?.getLocker() ?: return false
        val storedLocker = lockerDao.getAllEntries()

        val changedEntries = locker.filter { new ->
            storedLocker.any { old ->
                old.entry.id == new.id && !old.dataEqualTo(new.toEntity())
            }
        }
        val newEntries = locker.filter { new -> storedLocker.none { old -> old.entry.id == new.id } }
        val removedEntries = storedLocker.filter { old -> locker.none { nw -> nw.id == old.entry.id } }

        lockerDao.insertOrReplaceAll(newEntries.map { it.toEntity() })
        changedEntries.forEach {
            lockerDao.clearPlatformsFor(it.id)
        }
        lockerDao.insertOrReplaceAll(changedEntries.map { it.toEntity() })
        lockerDao.setNextSyncAction(removedEntries.map { it.entry.id }.toSet(), NextSyncAction.Delete)
        lockerDao.setNextSyncAction(changedEntries.map { it.id }.toSet(), NextSyncAction.Upload)
        Logging.i("Synced locker: ${newEntries.size} new, ${changedEntries.size} changed, ${removedEntries.size} removed")
        return true
    }
}