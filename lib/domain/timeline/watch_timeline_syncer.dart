import 'package:cobble/domain/db/dao/timeline_pin_dao.dart';
import 'package:cobble/domain/db/models/next_sync_action.dart';
import 'package:cobble/domain/timeline/blob_status.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.dart';
import 'package:hooks_riverpod/all.dart';

import '../logging.dart';

/// Sync controller that handles synchronization between internal database and
/// watch
///
/// Should only be used from background isolate
class WatchTimelineSyncer {
  final TimelinePinDao timelinePinDao;
  final TimelineSyncControl timelineSyncControl;
  final TimelineControl timelineControl = TimelineControl();

  WatchTimelineSyncer(this.timelinePinDao, this.timelineSyncControl);

  Future<bool> syncPinDatabaseWithWatch() async {
    final status = await _performSync();

    switch (status) {
      case statusSuccess:
        Log.d("Timeline Pin Sync OK");
        return true;
      case statusInvalidOperation:
      case statusInvalidDatabaseId:
      case statusInvalidData:
      case statusKeyDoesNotExist:
      case statusDataStale:
      case statusNotSupported:
      case statusLocked:
        Log.e(
            "Timeline Pin Sync failed due to a bug in the sync engine: $status");
        return false;
      case statusDatabaseFull:
        Log.w("Timeline Pin Sync database is full");
        // TODO display notification to the user
        return true;
      case statusGeneralFailure:
      case statusTryLater:
      case statusWatchDisconnected:
      default:
        Log.w("Timeline Pin Sync failed ($status). Retrying later...");
        // We have no idea what has gone wrong
        await this.timelineSyncControl.syncTimelineToWatchLater();
        return false;
    }
  }

  Future<int> _performSync() async {
    try {
      final pinsToDelete = await timelinePinDao.getAllPinsWithPendingDelete();
      for (final pinToDelete in pinsToDelete) {
        final StringWrapper idWrapper = StringWrapper();
        idWrapper.value = pinToDelete.itemId.toString();

        final res = await timelineControl.removePin(idWrapper);

        if (res.value != statusSuccess) {
          return res.value;
        }

        await timelinePinDao.delete(pinToDelete.itemId);
      }

      final pinsToUpload = await timelinePinDao.getAllPinsWithPendingUpload();
      for (final pinToSync in pinsToUpload) {
        final res = await timelineControl.addPin(pinToSync.toPigeon());

        if (res.value != statusSuccess) {
          return res.value;
        }

        await timelinePinDao.setSyncAction(
          pinToSync.itemId,
          NextSyncAction.Nothing,
        );
      }
    } catch (e) {
      // Log error to native
      return statusInvalidData;
    }

    return statusSuccess;
  }

  Future<int> removeAllPins() async {
    final res = await timelineControl.removeAllPins();

    if (res.value != statusSuccess) {
      return res.value;
    }

    await timelinePinDao.deleteAll();
    return statusSuccess;
  }
}

final watchTimelineSyncerProvider =
Provider.autoDispose<WatchTimelineSyncer>((ref) {
  final timelinePinDao = ref.watch(timelinePinDaoProvider);
  final timelineSyncControl = ref.watch(timelineSyncControlProvider);

  return WatchTimelineSyncer(timelinePinDao, timelineSyncControl);
});

final timelineSyncControlProvider = Provider((ref) => TimelineSyncControl());